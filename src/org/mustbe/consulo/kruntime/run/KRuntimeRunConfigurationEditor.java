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

package org.mustbe.consulo.kruntime.run;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.kruntime.ProjectJsonModel;
import org.mustbe.consulo.kruntime.module.extension.KRuntimeModuleExtension;
import com.intellij.application.options.ModuleListCellRenderer;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.ColoredListCellRendererWrapper;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ui.FormBuilder;
import lombok.val;

/**
 * @author VISTALL
 * @since 23.02.2015
 */
public class KRuntimeRunConfigurationEditor extends SettingsEditor<KRuntimeRunConfiguration>
{
	private final Project myProject;

	private JComboBox myModuleComboBox;
	private JComboBox myCommandComboBox;

	private ProjectJsonModel myCurrentJsonModel = new ProjectJsonModel();

	public KRuntimeRunConfigurationEditor(Project project)
	{
		myProject = project;
	}

	@Override
	protected void resetEditorFrom(KRuntimeRunConfiguration runConfiguration)
	{
		myModuleComboBox.setSelectedItem(runConfiguration.getConfigurationModule().getModule());
		selectCommand(runConfiguration.getCommand());
	}

	@Override
	protected void applyEditorTo(KRuntimeRunConfiguration runConfiguration) throws ConfigurationException
	{
		runConfiguration.getConfigurationModule().setModule((Module) myModuleComboBox.getSelectedItem());
		runConfiguration.setCommand((String) myCommandComboBox.getSelectedItem());
	}

	@NotNull
	@Override
	protected JComponent createEditor()
	{
		myModuleComboBox = new ComboBox();
		myModuleComboBox.setRenderer(new ModuleListCellRenderer());
		for(val module : ModuleManager.getInstance(myProject).getModules())
		{
			if(ModuleUtilCore.getExtension(module, KRuntimeModuleExtension.class) != null)
			{
				myModuleComboBox.addItem(module);
			}
		}
		myModuleComboBox.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if(e.getStateChange() == ItemEvent.SELECTED)
				{
					refreshCommandBox(true);
				}
			}
		});

		myCommandComboBox = new ComboBox();
		myCommandComboBox.setRenderer(new ColoredListCellRendererWrapper<String>()
		{
			@Override
			protected void doCustomize(JList list, String value, int index, boolean selected, boolean hasFocus)
			{
				if(value == null)
				{
					append("null", SimpleTextAttributes.ERROR_ATTRIBUTES);
					return;
				}
				if(!myCurrentJsonModel.commands.containsKey(value))
				{
					append(value, SimpleTextAttributes.ERROR_ATTRIBUTES);
				}
				else
				{
					append(value);
				}
			}
		});

		refreshCommandBox(false);

		FormBuilder formBuilder = FormBuilder.createFormBuilder();
		formBuilder.addLabeledComponent("Module", myModuleComboBox);
		formBuilder.addLabeledComponent("Command", myCommandComboBox);
		return formBuilder.getPanel();
	}

	private void refreshCommandBox(boolean select)
	{
		String commandSelectedItem = (String) myCommandComboBox.getSelectedItem();

		ProjectJsonModel temp;

		Module selectedItem = (Module) myModuleComboBox.getSelectedItem();
		if(selectedItem == null)
		{
			temp = new ProjectJsonModel();
		}
		else
		{
			KRuntimeModuleExtension extension = ModuleUtilCore.getExtension(selectedItem, KRuntimeModuleExtension.class);
			if(extension != null)
			{
				ProjectJsonModel projectJsonModel = extension.getProjectJsonModel();
				temp = ObjectUtils.notNull(projectJsonModel, new ProjectJsonModel());
			}
			else
			{
				temp = new ProjectJsonModel();
			}
		}

		myCurrentJsonModel = temp;
		myCommandComboBox.removeAllItems();
		Set<String> commands = temp.commands.keySet();
		for(String command : commands)
		{
			myCommandComboBox.addItem(command);
		}

		if(select)
		{
			selectCommand(commandSelectedItem);
		}
	}

	private void selectCommand(String command)
	{
		if(command == null)
		{
			myCommandComboBox.setSelectedItem(null);
		}
		Set<String> commands = myCurrentJsonModel.commands.keySet();
		if(commands.contains(command))
		{
			myCommandComboBox.setSelectedItem(command);
		}
		else
		{
			myCommandComboBox.addItem(command);
		}
	}
}