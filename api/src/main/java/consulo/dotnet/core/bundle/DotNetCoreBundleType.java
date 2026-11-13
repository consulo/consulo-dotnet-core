/*
 * Copyright 2013-2017 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.dotnet.core.bundle;

import consulo.annotation.component.ExtensionImpl;
import consulo.component.extension.ExtensionInstance;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkType;
import consulo.dotnet.icon.DotNetIconGroup;
import consulo.dotnet.sdk.DotNetSdkType;
import consulo.localize.LocalizeValue;
import consulo.platform.Platform;
import consulo.platform.PlatformOperatingSystem;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A home of the bundle is a {@code sdk/<version>} directory of a .NET installation of any machine.
 *
 * @author VISTALL
 * @since 02-Feb-17
 */
@ExtensionImpl
public class DotNetCoreBundleType extends DotNetSdkType {
    private static final Supplier<DotNetCoreBundleType> INSTANCE = ExtensionInstance.from(SdkType.class);

    @Nonnull
    public static DotNetCoreBundleType getInstance() {
        return INSTANCE.get();
    }

    @Nonnull
    public static String getExecutable(@Nonnull Platform platform) {
        return platform.os().isWindows() ? "dotnet.exe" : "dotnet";
    }

    /**
     * The {@code dotnet} of the installation of the bundle home.
     */
    @Nonnull
    public static Path getExecutablePath(@Nonnull Platform platform, @Nonnull Path sdkHome) {
        return sdkHome.resolve("../..").normalize().resolve(getExecutable(platform));
    }

    @Nonnull
    public static Path getExecutablePath(@Nonnull Sdk sdk) {
        return getExecutablePath(sdk.getPlatform(), sdk.getHomeNioPath());
    }

    public DotNetCoreBundleType() {
        super("DOTNET_CORE_SDK", LocalizeValue.localizeTODO(".NET (.NET Core)"), DotNetIconGroup.netfoundation());
    }

    @Override
    public boolean isValidSdkHome(@Nonnull Platform platform, @Nonnull Path path) {
        return Files.isRegularFile(getExecutablePath(platform, path)) && getVersionString(platform, path) != null;
    }

    @Nullable
    @Override
    public String getVersionString(@Nonnull Platform platform, @Nonnull Path sdkHome) {
        try {
            List<String> lines = Files.readAllLines(sdkHome.resolve(".version"));
            if (lines.size() >= 3) {
                // new lines after net7
                //4bbdd14480a177e60fba52abf34829020449e46e
                //7.0.102
                //win-x64
                //7.0.102-servicing.22607.3
                return lines.get(1).trim();
            }
        }
        catch (IOException ignored) {
        }
        return null;
    }

    @Override
    public String suggestSdkName(@Nonnull Platform platform, @Nullable String currentSdkName, @Nonnull Path sdkHome) {
        return ".NET " + getVersionString(platform, sdkHome);
    }

    @Override
    public void collectHomePaths(@Nonnull Platform platform, @Nonnull Consumer<Path> consumer) {
        PlatformOperatingSystem os = platform.os();

        // the installations - each has its sdk directory
        Set<Path> roots = new LinkedHashSet<>();
        addRoot(platform, roots, os.getEnvironmentVariable("DOTNET_ROOT"));
        if (os.isWindows()) {
            addRoot(platform, roots, os.getEnvironmentVariable("ProgramFiles"), "dotnet");
            addRoot(platform, roots, os.getEnvironmentVariable("ProgramFiles(x86)"), "dotnet");
        }
        else if (os.isMac()) {
            addRoot(platform, roots, "/usr/local/share/dotnet");
        }
        else {
            addRoot(platform, roots, "/usr/share/dotnet");
            addRoot(platform, roots, "/usr/lib/dotnet");
        }
        addRoot(platform, roots, platform.user().homePath().toString(), ".dotnet");

        // the dotnet of the path - a link of a system installation, or an installation of its own
        String pathVariable = os.getEnvironmentVariable("PATH");
        if (pathVariable != null) {
            for (String entry : StringUtil.split(pathVariable, platform.fs().getPathListSeparator())) {
                Path executable = platform.fs().getPath(entry).resolve(getExecutable(platform));
                if (!Files.isRegularFile(executable)) {
                    continue;
                }
                try {
                    Path parent = executable.toRealPath().getParent();
                    if (parent != null) {
                        roots.add(parent);
                    }
                }
                catch (IOException | UnsupportedOperationException e) {
                    roots.add(executable.getParent());
                }
            }
        }

        for (Path root : roots) {
            try (Stream<Path> children = Files.list(root.resolve("sdk"))) {
                children.filter(Files::isDirectory).forEach(consumer);
            }
            catch (IOException ignored) {
            }
        }
    }

    private static void addRoot(@Nonnull Platform platform, @Nonnull Set<Path> roots, @Nullable String path, @Nonnull String... more) {
        if (!StringUtil.isEmptyOrSpaces(path)) {
            roots.add(platform.fs().getPath(path, more));
        }
    }

    @Override
    public boolean canCreatePredefinedSdks(@Nonnull Platform platform) {
        return true;
    }
}
