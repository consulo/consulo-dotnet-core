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

package consulo.dnx.editor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import consulo.annotations.RequiredReadAction;
import consulo.dnx.module.extension.KRuntimeModuleExtension;
import consulo.editor.notifications.EditorNotificationProvider;
import consulo.json.JsonFileType;

/**
 * @author VISTALL
 * @since 22.02.2015
 */
public class ProjectJsonEditorNotificationProvider implements EditorNotificationProvider<EditorNotificationPanel>
{
	public static final Key<EditorNotificationPanel> KEY = Key.create("ProjectJsonEditorNotificationProvider");

	private final Project myProject;

	public ProjectJsonEditorNotificationProvider(Project project)
	{
		myProject = project;
	}

	@Nonnull
	@Override
	public Key<EditorNotificationPanel> getKey()
	{
		return KEY;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public EditorNotificationPanel createNotificationPanel(@Nonnull VirtualFile file, @Nonnull FileEditor fileEditor)
	{
		if(file.getFileType() != JsonFileType.INSTANCE)
		{
			return null;
		}

		if(!KRuntimeModuleExtension.PROJECT_JSON.equalsIgnoreCase(file.getName()))
		{
			return null;
		}

		Module moduleForFile = ModuleUtilCore.findModuleForFile(file, myProject);
		if(moduleForFile == null)
		{
			return null;
		}

		final KRuntimeModuleExtension extension = ModuleUtilCore.getExtension(moduleForFile, KRuntimeModuleExtension.class);
		if(extension == null)
		{
			return null;
		}

		VirtualFile moduleDir = moduleForFile.getModuleDir();
		if(moduleDir == null)
		{
			return null;
		}
		VirtualFile child = moduleDir.findChild(KRuntimeModuleExtension.PROJECT_JSON);
		if(!file.equals(child))
		{
			return null;
		}

		EditorNotificationPanel panel = new EditorNotificationPanel();
		panel.setText("DNX Project File");
		panel.createActionLabel("Update Dependencies", new Runnable()
		{
			@Override
			public void run()
			{
				extension.getWorker().forceUpdate();
			}
		});
		return panel;
	}
}
