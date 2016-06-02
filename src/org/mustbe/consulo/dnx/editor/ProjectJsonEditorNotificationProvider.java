package org.mustbe.consulo.dnx.editor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dnx.module.extension.KRuntimeModuleExtension;
import org.mustbe.consulo.editor.notifications.EditorNotificationProvider;
import org.mustbe.consulo.json.JsonFileType;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;

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

	@NotNull
	@Override
	public Key<EditorNotificationPanel> getKey()
	{
		return KEY;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public EditorNotificationPanel createNotificationPanel(@NotNull VirtualFile file, @NotNull FileEditor fileEditor)
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
