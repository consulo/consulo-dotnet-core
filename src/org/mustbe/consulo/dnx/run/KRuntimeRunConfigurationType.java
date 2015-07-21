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

package org.mustbe.consulo.dnx.run;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dnx.KRuntimeIcons;
import org.mustbe.consulo.dnx.ProjectJsonModel;
import org.mustbe.consulo.dnx.module.extension.KRuntimeModuleExtension;
import org.mustbe.consulo.module.extension.ModuleExtensionHelper;
import com.intellij.execution.configuration.ConfigurationFactoryEx;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import lombok.val;

/**
 * @author VISTALL
 * @since 23.02.2015
 */
public class KRuntimeRunConfigurationType extends ConfigurationTypeBase
{
	public static KRuntimeRunConfigurationType getInstance()
	{
		return CONFIGURATION_TYPE_EP.findExtension(KRuntimeRunConfigurationType.class);
	}

	public KRuntimeRunConfigurationType()
	{
		super("#DNXRunConfigurationType", "DNX", null, KRuntimeIcons.DotnetFoundation);

		addFactory(new ConfigurationFactoryEx(this)
		{
			@Override
			public RunConfiguration createTemplateConfiguration(Project project)
			{
				return new KRuntimeRunConfiguration("Unnamed", new RunConfigurationModule(project), this);
			}

			@Override
			public void onNewConfigurationCreated(@NotNull RunConfiguration configuration)
			{
				KRuntimeRunConfiguration conf = (KRuntimeRunConfiguration) configuration;

				for(val module : ModuleManager.getInstance(configuration.getProject()).getModules())
				{
					KRuntimeModuleExtension extension = ModuleUtilCore.getExtension(module, KRuntimeModuleExtension.class);
					if(extension != null)
					{
						ProjectJsonModel projectJsonModel = extension.getProjectJsonModel();
						if(projectJsonModel == null)
						{
							continue;
						}
						String firstItem = ContainerUtil.getFirstItem(projectJsonModel.commands.keySet());
						if(firstItem == null)
						{
							continue;
						}
						conf.setName(module.getName() + ":" + firstItem);
						conf.setModule(module);
						conf.setCommand(firstItem);
						break;
					}
				}
			}

			@Override
			public boolean isApplicable(@NotNull Project project)
			{
				return ModuleExtensionHelper.getInstance(project).hasModuleExtension(KRuntimeModuleExtension.class);
			}
		});
	}
}
