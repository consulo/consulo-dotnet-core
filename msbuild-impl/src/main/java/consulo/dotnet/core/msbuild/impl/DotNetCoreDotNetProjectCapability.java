package consulo.dotnet.core.msbuild.impl;

import consulo.annotation.component.ExtensionImpl;
import consulo.content.bundle.Sdk;
import consulo.dotnet.DotNetTarget;
import consulo.dotnet.module.extension.DotNetMutableModuleExtension;
import consulo.msbuild.MSBuildProcessProvider;
import consulo.dotnet.core.msbuild.impl.extension.MSBuildDotNetCoreMutableModuleExtension;
import consulo.msbuild.dotnet.impl.BaseDotNetProjectCapability;

import jakarta.annotation.Nonnull;
import java.util.Map;

/**
 * @author VISTALL
 * @since 18/01/2021
 */
@ExtensionImpl
public class DotNetCoreDotNetProjectCapability extends BaseDotNetProjectCapability
{
	@Override
	public boolean isApplicable(@Nonnull MSBuildProcessProvider provider)
	{
		return provider instanceof DotNetCoreMSBuildProcessProvider;
	}

	@Nonnull
	@Override
	protected Class<? extends DotNetMutableModuleExtension> getMutableExtensionClass()
	{
		return MSBuildDotNetCoreMutableModuleExtension.class;
	}

	@Override
	protected void postInitialize(DotNetMutableModuleExtension<?> extension,
								  Map<String, String> properties,
								  MSBuildProcessProvider buildProcessProvider,
								  Sdk msBuildSdk)
	{
		extension.getInheritableSdk().set(null, msBuildSdk);

		String assemblyName = properties.get("AssemblyName");

		if(extension.getTarget() == DotNetTarget.EXECUTABLE)
		{
			// FIXME [VISTALL] we always run dll
			extension.setFileName(assemblyName + "." + "dll");
		}
	}
}
