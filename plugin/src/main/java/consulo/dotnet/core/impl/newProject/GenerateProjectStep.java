package consulo.dotnet.core.impl.newProject;

import consulo.application.ReadAction;
import consulo.application.WriteAction;
import consulo.build.ui.event.MessageEvent;
import consulo.content.bundle.Sdk;
import consulo.dotnet.core.bundle.DotNetCoreBundleType;
import consulo.fileEditor.FileEditorManager;
import consulo.localize.LocalizeValue;
import consulo.module.ModifiableModuleModel;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.ModuleWithNameAlreadyExistsException;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.msbuild.MSBuildProjectFile;
import consulo.msbuild.daemon.impl.MSBuildDaemonContext;
import consulo.msbuild.daemon.impl.MSBuildDaemonService;
import consulo.msbuild.daemon.impl.logging.MSBuildLoggingSession;
import consulo.msbuild.daemon.impl.message.DaemonConnection;
import consulo.msbuild.daemon.impl.step.DaemonStep;
import consulo.msbuild.daemon.impl.step.DaemonStepQueue;
import consulo.msbuild.module.extension.MSBuildSolutionModuleExtension;
import consulo.msbuild.module.extension.MSBuildSolutionMutableModuleExtension;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilder;
import consulo.process.ProcessOutputTypes;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.project.Project;
import consulo.project.ui.view.ProjectView;
import consulo.project.ui.view.ProjectViewPane;
import consulo.util.dataholder.Key;
import consulo.util.io.FileUtil;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author VISTALL
 * @since 24/04/2023
 */
public class GenerateProjectStep implements DaemonStep
{
	private final Project myProject;
	private final NewProjectItem myNewProjectItem;
	private final String mySelectedLanguage;

	public GenerateProjectStep(Project project, NewProjectItem projectItem, String selectedLanguage)
	{
		myProject = project;
		myNewProjectItem = projectItem;
		mySelectedLanguage = selectedLanguage;
	}

	@Nonnull
	@Override
	public LocalizeValue getStepText()
	{
		return LocalizeValue.localizeTODO("Generate Project...");
	}

	@Override
	public void execute(@Nonnull MSBuildDaemonContext context, @Nonnull DaemonConnection connection, @Nullable MSBuildLoggingSession loggingSession) throws IOException
	{
		Sdk sdk = context.getMSBuildSdk();

		GeneralCommandLine line = new GeneralCommandLine();
		line.setExePath(DotNetCoreBundleType.getExecutablePath(sdk.getHomePath()).getAbsolutePath());
		line.addParameter("new");
		line.addParameter(myNewProjectItem.shortName());
		if(mySelectedLanguage != null)
		{
			line.addParameter("-lang");
			line.addParameter(mySelectedLanguage);
		}
		line.addParameter("--use-program-main");
		line.addParameter("-o");
		line.addParameter(FileUtil.toSystemDependentName(myProject.getBasePath()));
		line.addParameter("-n");
		line.addParameter(myProject.getName());

		try
		{
			ProcessHandler handler = ProcessHandlerBuilder.create(line).build();
			handler.addProcessListener(new ProcessListener()
			{
				@Override
				public void onTextAvailable(ProcessEvent event, Key outputType)
				{
					boolean isError = outputType == ProcessOutputTypes.STDERR;
					loggingSession.acceptMessage(event.getText(), isError ? MessageEvent.Kind.ERROR : MessageEvent.Kind.INFO);
				}
			});
			handler.startNotify();
			handler.waitFor();
		}
		catch(ExecutionException e)
		{
			throw new IOException(e);
		}

		LocalFileSystem.getInstance().refreshFiles(List.of(myProject.getBaseDir()), false, true, () ->
		{
			Set<String> projectExtensions = MSBuildProjectFile.listAll(myProject.getApplication());

			VirtualFile projectFile = null;
			for(VirtualFile file : myProject.getBaseDir().getChildren())
			{
				String extension = file.getExtension();
				if(projectExtensions.contains(extension))
				{
					projectFile = file;
					break;
				}
			}

			if(projectFile == null)
			{
				return;
			}

			MSBuildSolutionModuleExtension<?> extension = Objects.requireNonNull(MSBuildSolutionModuleExtension.getSolutionModuleExtension(myProject));

			ModifiableModuleModel moduleModel = ReadAction.compute(() -> ModuleManager.getInstance(myProject).getModifiableModel());

			// first of all we need rename root module since we can made name conflict
			try
			{
				Module rootModule = Objects.requireNonNull(moduleModel.findModuleByName(extension.getModule().getName()));
				// see SolutionModuleImportProvider.createModuleWithSingleContent()
				moduleModel.renameModule(rootModule, rootModule.getName() + " (Root)");
			}
			catch(ModuleWithNameAlreadyExistsException ignored)
			{
			}

			WriteAction.runAndWait(moduleModel::commit);

			// now we need set correct link to project file

			ModifiableRootModel modifiableRootModel = ReadAction.compute(() -> ModuleRootManager.getInstance(extension.getModule()).getModifiableModel());

			MSBuildSolutionMutableModuleExtension modifiableExtension = (MSBuildSolutionMutableModuleExtension) modifiableRootModel.getExtension(MSBuildSolutionModuleExtension.class);

			modifiableExtension.setProjectFileUrl(projectFile.getUrl());

			WriteAction.runAndWait(modifiableRootModel::commit);

			myProject.getApplication().invokeLater(() ->
			{
				ProjectViewPane currentProjectViewPane = ProjectView.getInstance(myProject).getCurrentProjectViewPane();

				for(VirtualFile virtualFile : myProject.getBaseDir().getChildren())
				{
					String name = virtualFile.getNameWithoutExtension();
					if(name.equals("Program"))
					{
						currentProjectViewPane.select(null, virtualFile, true);

						FileEditorManager.getInstance(myProject).openFile(virtualFile, true);
						break;
					}
				}
			});
		});
	}

	@Override
	public void refill(@Nonnull MSBuildDaemonService service, @Nonnull DaemonStepQueue queue)
	{
		service.fillDefaultSteps(queue);
	}

	@Override
	public boolean wantLogging()
	{
		return true;
	}
}
