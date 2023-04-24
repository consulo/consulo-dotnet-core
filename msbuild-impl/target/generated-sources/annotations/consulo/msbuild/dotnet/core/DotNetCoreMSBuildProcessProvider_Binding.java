package consulo.msbuild.dotnet.core;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.component.bind.InjectingBinding;
import consulo.content.bundle.SdkTable;
import consulo.msbuild.MSBuildProcessProvider;
import java.lang.Class;
import java.lang.Object;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.lang.reflect.Type;

@SuppressWarnings("ALL")
public final class DotNetCoreMSBuildProcessProvider_Binding implements InjectingBinding {
  public Class getApiClass() {
    return MSBuildProcessProvider.class;
  }

  public String getApiClassName() {
    return "consulo.msbuild.MSBuildProcessProvider";
  }

  public Class getImplClass() {
    return DotNetCoreMSBuildProcessProvider.class;
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
    return 1;
  }

  public Type[] getParameterTypes() {
    return new Type[] {SdkTable.class};
  }

  public Object create(Object[] args) {
    return new DotNetCoreMSBuildProcessProvider((SdkTable) args[0]);
  }
}
