package consulo.dotnet.core.msbuild.impl.debug;

import consulo.application.ApplicationManager;
import consulo.dotnet.debugger.DotNetDebugContext;
import consulo.dotnet.debugger.impl.DotNetDebugProcess;
import consulo.dotnet.debugger.impl.DotNetEditorsProvider;
import consulo.dotnet.debugger.impl.DotNetSuspendContext;
import consulo.dotnet.debugger.impl.breakpoint.DotNetLineBreakpointType;
import consulo.dotnet.debugger.impl.breakpoint.properties.DotNetLineBreakpointProperties;
import consulo.dotnet.debugger.impl.nodes.logicView.ArrayDotNetLogicValueView;
import consulo.dotnet.debugger.impl.nodes.logicView.DefaultDotNetLogicValueView;
import consulo.dotnet.debugger.impl.nodes.logicView.EnumerableDotNetLogicValueView;
import consulo.dotnet.debugger.impl.nodes.logicView.StringDotNetLogicValueView;
import consulo.dotnet.debugger.nodes.logicView.DotNetLogicValueView;
import consulo.dotnet.debugger.proxy.DotNetVirtualMachineProxy;
import consulo.execution.ExecutionResult;
import consulo.execution.configuration.RunProfile;
import consulo.execution.debug.XBreakpointManager;
import consulo.execution.debug.XDebugSession;
import consulo.execution.debug.XDebuggerManager;
import consulo.execution.debug.breakpoint.XBreakpoint;
import consulo.execution.debug.breakpoint.XLineBreakpoint;
import consulo.execution.debug.evaluation.XDebuggerEditorsProvider;
import consulo.execution.debugger.dap.protocol.DAP;
import consulo.execution.debugger.dap.protocol.DAPFactory;
import consulo.execution.ui.ExecutionConsole;
import consulo.execution.ui.console.TextConsoleBuilderFactory;
import consulo.process.ProcessHandler;
import consulo.util.lang.lazy.LazyValue;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 2024-12-22
 */
public class DotNetCoreDebugProcess extends DAPDebugProcess implements DotNetDebugProcess {
    private final String myHost;
    private final int myPort;

    private ExecutionResult myResult;
    private final RunProfile myRunProfile;
    protected final XDebuggerManager myDebuggerManager;

    private LazyValue<DotNetLogicValueView[]> myLogicValueViewsLazy;

    private DotNetCoreVirtualMachineProxy myVirtualMachine;

    public DotNetCoreDebugProcess(@Nonnull XDebugSession session,
                                  @Nonnull RunProfile runProfile,
                                  String host,
                                  int port) {
        super(session);

        myRunProfile = runProfile;
        myDebuggerManager = XDebuggerManager.getInstance(session.getProject());
        myLogicValueViewsLazy = LazyValue.notNull(this::createLogicValueViews);
        myHost = host;
        myPort = port;
    }

    @Override
    protected void onInitialized() {
        super.onInitialized();

        myVirtualMachine = new DotNetCoreVirtualMachineProxy(this);
    }

    @Override
    protected void doPause(@Nullable XBreakpoint<?> breakpoint, int threadId) {
        DotNetDebugContext context = createDebugContext(myVirtualMachine, null);
        if (breakpoint != null) {
            getSession().breakpointReached(breakpoint, null, new DotNetSuspendContext(context, threadId));
        }
        else {
            getSession().positionReached(new DotNetSuspendContext(context, threadId));
        }
    }

    @Nonnull
    protected DotNetLogicValueView[] createLogicValueViews() {
        return new DotNetLogicValueView[]{
            new ArrayDotNetLogicValueView(),
            new StringDotNetLogicValueView(),
            new EnumerableDotNetLogicValueView(),
            new DefaultDotNetLogicValueView()
        };
    }

    @Override
    protected DAP createDAP(DAPFactory factory) {
        return factory.createSocketDAP(myHost, myPort);
    }

    @Override
    protected String getAdapterId() {
        return "netcoredbg";
    }

    @Nonnull
    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return new DotNetEditorsProvider(getSession());
    }

    @Nonnull
    @Override
    public DotNetDebugContext createDebugContext(@Nonnull DotNetVirtualMachineProxy proxy, @Nullable XBreakpoint<?> breakpoint) {
        return new DotNetDebugContext(getSession().getProject(), proxy, myRunProfile, getSession(), breakpoint, myLogicValueViewsLazy.get());
    }

    @Override
    public void setExecutionResult(ExecutionResult executionResult) {
        myResult = executionResult;
    }

    @Nullable
    @Override
    protected ProcessHandler doGetProcessHandler() {
        return myResult.getProcessHandler();
    }

    @Nonnull
    @Override
    public ExecutionConsole createConsole() {
        ExecutionConsole executionConsole = myResult.getExecutionConsole();
        if (executionConsole == null) {
            return TextConsoleBuilderFactory.getInstance().createBuilder(getSession().getProject()).getConsole();
        }
        return executionConsole;
    }

    @Nonnull
    @Override
    public Collection<? extends XLineBreakpoint<?>> getLineBreakpoints() {
        return ApplicationManager.getApplication().runReadAction((Supplier<Collection<? extends XLineBreakpoint<DotNetLineBreakpointProperties>>>) () -> myDebuggerManager.getBreakpointManager()
            .getBreakpoints(DotNetLineBreakpointType.getInstance()));
    }

    @Nonnull
    @Override
    public XBreakpointManager getBreakpointManager() {
        return XDebuggerManager.getInstance(getSession().getProject()).getBreakpointManager();
    }
}
