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

package consulo.dnx.run;

import javax.annotation.Nonnull;

import com.intellij.execution.configuration.ConfigurationFactoryEx;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotations.RequiredDispatchThread;
import consulo.dnx.KRuntimeIcons;
import consulo.dnx.jom.ProjectElement;
import consulo.dnx.module.extension.KRuntimeModuleExtension;
import consulo.module.extension.ModuleExtensionHelper;

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
			@RequiredDispatchThread
			public void onNewConfigurationCreated(@Nonnull RunConfiguration configuration)
			{
				KRuntimeRunConfiguration conf = (KRuntimeRunConfiguration) configuration;

				for(Module module : ModuleManager.getInstance(configuration.getProject()).getModules())
				{
					KRuntimeModuleExtension extension = ModuleUtilCore.getExtension(module, KRuntimeModuleExtension.class);
					if(extension != null)
					{
						ProjectElement projectElement = extension.getProjectElement();
						if(projectElement == null)
						{
							continue;
						}
						String firstItem = ContainerUtil.getFirstItem(projectElement.getCommands().keySet());
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
			public boolean isApplicable(@Nonnull Project project)
			{
				return ModuleExtensionHelper.getInstance(project).hasModuleExtension(KRuntimeModuleExtension.class);
			}
		});
	}
}