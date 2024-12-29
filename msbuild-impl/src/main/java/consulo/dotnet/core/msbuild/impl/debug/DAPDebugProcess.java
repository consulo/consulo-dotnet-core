package consulo.dotnet.core.msbuild.impl.debug;

import consulo.application.Application;
import consulo.application.progress.Task;
import consulo.component.ProcessCanceledException;
import consulo.dotnet.debugger.impl.DotNetExecutionStack;
import consulo.execution.debug.XDebugProcess;
import consulo.execution.debug.XDebugSession;
import consulo.execution.debug.breakpoint.XBreakpoint;
import consulo.execution.debug.breakpoint.XLineBreakpoint;
import consulo.execution.debug.frame.XExecutionStack;
import consulo.execution.debug.frame.XSuspendContext;
import consulo.execution.debugger.dap.protocol.*;
import consulo.execution.debugger.dap.protocol.event.CapabilitiesEvent;
import consulo.execution.debugger.dap.protocol.event.OutputEvent;
import consulo.execution.debugger.dap.protocol.event.StoppedEvent;
import consulo.execution.debugger.dap.protocol.event.ThreadEvent;
import consulo.execution.ui.console.ConsoleViewContentType;
import consulo.logging.Logger;
import consulo.platform.Platform;
import consulo.ui.UIAccess;
import consulo.util.collection.MultiMap;
import consulo.util.concurrent.AsyncResult;
import consulo.util.lang.lazy.LazyValue;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * @author VISTALL
 * @since 2024-12-25
 */
public abstract class DAPDebugProcess extends XDebugProcess {
    private static final Logger LOG = Logger.getInstance(DAPDebugProcess.class);

    private LazyValue<DAP> myDapCache = LazyValue.atomicNotNull(() -> {
        DAP dap = createDAP(Application.get().getInstance(DAPFactory.class));
        init(dap);
        return dap;
    });

    private final Capabilities capabilities = new Capabilities();

    private Map<Integer, XLineBreakpoint> myBreakpointMapping = new ConcurrentHashMap<>();

    private Map<Integer, String> myThreads = new ConcurrentHashMap<>();

    public DAPDebugProcess(@Nonnull XDebugSession session) {
        super(session);
    }

    protected abstract DAP createDAP(DAPFactory factory);

    protected abstract String getAdapterId();

    public void start() {
        Application.get().executeOnPooledThread(this::initializeAsync);
    }

    @Nonnull
    protected abstract Collection<? extends XLineBreakpoint<?>> getLineBreakpoints();

    protected void init(DAP dap) {
        dap.registerEvent(CapabilitiesEvent.class, c -> {
            merge(capabilities, c);

            onUpdateCapabilities(capabilities);
        });

        dap.registerEvent(InitializedEvent.class, o -> onInitialized());

        dap.registerEvent(OutputEvent.class, this::onOutput);

        dap.registerEvent(BreakpointEvent.class, this::onBreakpoint);

        dap.registerEvent(ThreadEvent.class, threadEvent -> {
            switch (threadEvent.reason) {
                case ThreadEvent.THREAD_STARTED: {
                    myThreads.put(threadEvent.threadId, "Thread: " + threadEvent.threadId);
                    break;
                }
                case ThreadEvent.THREAD_EXITED: {
                    myThreads.remove(threadEvent.threadId);
                    break;
                }
            }
        });

        dap.registerEvent(StoppedEvent.class, v -> {
            if (v.hitBreakpointIds != null && v.hitBreakpointIds.length > 0) {
                for (int hitBreakpointId : v.hitBreakpointIds) {
                    XLineBreakpoint breakpoint = myBreakpointMapping.get(hitBreakpointId);
                    if (breakpoint != null) {
                        doPause(breakpoint, v.threadId);
                        break;
                    }
                }
            }
            else {
                doPause(null, v.threadId);
            }

//          getSession().breakpointReached()
//            try {
//                StackTraceArguments arguments = new StackTraceArguments();
//                if (v.threadId != null) {
//                    arguments.threadId = v.threadId;
//                }
//
//                StackTraceResult result = dap.stackTrace(arguments).get();
//
//                for (StackFrame frame : result.stackFrames) {
//                    ScopesResult scopesResult = dap.scopes(new ScopesArguments(frame.id)).get();
//
//                    for (Scope scope : scopesResult.scopes) {
//                        VariablesResult o = dap.variables(new VariablesArguments(scope.variablesReference)).get();
//                        System.out.println();
//                    }
//                }
//
//                ContinueArguments c = new ContinueArguments();
//                if (v.threadId != null) {
//                    c.threadId = v.threadId;
//                }
//                dap.continue_(c);
//            }
//            catch (InterruptedException | ExecutionException e) {
//                e.printStackTrace();
//            }
        });
    }

    public StackFrame[] getStackTraces(Integer threadId) {
        StackTraceArguments arguments = new StackTraceArguments();
        if (threadId != null) {
            arguments.threadId = threadId;
        }

        try {
            StackTraceResult result = myDapCache.get().stackTrace(arguments).get();
            return result.stackFrames;
        }
        catch (InterruptedException | ExecutionException e) {
            throw new ProcessCanceledException(e);
        }
    }

    public Map<Integer, String> getThreads() {
        return myThreads;
    }

    protected void doPause(@Nullable XBreakpoint<?> breakpoint, int threadId) {
    }

