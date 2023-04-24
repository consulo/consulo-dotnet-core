package consulo.msbuild.dotnet.core.module.extension;

import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkType;
import consulo.dotnet.compiler.DotNetMacroUtil;
import consulo.dotnet.core.bundle.DotNetCoreBundleType;
import consulo.dotnet.debugger.impl.DotNetDebugProcessBase;
import consulo.dotnet.util.DebugConnectionInfo;
import consulo.execution.configuration.RunProfile;
import consulo.execution.debug.XDebugSession;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.msbuild.dotnet.impl.module.extension.MSBuildBaseDotNetModuleExtension;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * @author VISTALL
 * @since 18/01/2021
 */
public class MSBuildDotNetCoreModuleExtension extends MSBuildBaseDotNetModuleExtension<MSBuildDotNetCoreModuleExtension>
{
	public MSBuildDotNetCoreModuleExtension(@Nonnull String id, @Nonnull ModuleRootLayer moduleRootLayer)
	{
		super(id, moduleRootLayer);
	}

	@Nonnull
	@Override
	public Class<? extends SdkType> getSdkTypeClass()
	{
		return DotNetCoreBundleType.class;
	}

	@Nonnull
	@Override
	public DotNetDebugProcessBase createDebuggerProcess(@Nonnull XDebugSession xDebugSession,
														@Nonnull RunProfile runProfile,
														@Nonnull DebugConnectionInfo debugConnectionInfo)
	{
		throw new UnsupportedOperationException();
	}

	@Nonnull
	@Override
	public GeneralCommandLine createDefaultCommandLine(@Nonnull Sdk sdk,
													   @Nullable DebugConnectionInfo debugConnectionInfo) throws ExecutionException
	{
		if(debugConnectionInfo != null)
		{
			throw new ExecutionException("Debug not supported");
		}

		GeneralCommandLine commandLine = new GeneralCommandLine();
		try
		{
			commandLine.setExePath(DotNetCoreBundleType.getExecutablePath(sdk.getHomePath()).getCanonicalPath());
			String file = DotNetMacroUtil.expandOutputFile(this);
			commandLine.addParameter(file);
		}
		catch(IOException e)
		{
			throw new ExecutionException(e);
		}
		return commandLine;
	}

	@Nonnull
	@Override
	public String getDebugFileExtension()
	{
		return "pdb";
	}
}
