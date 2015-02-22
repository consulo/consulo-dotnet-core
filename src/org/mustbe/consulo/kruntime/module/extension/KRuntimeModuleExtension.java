/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.kruntime.module.extension;

import java.io.IOException;
import java.io.InputStreamReader;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.module.extension.BaseDotNetSimpleModuleExtension;
import org.mustbe.consulo.kruntime.ProjectJsonModel;
import org.mustbe.consulo.kruntime.bundle.KRuntimeBundleType;
import com.google.gson.Gson;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.ModuleRootLayer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;

/**
 * @author VISTALL
 * @since 22.02.2015
 */
public class KRuntimeModuleExtension extends BaseDotNetSimpleModuleExtension<KRuntimeModuleExtension>
{
	public static final String PROJECT_JSON = "project.json";

	public KRuntimeModuleExtension(@NotNull String id, @NotNull ModuleRootLayer rootModel)
	{
		super(id, rootModel);
	}

	@NotNull
	@LazyInstance
	public KRuntimeNuGetWorker getWorker()
	{
		return new KRuntimeNuGetWorker(KRuntimeModuleExtension.this);
	}

	@Nullable
	public ProjectJsonModel getProjectJsonModel()
	{
		return CachedValuesManager.getManager(getProject()).createCachedValue(new CachedValueProvider<ProjectJsonModel>()
		{
			@Nullable
			@Override
			public Result<ProjectJsonModel> compute()
			{
				VirtualFile moduleDir = getModule().getModuleDir();
				if(moduleDir == null)
				{
					return Result.create(null, PsiModificationTracker.MODIFICATION_COUNT);
				}
				VirtualFile child = moduleDir.findChild(PROJECT_JSON);
				if(child == null)
				{
					return Result.create(null, PsiModificationTracker.MODIFICATION_COUNT);
				}
				try
				{
					ProjectJsonModel projectJsonModel = new Gson().fromJson(new InputStreamReader(child.getInputStream()), ProjectJsonModel.class);
					return Result.create(projectJsonModel, PsiModificationTracker.MODIFICATION_COUNT);
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
				return Result.create(null, PsiModificationTracker.MODIFICATION_COUNT);
			}
		}, false).getValue();
	}

	@NotNull
	@Override
	public Class<? extends SdkType> getSdkTypeClass()
	{
		return KRuntimeBundleType.class;
	}
}
