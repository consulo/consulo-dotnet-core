package consulo.msbuild.dotnet.core.module.extension;

import consulo.annotation.component.ExtensionImpl;
import consulo.dotnet.icon.DotNetIconGroup;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.msbuild.csharp.module.extension.MSBuildCSharpModuleExtension;
import consulo.msbuild.csharp.module.extension.MSBuildCSharpMutableModuleExtension;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 11/01/2023
 */
@ExtensionImpl
public class MSBuildDotNetCSharpModuleExtensionProvider implements ModuleExtensionProvider<MSBuildCSharpModuleExtension>
{
	@Nonnull
	@Override
	public String getId()
	{
		return "dotnet-core-csharp-by-msbuild";
	}

	@Nullable
	@Override
	public String getParentId()
	{
		return "dotnet-core-by-msbuild";
	}

	@Override
	public boolean isSystemOnly()
	{
		return true;
	}

	@Nonnull
	@Override
	public LocalizeValue getName()
	{
		return LocalizeValue.of("C#");
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return DotNetIconGroup.netfoundation();
	}

	@Nonnull
	@Override
	public ModuleExtension<MSBuildCSharpModuleExtension> createImmutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new MSBuildCSharpModuleExtension(getId(), moduleRootLayer);
	}

	@Nonnull
	@Override
	public MutableModuleExtension<MSBuildCSharpModuleExtension> createMutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new MSBuildCSharpMutableModuleExtension(getId(), moduleRootLayer);
	}
}
