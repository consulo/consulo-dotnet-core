package consulo.msbuild.dotnet.core.module.extension;

import consulo.annotation.component.ExtensionImpl;
import consulo.dotnet.icon.DotNetIconGroup;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 11/01/2023
 */
@ExtensionImpl
public class MSBuildDotNetCoreModuleExtensionProvider implements ModuleExtensionProvider<MSBuildDotNetCoreModuleExtension>
{
	@Nonnull
	@Override
	public String getId()
	{
		return "dotnet-core-by-msbuild";
	}

	@Nonnull
	@Override
	public LocalizeValue getName()
	{
		return LocalizeValue.localizeTODO(".NET/.NET Core (MSBuild)");
	}

	@Override
	public boolean isSystemOnly()
	{
		return true;
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return DotNetIconGroup.netfoundation();
	}

	@Nonnull
	@Override
	public ModuleExtension<MSBuildDotNetCoreModuleExtension> createImmutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new MSBuildDotNetCoreModuleExtension(getId(), moduleRootLayer);
	}

	@Nonnull
	@Override
	public MutableModuleExtension<MSBuildDotNetCoreModuleExtension> createMutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new MSBuildDotNetCoreMutableModuleExtension(getId(), moduleRootLayer);
	}
}
