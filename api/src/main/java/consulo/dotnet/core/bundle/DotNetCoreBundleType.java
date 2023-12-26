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
import consulo.content.OrderRootType;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkType;
import consulo.dotnet.externalAttributes.ExternalAttributesRootOrderType;
import consulo.dotnet.icon.DotNetIconGroup;
import consulo.dotnet.sdk.DotNetSdkType;
import consulo.platform.Platform;
import consulo.platform.PlatformOperatingSystem;
import consulo.ui.image.Image;
import consulo.util.io.FileUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 02-Feb-17
 */
@ExtensionImpl
public class DotNetCoreBundleType extends DotNetSdkType
{
	private static final Supplier<DotNetCoreBundleType> INSTANCE = ExtensionInstance.from(SdkType.class);

	@Nonnull
	public static DotNetCoreBundleType getInstance()
	{
		return INSTANCE.get();
	}

	@Nonnull
	public static String getExecutable()
	{
		if(Platform.current().os().isWindows())
		{
			return "dotnet.exe";
		}
		return "dotnet";
	}

	@Nonnull
	public static File getExecutablePath(@Nonnull String sdkHome)
	{
		return new File(new File(sdkHome, "./../../"), getExecutable());
	}

	public DotNetCoreBundleType()
	{
		super("DOTNET_CORE_SDK");
	}

	@Override
	public boolean isValidSdkHome(String path)
	{
		return getExecutablePath(path).exists() && getVersionString(path) != null;
	}

	@Nullable
	@Override
	public String getVersionString(String sdkHome)
	{
		File versionFile = new File(sdkHome, ".version");
		if(versionFile.exists())
		{
			try
			{
				List<String> lines = FileUtil.loadLines(versionFile);
				if(lines.size() >= 3)
				{
					// new lines after net7
					//4bbdd14480a177e60fba52abf34829020449e46e
					//7.0.102
					//win-x64
					//7.0.102-servicing.22607.3
					return lines.get(1);
				}
			}
			catch(IOException ignored)
			{
			}
		}
		return null;
	}

	@Override
	public boolean isRootTypeApplicable(OrderRootType type)
	{
		return type == ExternalAttributesRootOrderType.getInstance();
	}

	@Override
	public String suggestSdkName(String currentSdkName, String sdkHome)
	{
		return ".NET " + getVersionString(sdkHome);
	}

	@Nonnull
	@Override
	public Collection<String> suggestHomePaths()
	{
		List<String> result = new ArrayList<>();
		Platform platform = Platform.current();
		PlatformOperatingSystem os = platform.os();
		if(os.isWindows())
		{
			collectFromProgramFiles(platform, result, "ProgramFiles");
			collectFromProgramFiles(platform, result, "ProgramFiles(x86)");
		}
		else if(os.isMac())
		{
			collectSdkPaths(new File("/usr/local/share/dotnet/sdk"), result);
		}
		else if(os.isLinux())
		{
			collectSdkPaths(new File("/usr/share/dotnet/sdk/"), result);
		}
		return result;
	}

	private void collectFromProgramFiles(Platform platform, List<String> paths, String env)
	{
		String path = platform.os().getEnvironmentVariable(env);
		if(path != null)
		{
			collectSdkPaths(new File(path, "/dotnet/sdk"), paths);
		}
	}

	private void collectSdkPaths(File dotnetSdk, List<String> paths)
	{
		if(dotnetSdk.exists())
		{
			File[] list = dotnetSdk.listFiles();
			if(list != null)
			{
				for(File file : list)
				{
					paths.add(file.getPath());
				}
			}
		}
	}

	@Override
	public boolean canCreatePredefinedSdks()
	{
		return true;
	}

	@Nonnull
	@Override
	public String getPresentableName()
	{
		return ".NET (.NET Core)";
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return DotNetIconGroup.netfoundation();
	}

	@Nonnull
	@Override
	public File getLoaderFile(@Nonnull Sdk sdk)
	{
		throw new UnsupportedOperationException();
	}
}
