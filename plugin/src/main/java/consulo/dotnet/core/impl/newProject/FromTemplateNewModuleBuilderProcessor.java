package consulo.dotnet.core.impl.newProject;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.Application;
import consulo.application.progress.Task;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTable;
import consulo.dotnet.core.bundle.DotNetCoreBundleType;
import consulo.ide.newModule.NewModuleBuilderProcessor;
import consulo.language.editor.PlatformDataKeys;
import consulo.module.content.layer.ContentEntry;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.msbuild.daemon.impl.MSBuildDaemonService;
import consulo.msbuild.module.extension.MSBuildSolutionMutableModuleExtension;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilder;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.project.Project;
import consulo.project.ui.view.ProjectView;
import consulo.project.ui.view.ProjectViewPane;
import consulo.ui.ex.TreeExpander;
import consulo.ui.ex.wizard.WizardStep;
import consulo.util.dataholder.Key;
import consulo.util.io.FileUtil;
import consulo.virtualFileSystem.LocalFileSystem;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 27/01/2023
 */
public class FromTemplateNewModuleBuilderProcessor implements NewModuleBuilderProcessor<FromTemplateNewModuleWizardContext>
{
	private final SdkTable mySdkTable;

	public FromTemplateNewModuleBuilderProcessor(SdkTable sdkTable)
	{
		mySdkTable = sdkTable;
	}

	@Override
	public void buildSteps(@Nonnull Consumer<WizardStep<FromTemplateNewModuleWizardContext>> consumer,
						   @Nonnull FromTemplateNewModuleWizardContext context)
	{
		consumer.accept(new FromTemplateStep(context, mySdkTable));
	}

	@RequiredReadAction
	@Override
	public void process(@Nonnull FromTemplateNewModuleWizardContext context,
						@Nonnull ContentEntry contentEntry,
						@Nonnull ModifiableRootModel modifiableRootModel)
	{
		Project project = modifiableRootModel.getProject();
		NewProjectItem projectItem = context.getNewProjectItem();
		String selectedLanguage = context.getSelectedLanguage();

		Sdk bundle = context.getSelectedSdk();
		if(bundle == null || projectItem == null)
		{
			return;
		}

		MSBuildSolutionMutableModuleExtension<?> solExtension = modifiableRootModel.getExtensionWithoutCheck("msbuild-dotnet-core");
		assert solExtension != null;
		solExtension.setEnabled(true);
		//solExtension.setProjectFileUrl(project.getBaseDir().getUrl());
		solExtension.setSdkName(bundle.getName());
		solExtension.setProcessProviderId("dotnet-core");

		Application.get().invokeLater(() ->
		{
			MSBuildDaemonService.getInstance(project).externalUpdate(new GenerateProjectStep(project, projectItem, selectedLanguage));
		});
	}

	@Nonnull
	@Override
	public FromTemplateNewModuleWizardContext createContext(boolean b)
	{
		return new FromTemplateNewModuleWizardContext(b);
	}
}
