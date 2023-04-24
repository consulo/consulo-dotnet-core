package consulo.msbuild.dotnet.core.module.extension;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.component.bind.InjectingBinding;
import consulo.module.content.layer.ModuleExtensionProvider;
import java.lang.Class;
import java.lang.Object;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.lang.reflect.Type;

@SuppressWarnings("ALL")
public final class MSBuildDotNetCoreModuleExtensionProvider_Binding implements InjectingBinding {
  public Class getApiClass() {
    return ModuleExtensionProvider.class;
  }

  public String getApiClassName() {
    return "consulo.module.content.layer.ModuleExtensionProvider";
  }

  public Class getImplClass() {
    return MSBuildDotNetCoreModuleExtensionProvider.class;
  }

  public Class getComponentAnnotationClass() {
    return ExtensionAPI.class;
  }

  public ComponentScope getComponentScope() {
    return ComponentScope.APPLICATION;
  }

  public int getComponentProfiles() {
    return 0;
  }

  public int getParametersCount() {
    return 0;
  }

  public Type[] getParameterTypes() {
    return EMPTY_TYPES;
  }

  public Object create(Object[] args) {
    return new MSBuildDotNetCoreModuleExtensionProvider();
  }
}
