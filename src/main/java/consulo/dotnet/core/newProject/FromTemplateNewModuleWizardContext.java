package consulo.dotnet.core.newProject;

import consulo.content.bundle.Sdk;
import consulo.ide.newModule.NewModuleWizardContextBase;

/**
 * @author VISTALL
 * @since 27/01/2023
 */
public class FromTemplateNewModuleWizardContext extends NewModuleWizardContextBase {
  private Sdk mySelectedSdk;
  private NewProjectItem myNewProjectItem;
  private String mySelectedLanguage;

  public FromTemplateNewModuleWizardContext(boolean isNewProject) {
    super(isNewProject);
  }

  public Sdk getSelectedSdk() {
    return mySelectedSdk;
  }

  public void setSelectedSdk(Sdk selectedSdk) {
    mySelectedSdk = selectedSdk;
  }

  public NewProjectItem getNewProjectItem() {
    return myNewProjectItem;
  }

  public void setNewProjectItem(NewProjectItem newProjectItem) {
    myNewProjectItem = newProjectItem;
  }

  public String getSelectedLanguage() {
    return mySelectedLanguage;
  }

  public void setSelectedLanguage(String selectedLanguage) {
    mySelectedLanguage = selectedLanguage;
  }
}
