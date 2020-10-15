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

import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.io.FileUtil;
import consulo.dotnet.externalAttributes.ExternalAttributesRootOrderType;
import consulo.dotnet.icon.DotNetIconGroup;
import consulo.platform.Platform;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 02-Feb-17
 */
public class DotNetCoreBundleType extends SdkType
{
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

	@Nonnull
	public static DotNetCoreBundleType getInstance()
	{
		return EP_NAME.findExtensionOrFail(DotNetCoreBundleType.class);
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
				if(lines.size() == 3)
				{
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
		return ".NET Core " + getVersionString(sdkHome);
	}

	@Nonnull
	@Override
	public Collection<String> suggestHomePaths()
	{
		List<String> list = new ArrayList<>();
		Platform platform = Platform.current();
		if(platform.os().isWindows())
		{
			collectFromProgramFiles(platform, list, "ProgramFiles");
			collectFromProgramFiles(platform, list, "ProgramFiles(x86)");
		}
		return list;
	}

	private void collectFromProgramFiles(Platform platform, List<String> paths, String env)
	{
		String path = platform.os().getEnvironmentVariable(env);
		if(path != null)
		{
			File dotnetSdk = new File(path, "/dotnet/sdk");
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
		return ".NET Core";
	}

	@Nullable
	@Override
	public Image getIcon()
	{
		return DotNetIconGroup.netFoundation();
	}
}
