package consulo.dotnet.core.msbuild.impl.extension;

import consulo.container.plugin.PluginManager;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkType;
import consulo.dotnet.compiler.DotNetMacroUtil;
import consulo.dotnet.core.bundle.DotNetCoreBundleType;
import consulo.dotnet.core.msbuild.impl.debug.DotNetCoreDebugProcess;
import consulo.dotnet.debugger.impl.DotNetDebugProcess;
import consulo.dotnet.util.DebugConnectionInfo;
import consulo.execution.configuration.RunProfile;
import consulo.execution.debug.XDebugSession;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.msbuild.dotnet.impl.module.extension.MSBuildBaseDotNetModuleExtension;
import consulo.platform.Platform;
import consulo.platform.PlatformOperatingSystem;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.File;
import java.io.IOException;

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
        return new DotNetCoreDebugProcess(session, runProfile, info.getHost(), info.getPort());
    }

    @Nonnull
    @Override
    public GeneralCommandLine createDefaultCommandLine(@Nonnull Sdk sdk,
                                                       @Nullable DebugConnectionInfo debugConnectionInfo) throws ExecutionException {
        GeneralCommandLine commandLine = new GeneralCommandLine();
        String file = DotNetMacroUtil.expandOutputFile(this);

        String dotNetExePath;
        try {
            dotNetExePath = DotNetCoreBundleType.getExecutablePath(sdk.getHomePath()).getCanonicalPath();
        }
        catch (IOException e) {
            throw new ExecutionException(e);
        }

        if (debugConnectionInfo != null) {
            Platform platform = Platform.current();
            PlatformOperatingSystem os = platform.os();

            String exePath = os.isWindows() ? "netcoredbg.exe" : "netcoredbg";
            String dirPath = platform.os().fileNamePrefix() + "-netcoredbg" + platform.jvm().arch().fileNameSuffix();

            File pluginPath = PluginManager.getPluginPath(MSBuildDotNetCoreModuleExtension.class);

            File fullPath = new File(new File(pluginPath, dirPath), exePath);

            if (!fullPath.exists()) {
                throw new ExecutionException("Debug not supported. Path not exists: " + fullPath);
            }

            if (!os.isWindows() && !fullPath.canExecute()) {
                fullPath.setExecutable(true);
            }

            commandLine.setExecutable(fullPath.toPath());
            commandLine.addParameter("--interpreter=vscode");
            commandLine.addParameter("--server=" + debugConnectionInfo.getPort());
            commandLine.addParameter("--");
            commandLine.addParameter(dotNetExePath);
            commandLine.addParameter(file);
        } else {
            commandLine.setExePath(dotNetExePath);
            commandLine.addParameter(file);
        }

        return commandLine;
    }

    @Nonnull
    @Override
    public String getDebugFileExtension() {
        return "pdb";
    }
}
