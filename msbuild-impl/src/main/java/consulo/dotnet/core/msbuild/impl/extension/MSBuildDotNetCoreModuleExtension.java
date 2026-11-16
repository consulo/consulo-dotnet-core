package consulo.dotnet.core.msbuild.impl.extension;

import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkType;
import consulo.dotnet.compiler.DotNetMacroUtil;
import consulo.dotnet.core.bundle.DotNetCoreBundleType;
import consulo.dotnet.core.msbuild.impl.debug.DotNetCoreDebugProcess;
import consulo.dotnet.core.msbuild.impl.debug.DotNetCoreDebuggerCommandLine;
import consulo.dotnet.debugger.impl.DotNetDebugProcess;
import consulo.dotnet.util.DebugConnectionInfo;
import consulo.execution.configuration.RunProfile;
import consulo.execution.debug.XDebugSession;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.msbuild.dotnet.impl.module.extension.MSBuildBaseDotNetModuleExtension;
import consulo.platform.Platform;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 18/01/2021
 */
public class MSBuildDotNetCoreModuleExtension extends MSBuildBaseDotNetModuleExtension<MSBuildDotNetCoreModuleExtension> {
    public MSBuildDotNetCoreModuleExtension(@Nonnull String id, @Nonnull ModuleRootLayer moduleRootLayer) {
        super(id, moduleRootLayer);
    }

    @Nonnull
    @Override
    public Class<? extends SdkType> getSdkTypeClass() {
        return DotNetCoreBundleType.class;
    }

    @Nonnull
    @Override
    public DotNetDebugProcess createDebuggerProcess(@Nonnull XDebugSession session,
                                                    @Nonnull RunProfile runProfile,
                                                    @Nonnull DebugConnectionInfo info) {
        Sdk sdk = getSdk();
        // the debugger runs on the machine of the bundle
        Platform platform = sdk != null ? sdk.getPlatform() : Platform.current();
        return new DotNetCoreDebugProcess(session, runProfile, info.getHost(), info.getPort(), platform);
    }

    @Nonnull
    @Override
    public GeneralCommandLine createDefaultCommandLine(@Nonnull Sdk sdk,
                                                       @Nullable DebugConnectionInfo debugConnectionInfo) throws ExecutionException {
        return DotNetCoreDebuggerCommandLine.create(sdk, DotNetMacroUtil.expandOutputFile(this), debugConnectionInfo);
    }

    @Nonnull
    @Override
    public String getDebugFileExtension() {
        return "pdb";
    }
}
