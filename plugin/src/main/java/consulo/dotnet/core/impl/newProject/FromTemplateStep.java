package consulo.dotnet.core.impl.newProject;

import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTable;
import consulo.disposer.Disposable;
import consulo.dotnet.core.bundle.DotNetCoreBundleType;
import consulo.ide.newModule.ui.UnifiedProjectOrModuleNameStep;
import consulo.localize.LocalizeValue;
import consulo.module.ui.BundleBox;
import consulo.module.ui.BundleBoxBuilder;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.local.ExecUtil;
import consulo.process.local.ProcessOutput;
import consulo.ui.*;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.border.BorderStyle;
import consulo.ui.ex.wizard.WizardStepValidationException;
import consulo.ui.layout.DockLayout;
import consulo.ui.layout.HorizontalLayout;
import consulo.ui.layout.LoadingLayout;
import consulo.ui.layout.ScrollableLayout;
import consulo.ui.style.ComponentColors;
import consulo.ui.util.FormBuilder;
import consulo.ui.util.ShowNotifier;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * @author VISTALL
 * @since 27/01/2023
 */
public class FromTemplateStep extends UnifiedProjectOrModuleNameStep<FromTemplateNewModuleWizardContext> {
  private final SdkTable mySdkTable;

  private BundleBox myBundleBox;

  private String mySelectedSdk;

  private NewProjectItem mySelectedItem;
  private String mySelectedLanguage;

  public FromTemplateStep(FromTemplateNewModuleWizardContext context, SdkTable sdkTable) {
    super(context);
    mySdkTable = sdkTable;
  }

  @RequiredUIAccess
  @Override
  protected void extend(@Nonnull FormBuilder builder, Disposable uiDisposable) {
    BundleBoxBuilder boxBuilder = BundleBoxBuilder.create(uiDisposable);
    boxBuilder.withSdkTypeFilterByType(DotNetCoreBundleType.getInstance());
    myBundleBox = boxBuilder.build();
    if (mySelectedSdk != null) {
      myBundleBox.setSelectedBundle(mySelectedSdk);
    }

    ComboBox<BundleBox.BundleBoxItem> component = myBundleBox.getComponent();
    builder.addLabeled("SDK:", component);

    LoadingLayout<DockLayout> loadingLayout = LoadingLayout.create(DockLayout.create(), uiDisposable);
    builder.setBottom(loadingLayout);

    ShowNotifier.once(myBundleBox.getComponent(), () -> {
      BundleBox.BundleBoxItem value = component.getValue();

      startLoad(value == null ? null : value.getBundle(), loadingLayout);
    });

    component.addValueListener(valueEvent -> startLoad(valueEvent.getValue().getBundle(), loadingLayout));
  }

  @Override
  public void validateStep(@Nonnull FromTemplateNewModuleWizardContext context) throws WizardStepValidationException {
    if (mySelectedItem == null) {
      throw new WizardStepValidationException("Must item selected");
    }
  }

  @RequiredUIAccess
  private void startLoad(Sdk bundle, LoadingLayout<DockLayout> layout) {
    if (bundle == null) {
      return;
    }

    mySelectedItem = null;

    layout.startLoading(() -> {
      return load(bundle);
    }, (dockLayout, newProjectItems) -> {
      ListBox<NewProjectItem> itemListBox = ListBox.create(newProjectItems);

      HorizontalLayout languageGroupLayout = HorizontalLayout.create();

      itemListBox.addValueListener(valueEvent -> {
        mySelectedItem = valueEvent.getValue();

        languageGroupLayout.removeAll();
        languageGroupLayout.setVisible(false);

        if (mySelectedItem != null && !mySelectedItem.languages().isEmpty()) {
          ValueGroup<Boolean> group = ValueGroups.boolGroup();

          languageGroupLayout.add(Label.create(LocalizeValue.localizeTODO("Languages")));

          for (String language : mySelectedItem.languages()) {
            RadioButton button = RadioButton.create(LocalizeValue.of(language));
            button.addValueListener(e -> mySelectedLanguage = language);
            languageGroupLayout.add(button);
            group.add(button);

            if (Objects.equals(language, mySelectedItem.defaultLanguage())) {
              button.setValue(Boolean.TRUE);
            }
          }

          languageGroupLayout.setVisible(true);
        }
      });
      
      itemListBox.setRender((render, i, item) -> {
        render.append(item == null ? "" : item.name());
      });

      ScrollableLayout scrollableLayout = ScrollableLayout.create(itemListBox);
      scrollableLayout.addBorders(BorderStyle.LINE, ComponentColors.BORDER, 1);

      dockLayout.center(scrollableLayout);
      dockLayout.bottom(languageGroupLayout);
    });
  }

  private List<NewProjectItem> load(Sdk bundle) {
    if (bundle == null) {
      return List.of();
    }

    GeneralCommandLine line = new GeneralCommandLine();
    line.setExePath(DotNetCoreBundleType.getExecutablePath(bundle.getHomePath()).getAbsolutePath());
    line.addParameters("new", "list");
    try {
      ProcessOutput processOutput = ExecUtil.execAndGetOutput(line, 5000);

      NewParser newParser = NewParser.parse(processOutput.getStdout());

      List<NewProjectItem> items = new ArrayList<>();
      for (List<String> data : newParser.getData()) {
        String name = data.get(0);
        String shortName = data.get(1);

        Set<String> languages;
        String defaultLanguage = null;
        String languagesStr = data.get(2);
        if (languagesStr.isBlank()) {
          languages = Set.of();
        }
        else {
          languages = new LinkedHashSet<>();
          String[] values = languagesStr.split(",");
          for (String s : values) {
            if (s.isBlank()) {
              continue;
            }

            if (s.indexOf('[') != -1) {
              // [C#] -> C#
              defaultLanguage = s.substring(1, s.length() - 1);
              languages.add(defaultLanguage);
            }
            else {
              languages.add(s);
            }
          }
        }

        items.add(new NewProjectItem(name, shortName, languages, defaultLanguage));
      }

      return items;
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    return List.of();
  }

  @Override
  @RequiredUIAccess
  public void onStepEnter(@Nonnull FromTemplateNewModuleWizardContext context) {
    Sdk sdk = mySdkTable.findMostRecentSdkOfType(DotNetCoreBundleType.getInstance());
    if (sdk != null && myBundleBox == null) {
      mySelectedSdk = sdk.getName();
    }
  }

  @Override
  public void onStepLeave(@Nonnull FromTemplateNewModuleWizardContext context) {
    if (myBundleBox != null) {
      context.setSelectedSdk(mySdkTable.findSdk(myBundleBox.getSelectedBundleName()));
    }

    context.setNewProjectItem(mySelectedItem);
    context.setSelectedLanguage(mySelectedLanguage);
  }
}
