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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.dnx.bundle.KRuntimeBundleType;
import consulo.dnx.module.extension.KRuntimeModuleExtension;
import com.intellij.compiler.options.CompileStepBeforeRun;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;

/**
 * @author VISTALL
 * @since 23.02.2015
 */
public class KRuntimeRunConfiguration extends ModuleBasedConfiguration<RunConfigurationModule> implements CompileStepBeforeRun.Suppressor
{
	private String myCommand;

	public KRuntimeRunConfiguration(String name, RunConfigurationModule configurationModule, ConfigurationFactory factory)
	{
		super(name, configurationModule, factory);
	}

	public KRuntimeRunConfiguration(RunConfigurationModule configurationModule, ConfigurationFactory factory)
	{
		super(configurationModule, factory);
	}

	@Override
	public Collection<Module> getValidModules()
	{
		List<Module> list = new ArrayList<Module>();
		for(Module module : ModuleManager.getInstance(getProject()).getModules())
		{
			if(ModuleUtilCore.getExtension(module, KRuntimeModuleExtension.class) != null)
			{
				list.add(module);
			}
		}
		return list;
	}

	@NotNull
	@Override
	public SettingsEditor<? extends RunConfiguration> getConfigurationEditor()
	{
		return new KRuntimeRunConfigurationEditor(getProject());
	}

	@Nullable
	@Override
	public RunProfileState getState(@NotNull Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException
	{
		return new CommandLineState(env)
		{
			@NotNull
			@Override
			protected ProcessHandler startProcess() throws ExecutionException
			{
				KRuntimeRunConfiguration runProfile = (KRuntimeRunConfiguration) env.getRunProfile();
				Module module = runProfile.getConfigurationModule().getModule();
				if(module == null)
				{
					throw new ExecutionException("Invalid Module");
				}

				KRuntimeModuleExtension extension = ModuleUtilCore.getExtension(module, KRuntimeModuleExtension.class);
				if(extension == null)
				{
					throw new ExecutionException("No DNX extension");
				}

				Sdk sdk = extension.getSdk();
				if(sdk == null)
				{
					throw new ExecutionException("SDK is null");
				}
				String command = runProfile.getCommand();
				if(command == null)
				{
					throw new ExecutionException("Command is not set");
				}
				File kFile = KRuntimeBundleType.getKFile(sdk.getHomePath());
				if(kFile == null)
				{
					throw new ExecutionException("OS is not supported");
				}
				GeneralCommandLine generalCommandLine = new GeneralCommandLine();
				generalCommandLine.setExePath(kFile.getPath());
				generalCommandLine.addParameter(command);
				generalCommandLine.setWorkDirectory(module.getModuleDirPath());
				return new OSProcessHandler(generalCommandLine);
			}
		};
	}

	@Override
	public void readExternal(Element element) throws InvalidDataException
	{
		super.readExternal(element);
		readModule(element);
		myCommand = element.getChildText("command");
	}

	@Override
	public void writeExternal(Element element) throws WriteExternalException
	{
		super.writeExternal(element);
		writeModule(element);
		if(myCommand != null)
		{
			element.addContent(new Element("command").setText(myCommand));
		}
	}

	public String getCommand()
	{
		return myCommand;
	}

	public void setCommand(String command)
	{
		myCommand = command;
	}
}
