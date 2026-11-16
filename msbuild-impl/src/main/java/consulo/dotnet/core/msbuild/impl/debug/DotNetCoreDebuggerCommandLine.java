package consulo.dotnet.core.msbuild.impl.debug;

import consulo.application.platform.PlatformTransfer;
import consulo.container.plugin.PluginManager;
import consulo.content.bundle.Sdk;
import consulo.dotnet.core.bundle.DotNetCoreBundleType;
import consulo.dotnet.util.DebugConnectionInfo;
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
 * Command line which runs an assembly with the dotnet executable of a .NET Core bundle, or debugs it with the bundled netcoredbg.
 * Shared by every module extension which runs its output with a .NET Core bundle (MSBuild, Bazel).
 *
 * @author VISTALL
 * @since 2026-11-16
 */
public final class DotNetCoreDebuggerCommandLine {
    private DotNetCoreDebuggerCommandLine() {
    }

    @Nonnull
    public static GeneralCommandLine create(@Nonnull Sdk sdk,
                                            @Nonnull String file,
                                            @Nullable DebugConnectionInfo debugConnectionInfo) throws ExecutionException {
        // the program runs on the machine of the bundle
        GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.withPlatform(sdk.getPlatform());

        String dotNetExePath = DotNetCoreBundleType.getExecutablePath(sdk).toString();

        if (debugConnectionInfo != null) {
            // the debugger of the machine of the bundle - bundled for its system, copied there
            Platform platform = sdk.getPlatform();
            PlatformOperatingSystem os = platform.os();

            String exePath = os.isWindows() ? "netcoredbg.exe" : "netcoredbg";
            String dirPath = os.fileNamePrefix() + "-netcoredbg" + platform.os().arch().fileNameSuffix();

            Path localDir = PluginManager.getPluginPath(DotNetCoreDebuggerCommandLine.class).toPath().resolve(dirPath);
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
        }
        else {
            commandLine.withExecutablePath(DotNetCoreBundleType.getExecutablePath(sdk));
            commandLine.addParameter(file);
        }

        return commandLine;
    }
}
