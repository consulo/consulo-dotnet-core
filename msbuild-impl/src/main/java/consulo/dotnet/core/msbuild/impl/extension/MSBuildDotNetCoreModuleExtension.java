package consulo.dotnet.core.msbuild.impl.extension;

import consulo.application.platform.PlatformTransfer;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

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
        // the program runs on the machine of the bundle
        GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.withPlatform(sdk.getPlatform());
        String file = DotNetMacroUtil.expandOutputFile(this);

        String dotNetExePath = DotNetCoreBundleType.getExecutablePath(sdk).toString();

        if (debugConnectionInfo != null) {
            // the debugger of the machine of the bundle - bundled for its system, copied there
            Platform platform = sdk.getPlatform();
            PlatformOperatingSystem os = platform.os();

            String exePath = os.isWindows() ? "netcoredbg.exe" : "netcoredbg";
            String dirPath = os.fileNamePrefix() + "-netcoredbg" + platform.jvm().arch().fileNameSuffix();

            Path localDir = PluginManager.getPluginPath(MSBuildDotNetCoreModuleExtension.class).toPath().resolve(dirPath);
            if (!Files.isDirectory(localDir)) {
                throw new ExecutionException("Debug not supported. Path not exists: " + localDir);
            }

            Path fullPath;
            try {
                fullPath = PlatformTransfer.getInstance().transfer(localDir, platform).resolve(exePath);
                if (!os.isWindows() && !Files.isExecutable(fullPath)) {
                    Files.setAttribute(fullPath, "posix:permissions", PosixFilePermissions.fromString("rwxr-xr-x"));
                }
            }
            catch (IOException e) {
                throw new ExecutionException(e);
            }

            commandLine.withExecutablePath(fullPath);
            commandLine.addParameter("--interpreter=vscode");
            commandLine.addParameter("--server=" + debugConnectionInfo.getPort());
            commandLine.addParameter("--");
            commandLine.addParameter(dotNetExePath);
            commandLine.addParameter(file);
        } else {
            commandLine.withExecutablePath(DotNetCoreBundleType.getExecutablePath(sdk));
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
