package consulo.dotnet.core.msbuild.impl.debug;

import consulo.dotnet.debugger.proxy.DotNetThreadProxy;
import consulo.dotnet.debugger.proxy.DotNetTypeProxy;
import consulo.dotnet.debugger.proxy.DotNetVirtualMachineProxy;
import consulo.dotnet.debugger.proxy.value.*;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

/**
 * @author VISTALL
 * @since 2024-12-25
 */
public class DotNetCoreVirtualMachineProxy implements DotNetVirtualMachineProxy {
    private final DotNetCoreDebugProcess myProcess;

    public DotNetCoreVirtualMachineProxy(DotNetCoreDebugProcess process) {
        myProcess = process;
    }

    @Nullable
    @Override
    public DotNetTypeProxy findType(@Nonnull Project project, @Nonnull String s, @Nonnull VirtualFile virtualFile) {
        return null;
    }

    @Nullable
    @Override
    public DotNetTypeProxy findTypeInCorlib(@Nonnull String s) {
        return null;
    }

    @Override
    public void invoke(@Nonnull Runnable runnable) {
        ForkJoinPool.commonPool().execute(runnable);
    }

    @Nonnull
    @Override
    public List<DotNetThreadProxy> getThreads() {
        return myProcess.getThreads()
            .entrySet()
            .stream()
            .map(e -> new DotNetCoreThreadProxy(e.getKey(), e.getValue(), myProcess))
            .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public DotNetStringValueProxy createStringValue(@Nonnull String s) {
        return null;
    }

    @Nonnull
    @Override
    public DotNetCharValueProxy createCharValue(char c) {
        return null;
    }

    @Nonnull
    @Override
    public DotNetBooleanValueProxy createBooleanValue(boolean b) {
        return null;
    }

    @Nonnull
    @Override
    public DotNetNumberValueProxy createNumberValue(int i, @Nonnull Number number) {
        return null;
    }

    @Nonnull
    @Override
    public DotNetNullValueProxy createNullValue() {
        return null;
    }
}
