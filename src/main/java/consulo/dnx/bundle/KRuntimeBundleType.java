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

package consulo.dnx.bundle;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.execution.util.ExecUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.SmartList;
import consulo.dnx.KRuntimeIcons;
import consulo.ui.image.Image;

/**
 * @author VISTALL
 * @since 22.02.2015
 */
@Deprecated
public class KRuntimeBundleType extends SdkType
{
	private static final Logger LOGGER = Logger.getInstance(KRuntimeBundleType.class);

	public static enum RuntimeType
	{
		Unknown,
		CLR("dnx.clr.managed.dll"),
		CoreCLR("dnx.coreclr.managed.dll"),
		Mono("dnx.mono.managed.dll");

		private final String[] myFilesToValidate;

		RuntimeType(String... filesToValidate)
		{
			myFilesToValidate = filesToValidate;
		}

		public String[] getFilesToValidate()
		{
			return myFilesToValidate;
		}
	}

	@Nullable
	public static File getKFile(String path)
	{
		String relativePath = null;
		if(SystemInfo.isLinux || SystemInfo.isWindows)
		{
			relativePath = "bin/dnx.exe";
		}
		if(relativePath == null)
		{
			return null;
		}
		return new File(path, relativePath);
	}

	@NotNull
	public static RuntimeType getRuntimeType(@Nullable Sdk sdk)
	{
		if(sdk == null)
		{
			return RuntimeType.Unknown;
		}

		VirtualFile homeDirectory = sdk.getHomeDirectory();
		if(homeDirectory == null)
		{
			return RuntimeType.Unknown;
		}

		VirtualFile binDirectory = homeDirectory.findChild("bin");
		if(binDirectory == null)
		{
			return RuntimeType.Unknown;
		}

		for(RuntimeType runtimeType : RuntimeType.values())
		{
			for(String file : runtimeType.getFilesToValidate())
			{
				VirtualFile child = binDirectory.findChild(file);
				if(child != null)
				{
					return runtimeType;
				}
			}
		}
		return RuntimeType.Unknown;
	}

	public KRuntimeBundleType()
	{
		super("K_RUNTIME_BUNDLE");
	}

	@NotNull
	@Override
	public Collection<String> suggestHomePaths()
	{
		List<String> paths = new SmartList<String>();
		if(SystemInfo.isWindows || SystemInfo.isLinux)
		{
			String userHomePath = System.getProperty("user.home");

			File dir = new File(userHomePath, ".dnx/runtimes");
			if(!dir.exists())
			{
				return Collections.emptyList();
			}

			for(File file : dir.listFiles())
			{
				String path = file.getPath();
				if(isValidSdkHome(path))
				{
					paths.add(path);
				}
			}
		}
		return paths;
	}

	@Override
	public boolean canCreatePredefinedSdks()
	{
		return true;
	}

	@Override
	public boolean isValidSdkHome(String path)
	{
		File kFile = getKFile(path);

		return kFile != null && kFile.exists();
	}

	@Nullable
	@Override
	public String getVersionString(String sdkHome)
	{
		File kFile = getKFile(sdkHome);
		if(kFile == null)
		{
			return null;
		}
		try
		{
			ProcessOutput processOutput = ExecUtil.execAndGetOutput(Arrays.asList(kFile.getPath(), "--version"), null);
			List<String> stdoutLines = processOutput.getStdoutLines();
			for(String stdoutLine : stdoutLines)
			{
				String prefix = "Version:";
				stdoutLine = stdoutLine.trim();
				if(stdoutLine.startsWith(prefix))
				{
					return stdoutLine.substring(prefix.length(), stdoutLine.length()).trim();
				}
			}
		}
		catch(ExecutionException e)
		{
			KRuntimeBundleType.LOGGER.warn(e);
		}
		return null;
	}

	@Override
	public String suggestSdkName(String currentSdkName, String sdkHome)
	{
		return new File(sdkHome).getName();
	}

	@NotNull
	@Override
	public String getPresentableName()
	{
		return "DNX";
	}

	@Nullable
	@Override
	public Image getIcon()
	{
		return KRuntimeIcons.DotnetFoundation;
	}
}
