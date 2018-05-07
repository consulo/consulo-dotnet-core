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

import java.io.File;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.ArrayUtil;
import consulo.dnx.bundle.KRuntimeBundleType;
import consulo.dnx.jom.ProjectElement;
import consulo.dnx.util.KRuntimeUtil;
import consulo.dotnet.module.extension.BaseDotNetSimpleModuleExtension;
import consulo.json.jom.JomFileElement;
import consulo.json.jom.JomManager;
import consulo.roots.ModuleRootLayer;

/**
 * @author VISTALL
 * @since 22.02.2015
 */
public class KRuntimeModuleExtension extends BaseDotNetSimpleModuleExtension<KRuntimeModuleExtension>
{
	private NotNullLazyValue<KRuntimeNuGetWorker> myWorkerValue = NotNullLazyValue.createValue(() -> new KRuntimeNuGetWorker(KRuntimeModuleExtension.this));
	public static final String PROJECT_JSON = "project.json";

	public KRuntimeModuleExtension(@Nonnull String id, @Nonnull ModuleRootLayer rootModel)
	{
		super(id, rootModel);
	}

	@Nonnull
	public KRuntimeNuGetWorker getWorker()
	{
		return myWorkerValue.getValue();
	}

	@Nullable
	@consulo.annotations.RequiredReadAction
	public ProjectElement getProjectElement()
	{
		VirtualFile moduleDir = getModule().getModuleDir();
		if(moduleDir == null)
		{
			return null;
		}
		VirtualFile child = moduleDir.findChild(PROJECT_JSON);
		if(child == null)
		{
			return null;
		}
		PsiFile file = PsiManager.getInstance(getProject()).findFile(child);
		if(file == null)
		{
			return null;
		}
		JomFileElement<ProjectElement> fileElement = JomManager.getInstance(getProject()).getFileElement(file);
		if(fileElement == null)
		{
			return null;
		}
		return fileElement.getRootElement();
	}

	@Nonnull
	@Override
	public File[] getFilesForLibraries()
	{
		List<String> pathsForLibraries = getPathsForLibraries(getSdk());

		File[] array = EMPTY_FILE_ARRAY;
		for(String pathsForLibrary : pathsForLibraries)
		{
			File dir = new File(pathsForLibrary);
			if(dir.exists())
			{
				File[] files = dir.listFiles();
				if(files != null)
				{
					array = ArrayUtil.mergeArrays(array, files);
				}
			}
		}
		return array;
	}

	@Nonnull
	private List<String> getPathsForLibraries(@Nullable Sdk sdk)
	{
		String homePath = sdk == null ? null : sdk.getHomePath();

		KRuntimeBundleType.RuntimeType runtimeType = KRuntimeBundleType.getRuntimeType(sdk);
		switch(runtimeType)
		{
			case CLR:
			case Mono:
				// on clr and mono we need search active
				String activeRuntimePath = KRuntimeUtil.getActiveRuntimePath();
				if(activeRuntimePath != null)
				{
					return Collections.singletonList(activeRuntimePath);
				}
				break;
			case CoreCLR:
				if(homePath != null)
				{
					return Collections.singletonList(homePath + "/bin");
				}
				break;
		}
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public Class<? extends SdkType> getSdkTypeClass()
	{
		return KRuntimeBundleType.class;
	}
}
