package org.mustbe.consulo.kruntime.module.extension;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.kruntime.ProjectJsonModel;
import org.mustbe.consulo.nuget.module.extension.NuGetBasedRepositoryWorker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;

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

	@NotNull
	@Override
	protected Map<String, PackageInfo> getPackagesInfo()
	{
		ProjectJsonModel projectJsonModel = myExtension.getProjectJsonModel();
		if(projectJsonModel == null)
		{
			return Collections.emptyMap();
		}

		Set<String> frameworksAsSet = projectJsonModel.frameworks.keySet();
		if(frameworksAsSet.isEmpty())
		{
			return Collections.emptyMap();
		}

		String[] frameworks = ArrayUtil.toStringArray(frameworksAsSet);

		Map<String, PackageInfo> map = new TreeMap<String, PackageInfo>();
		for(Map.Entry<String, String> entry : projectJsonModel.dependencies.entrySet())
		{
			String idValue = entry.getKey();
			String versionValue = entry.getValue();

			map.put(idValue + "/" + versionValue, new PackageInfo(idValue, versionValue, frameworks));
		}
		return map;
	}
}
