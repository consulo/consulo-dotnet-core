package consulo.dotnet.core.msbuild.impl.debug;

import consulo.dotnet.debugger.proxy.DotNetMethodProxy;
import consulo.dotnet.debugger.proxy.DotNetSourceLocation;
import consulo.execution.debugger.dap.protocol.StackFrame;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2024-12-25
 */
public class DotNetSourceLocationFromStackFrame implements DotNetSourceLocation {
    private final StackFrame myStackFrame;

    public DotNetSourceLocationFromStackFrame(StackFrame stackFrame) {
        myStackFrame = stackFrame;
    }

    @Nullable
    @Override
    public String getFilePath() {
        return myStackFrame.source.path;
    }

    @Override
    public int getLineZeroBased() {
        return myStackFrame.line;
    }

    @Override
    public int getLineOneBased() {
        return getLineZeroBased() + 1;
    }

    @Override
    public int getColumn() {
        return myStackFrame.column;
    }

    @Nonnull
    @Override
    public DotNetMethodProxy getMethod() {
        return new DotNetMethodProxyOverPsi();
    }
}
