package consulo.msbuild.dotnet.core;

import consulo.annotation.component.ExtensionImpl;
import consulo.msbuild.MSBuildProcessProvider;
import consulo.msbuild.csharp.BaseCSharpProjectCapability;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 21/01/2021
 */
@ExtensionImpl
public class DotNetCoreCSharpProjectCapability extends BaseCSharpProjectCapability
{
	@Override
	public boolean isApplicable(@Nonnull MSBuildProcessProvider provider)
	{
		return provider instanceof DotNetCoreMSBuildProcessProvider;
	}

	@Nonnull
	@Override
	public String getExtensionId()
	{
		return "dotnet-core-csharp-by-msbuild";
	}
}
