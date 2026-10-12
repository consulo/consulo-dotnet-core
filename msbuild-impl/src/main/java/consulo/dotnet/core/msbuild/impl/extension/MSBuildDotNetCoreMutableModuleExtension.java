package consulo.dotnet.core.msbuild.impl.extension;

import consulo.content.bundle.Sdk;
import consulo.disposer.Disposable;
import consulo.dotnet.module.extension.DotNetConfigurationPanel;
import consulo.dotnet.module.extension.DotNetMutableModuleExtension;
import consulo.dotnet.psi.impl.module.extension.DotNetQualifiedElementQualifierProducer;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.MutableModuleInheritableNamedPointer;
import consulo.msbuild.module.extension.MSBuildProjectMutableModuleExtension;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 18/01/2021
 */
public class MSBuildDotNetCoreMutableModuleExtension extends MSBuildDotNetCoreModuleExtension implements DotNetMutableModuleExtension<MSBuildDotNetCoreModuleExtension>,
    MSBuildProjectMutableModuleExtension<MSBuildDotNetCoreModuleExtension> {
    public MSBuildDotNetCoreMutableModuleExtension(@Nonnull String id, @Nonnull ModuleRootLayer moduleRootLayer) {
        super(id, moduleRootLayer);
    }

    @Nonnull
    @Override
    public MutableModuleInheritableNamedPointer<Sdk> getInheritableSdk() {
        return (MutableModuleInheritableNamedPointer<Sdk>) super.getInheritableSdk();
    }

    @RequiredUIAccess
    @Nullable
    @Override
    public Component createConfigurationComponent(@Nonnull Disposable uiDisposable, @Nonnull Runnable updateOnCheck) {
        return DotNetConfigurationPanel.create(this, DotNetQualifiedElementQualifierProducer.INSTANCE, getVariables(), uiDisposable, updateOnCheck);
    }

    @Override
    public void setEnabled(boolean val) {
        myIsEnabled = val;
    }

    @Override
    public boolean isModified(@Nonnull MSBuildDotNetCoreModuleExtension originalExtension) {
        return isModifiedImpl(originalExtension);
    }
}
