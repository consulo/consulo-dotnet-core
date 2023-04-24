package consulo.msbuild.dotnet.core.module.extension;

import consulo.annotation.component.ExtensionImpl;
import consulo.dotnet.icon.DotNetIconGroup;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.msbuild.module.extension.MSBuildSolutionModuleExtensionImpl;
import consulo.msbuild.module.extension.MSBuildSolutionMutableModuleExtensionImpl;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 11/01/2023
 */
@ExtensionImpl
public class MSBuildDotNetSolutionModuleExtensionProviderImpl implements ModuleExtensionProvider<MSBuildSolutionModuleExtensionImpl>
{
	@Nonnull
	@Override
	public String getId()
	{
		return "msbuild-dotnet-core";
	}

	@Nonnull
	@Override
	public LocalizeValue getName()
	{
		return LocalizeValue.localizeTODO("MSBuild Solution (.NET Core)");
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return DotNetIconGroup.netfoundation();
	}

	@Nonnull
	@Override
	public ModuleExtension<MSBuildSolutionModuleExtensionImpl> createImmutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new MSBuildSolutionModuleExtensionImpl(getId(), moduleRootLayer);
	}

	@Nonnull
	@Override
	public MutableModuleExtension<MSBuildSolutionModuleExtensionImpl> createMutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new MSBuildSolutionMutableModuleExtensionImpl(getId(), moduleRootLayer);
	}
}
