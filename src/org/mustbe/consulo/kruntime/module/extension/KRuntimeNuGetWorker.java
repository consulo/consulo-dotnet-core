package org.mustbe.consulo.kruntime.module.extension;

import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.kruntime.ProjectJsonModel;
import org.mustbe.consulo.nuget.module.extension.NuGetBasedRepositoryWorker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;

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

	//TODO [VISTALL] map to user.home/.kpm
	@Nullable
	@Override
	protected String getPackagesDirPath()
	{
		VirtualFile moduleDir = myModule.getModuleDir();
		if(moduleDir == null)
		{
			return null;
		}
		return moduleDir.getPath() + "/" + PACKAGES_DIR;
	}

	@Override
	protected void loadDefinedPackages(@NotNull Consumer<PackageInfo> packageInfoConsumer)
	{
		ProjectJsonModel projectJsonModel = myExtension.getProjectJsonModel();
		if(projectJsonModel == null)
		{
			return;
		}

		Set<String> frameworksAsSet = projectJsonModel.frameworks.keySet();
		if(frameworksAsSet.isEmpty())
		{
			return;
		}

		String[] frameworks = ArrayUtil.toStringArray(frameworksAsSet);

		for(Map.Entry<String, String> entry : projectJsonModel.dependencies.entrySet())
		{
			String idValue = entry.getKey();
			String versionValue = entry.getValue();

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
