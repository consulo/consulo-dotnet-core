package consulo.dotnet.core.newProject;

import consulo.annotation.component.ExtensionImpl;
import consulo.content.bundle.SdkTable;
import consulo.ide.newModule.NewModuleBuilder;
import consulo.ide.newModule.NewModuleContext;
import consulo.ide.newModule.NewModuleContextGroup;
import consulo.localize.LocalizeValue;
import consulo.platform.base.icon.PlatformIconGroup;
import jakarta.inject.Inject;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 27/01/2023
 */
@ExtensionImpl(id = "dotnet-core")
public class FromTemplateNewModuleBuilder implements NewModuleBuilder {
  private final SdkTable mySdkTable;

  @Inject
  public FromTemplateNewModuleBuilder(SdkTable sdkTable) {
    mySdkTable = sdkTable;
  }

  @Override
  public void setupContext(@Nonnull NewModuleContext newModuleContext) {
    NewModuleContextGroup group = newModuleContext.addGroup("dotnet-core", LocalizeValue.localizeTODO(".NET"));

    group.add(LocalizeValue.localizeTODO("From Template"),
              PlatformIconGroup.actionsGroupby(),
              0, new FromTemplateNewModuleBuilderProcessor(mySdkTable));
  }
}