    protected void onInitialized() {
        registerBreakpoints(myDapCache.get());
    }

    protected void onBreakpoint(BreakpointEvent event) {
        XLineBreakpoint lineBreakpoint = myBreakpointMapping.get(event.breakpoint.id);
        if (lineBreakpoint == null) {
            return;
        }

        switch (event.reason) {
            case "changed": {
                updateBreakpointState(lineBreakpoint, event.breakpoint);
                break;
            }
            case "new":
                break;
            case "removed":
                break;
        }
    }

    protected void registerBreakpoints(DAP dap) {
        Collection<? extends XLineBreakpoint<?>> breakpoints = getLineBreakpoints();
        if (breakpoints.isEmpty()) {
            return;
        }

        MultiMap<String, XLineBreakpoint<?>> map = MultiMap.create();
        for (XLineBreakpoint<?> breakpoint : breakpoints) {
            String path = breakpoint.getPresentableFilePath();

            map.putValue(path, breakpoint);
        }

        for (Map.Entry<String, Collection<XLineBreakpoint<?>>> entry : map.entrySet()) {
            String filePath = entry.getKey();
            // copy - do not lose order
            List<XLineBreakpoint<?>> result = new ArrayList<>(entry.getValue());

            SetBreakpointsArguments arguments = new SetBreakpointsArguments();
            arguments.source = new Source();
            arguments.source.path = filePath;

            List<SourceBreakpoint> sourceBreakpoints = new ArrayList<>(result.size());

            for (XLineBreakpoint<?> breakpoint : result) {
                SourceBreakpoint sourceBreakpoint = new SourceBreakpoint();
                sourceBreakpoint.line = breakpoint.getLine();

                sourceBreakpoints.add(sourceBreakpoint);
            }

            arguments.breakpoints = sourceBreakpoints.toArray(SourceBreakpoint[]::new);

            dap.setBreakpoints(arguments).whenCompleteAsync((setBreakpointsResult, t) -> {
                if (t != null) {
                    LOG.warn(t);
                }
                else if (setBreakpointsResult != null) {
                    for (int i = 0; i < result.size(); i++) {
                        Breakpoint breakpoint = setBreakpointsResult.breakpoints[i];

                        XLineBreakpoint<?> lineBreakpoint = result.get(i);

                        myBreakpointMapping.put(breakpoint.id, lineBreakpoint);

                        updateBreakpointState(lineBreakpoint, breakpoint);
                    }
                }
            });
        }
    }

    protected void updateBreakpointState(XLineBreakpoint<?> lineBreakpoint, Breakpoint breakpoint) {
        XDebugSession session = getSession();
        UIAccess uiAccess = session.getProject().getUIAccess();

        uiAccess.give(() -> {
            if (breakpoint.verified) {
                session.setBreakpointVerified(lineBreakpoint);
            }
            else {
                session.setBreakpointInvalid(lineBreakpoint, breakpoint.message);
            }
        });
    }

    protected void onOutput(OutputEvent event) {
        getSession().getConsoleView().print(event.output, ConsoleViewContentType.LOG_INFO_OUTPUT);
    }

    protected void onUpdateCapabilities(Capabilities capabilities) {
    }

    private static void merge(Object to, Object from) {
        if (to.getClass() != from.getClass()) {
            throw new IllegalArgumentException();
        }

        try {
            Class<?> clazz = to.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                Object newValue = field.get(from);
                if (newValue != null) {
                    field.set(to, newValue);
                }
            }
        }
        catch (Exception e) {
            throw new Error(e);
        }
    }

    private void initializeAsync() {
        DAP dap = myDapCache.get();

        InitializeRequestArguments arguments = new InitializeRequestArguments();
        String ideName = Application.get().getName().get();

        arguments.clientID = ideName;
        arguments.clientName = ideName;
        arguments.adapterID = getAdapterId();

        dap.initialize(arguments).whenCompleteAsync((res, t) -> {
            if (res != null) {
                merge(capabilities, res);

                onUpdateCapabilities(capabilities);

                LaunchRequestArguments launch = new LaunchRequestArguments();
                launch.env = Platform.current().os().environmentVariables();

                dap.launch(launch).whenCompleteAsync((lResult, t1) -> {
                    if (lResult != null) {
                        dap.configurationDone(new ConfigurationDoneArguments());
                    }
                });
            }
            else if (t != null) {
                LOG.warn(t);
            }
        });
    }

    @Override
    public void resume(@Nullable XSuspendContext context) {
        ContinueArguments arguments = new ContinueArguments();
        if (context != null) {
            XExecutionStack stack = context.getActiveExecutionStack();

            if (stack instanceof DotNetExecutionStack dotNetExecutionStack) {
                arguments.threadId = (int) dotNetExecutionStack.getThreadProxy().getId();
            }
        }

        myDapCache.get().continue_(arguments);
    }

    @Nonnull
    @Override
    public AsyncResult<Void> stopAsync() {
        AsyncResult<Void> result = AsyncResult.undefined();
        Task.Backgroundable.queue(getSession().getProject(), "Waiting for debugger response...", indicator -> {
            stopImpl();
            result.setDone();
        });
        return result;
    }

    protected void stopImpl() {
        myDapCache.get().close();
    }

    @Override
    public boolean checkCanInitBreakpoints() {
        return false;
    }
}
