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

package consulo.dnx.module.extension;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import com.intellij.util.SystemProperties;
import consulo.annotation.access.RequiredReadAction;
import consulo.dnx.jom.ProjectElement;
import consulo.nuget.module.extension.NuGetBasedRepositoryWorker;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * @author VISTALL
 * @since 22.02.2015
 */
public class KRuntimeNuGetWorker extends NuGetBasedRepositoryWorker
{
	private final KRuntimeModuleExtension myExtension;

	public KRuntimeNuGetWorker(KRuntimeModuleExtension extension)
	{
		super(extension.getModule());
		myExtension = extension;
	}

	@Nullable
	@Override
	protected String getPackagesDirPath()
	{
		return SystemProperties.getUserHome() + File.separator + ".kpm" + File.separator + PACKAGES_DIR;
	}

	@Override
	protected void removeInvalidDependenciesFromFileSystem(Map<String, PackageInfo> packages, ProgressIndicator indicator)
	{
	}

	@RequiredReadAction
	@Override
	protected void loadDefinedPackages(@Nonnull Consumer<PackageInfo> packageInfoConsumer)
	{
		ProjectElement projectJsonModel = myExtension.getProjectElement();
		if(projectJsonModel == null)
		{
			return;
		}

		Set<String> frameworksAsSet = projectJsonModel.getFrameworks().keySet();
		if(frameworksAsSet.isEmpty())
		{
			return;
		}

		String[] frameworks = ArrayUtil.toStringArray(frameworksAsSet);

		for(Map.Entry<String, String> entry : projectJsonModel.getDependencies().entrySet())
		{
			String idValue = entry.getKey();
			String versionValue = entry.getValue();
			if(idValue == null || versionValue == null)
			{
				continue;
			}

			packageInfoConsumer.consume(new PackageInfo(idValue, versionValue, frameworks));
		}
	}

	@Nonnull
	@Override
	public String getNameAndVersionSeparator()
	{
		return "/";
	}
}
