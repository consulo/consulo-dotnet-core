package consulo.dotnet.core.msbuild.impl.debug;

import consulo.dotnet.debugger.proxy.DotNetNotSuspendedException;
import consulo.dotnet.debugger.proxy.DotNetStackFrameProxy;
import consulo.dotnet.debugger.proxy.DotNetThreadProxy;
import consulo.execution.debugger.dap.protocol.StackFrame;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2024-12-25
 */
public class DotNetCoreThreadProxy extends DotNetThreadProxy {
    private final int myId;
    private final String myName;
    private final DotNetCoreDebugProcess myProcess;

    public DotNetCoreThreadProxy(int id, String name, DotNetCoreDebugProcess process) {
        myId = id;
        myName = name;
        myProcess = process;
    }

    @Override
    public long getId() {
        return myId;
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public boolean isSuspended() {
        return false;
    }

    @Nullable
    @Override
    public String getName() {
        return myName;
    }

    @Nonnull
    @Override
    public List<DotNetStackFrameProxy> getFrames() throws DotNetNotSuspendedException {
        StackFrame[] traces = myProcess.getStackTraces(myId);
        if (traces.length == 0) {
            return List.of();
        }

        List<DotNetStackFrameProxy> list = new ArrayList<>(traces.length);
        for (StackFrame trace : traces) {
            list.add(new DotNetCoreStackFrameProxy(trace, this));
        }
        return list;
    }
}
