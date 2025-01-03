package consulo.dotnet.core.msbuild.impl.debug;

import consulo.dotnet.debugger.proxy.*;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;
import consulo.execution.debugger.dap.protocol.StackFrame;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2024-12-25
 */
public class DotNetCoreStackFrameProxy implements DotNetStackFrameProxy {
    private final StackFrame myTrace;
    private final DotNetCoreThreadProxy myThreadProxy;

    public DotNetCoreStackFrameProxy(StackFrame trace, DotNetCoreThreadProxy threadProxy) {
        myTrace = trace;
        myThreadProxy = threadProxy;
    }

    @Override
    public int getIndex() {
        return myTrace.id;
    }

    @Nonnull
    @Override
    public DotNetThreadProxy getThread() {
        return myThreadProxy;
    }

    @Nonnull
    @Override
    public Object getEqualityObject() {
        return myTrace.id;
    }

    @Nullable
    @Override
    public DotNetSourceLocation getSourceLocation() {
        return new DotNetSourceLocationFromStackFrame(myTrace);
    }

    @Nonnull
    @Override
    public DotNetValueProxy getThisObject() throws DotNetInvalidObjectException, DotNetInvalidStackFrameException, DotNetAbsentInformationException {
        return new DotNetValueProxyImpl();
    }

    @Nullable
    @Override
    public DotNetValueProxy getParameterValue(@Nonnull DotNetMethodParameterProxy dotNetMethodParameterProxy) {
        return null;
    }

    @Override
    public void setParameterValue(@Nonnull DotNetMethodParameterProxy dotNetMethodParameterProxy, @Nonnull DotNetValueProxy dotNetValueProxy) {

    }

    @Nullable
    @Override
    public DotNetValueProxy getLocalValue(@Nonnull DotNetLocalVariableProxy dotNetLocalVariableProxy) {
        return null;
    }

    @Override
    public void setLocalValue(@Nonnull DotNetLocalVariableProxy dotNetLocalVariableProxy, @Nonnull DotNetValueProxy dotNetValueProxy) {

    }
}
