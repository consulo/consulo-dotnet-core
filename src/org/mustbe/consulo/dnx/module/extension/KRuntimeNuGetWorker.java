package org.mustbe.consulo.dnx.module.extension;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dnx.jom.ProjectElement;
import org.mustbe.consulo.nuget.module.extension.NuGetBasedRepositoryWorker;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import com.intellij.util.SystemProperties;

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
	protected void loadDefinedPackages(@NotNull Consumer<PackageInfo> packageInfoConsumer)
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

	@NotNull
	@Override
	public String getNameAndVersionSeparator()
	{
		return "/";
	}
}
