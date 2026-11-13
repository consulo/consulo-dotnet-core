package consulo.dotnet.core.msbuild.impl;

import consulo.annotation.component.ExtensionImpl;
import consulo.container.plugin.PluginManager;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTable;
import consulo.dotnet.core.bundle.DotNetCoreBundleType;
import consulo.logging.Logger;
import consulo.msbuild.MSBuildProcessProvider;
import consulo.msbuild.importProvider.MSBuildBaseImportContext;
import consulo.msbuild.importProvider.MSBuildModuleImportContext;
import consulo.msbuild.importProvider.SolutionModuleImportContext;
import consulo.msbuild.solution.reader.SlnFile;
import consulo.msbuild.solution.reader.SlnProject;
import consulo.process.cmd.GeneralCommandLine;
import consulo.util.collection.ContainerUtil;
import consulo.util.io.CharsetToolkit;
import consulo.util.io.FileUtil;
import consulo.util.jdom.JDOMUtil;
import consulo.util.lang.StringUtil;
import jakarta.inject.Inject;
import org.jdom.Element;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 10/01/2021
 */
@ExtensionImpl(order = "before DefaultMSBuildProcessProvider")
public class DotNetCoreMSBuildProcessProvider implements MSBuildProcessProvider
{
	private static final Logger LOG = Logger.getInstance(DotNetCoreMSBuildProcessProvider.class);

	private static final String ourRuntimeJsonSuffix = ".runtimeconfig.json";
	private static final String ourDepsJsonSuffix = ".deps.json";

	private final SdkTable mySdkTable;

	@Inject
	public DotNetCoreMSBuildProcessProvider(SdkTable sdkTable)
	{
		mySdkTable = sdkTable;
	}

	@Override
	public Path getBinDir(@Nonnull Sdk sdk)
	{
		return sdk.getHomeNioPath();
	}

	@Nonnull
	@Override
	public String getId()
	{
		return "dotnet-core";
	}

	@Override
	public void fillBundles(@Nonnull Consumer<Sdk> consumer)
	{
		List<Sdk> sdksOfType = mySdkTable.getSdksOfType(DotNetCoreBundleType.getInstance());
		for(Sdk sdk : sdksOfType)
		{
			consumer.accept(sdk);
		}
	}

	@Nullable
	@Override
	public Sdk findBundle(@Nullable String bundleName)
	{
		return mySdkTable.findSdk(bundleName);
	}

	@Nullable
	@Override
	public Sdk findBundleForImport(@Nonnull MSBuildBaseImportContext context)
	{
		if(context instanceof MSBuildModuleImportContext)
		{
			return isSdkProject(new File(context.getFileToImport())) ? findFirstSdk() : null;
		}
		else if(context instanceof SolutionModuleImportContext)
		{
			SlnFile slnFile = ((SolutionModuleImportContext) context).getSlnFile();

			File parentDir = new File(context.getPath());

			for(SlnProject slnProject : slnFile.getProjects())
			{
				File projFile = new File(parentDir, slnProject.FilePath);
				if(projFile.exists() && isSdkProject(projFile))
				{
					return findFirstSdk();
				}
			}
		}
		return null;
	}

	@Nullable
	private Sdk findFirstSdk()
	{
		return ContainerUtil.getFirstItem(mySdkTable.getSdksOfType(DotNetCoreBundleType.getInstance()));
	}

	private boolean isSdkProject(File path)
	{
		try
		{
			try (FileInputStream stream = new FileInputStream(path))
			{
				InputStream noBomStream = CharsetToolkit.inputStreamSkippingBOM(stream);

				Element element = JDOMUtil.load(noBomStream);
				if("Project".equals(element.getName()) && !StringUtil.isEmptyOrSpaces(element.getAttributeValue("Sdk")))
				{
					return true;
				}
			}
		}
		catch(Throwable e)
		{
			LOG.warn(e);
		}
		return false;
	}

	@Nonnull
	@Override
	public GeneralCommandLine buildCommandLine(@Nonnull Sdk msBuildSdk, @Nonnull Path targetFile, int port)
	{
		// the daemon runs on the machine of the bundle
		GeneralCommandLine c = new GeneralCommandLine();
		c.withPlatform(msBuildSdk.getPlatform());
		c.withExecutablePath(DotNetCoreBundleType.getExecutablePath(msBuildSdk));
		c.withWorkingDirectory(msBuildSdk.getHomeNioPath());
		c.addParameter(targetFile.toString());
		c.addParameter(String.valueOf(port));
		c.addParameter("true"); // debug

		// set exe path to original msbuild dir
		c.withEnvironment("MSBUILD_EXE_PATH", msBuildSdk.getHomeNioPath().resolve("MSBuild.dll").toString());

		// the dotnet of the bundle for the tools the build starts (the compiler) - as the dotnet cli sets it, an installation
		// out of the default location (~/.dotnet) is not found otherwise
		Path dotnet = DotNetCoreBundleType.getExecutablePath(msBuildSdk);
		c.withEnvironment("DOTNET_HOST_PATH", dotnet.toString());
		Path dotnetRoot = dotnet.getParent();
		if(dotnetRoot != null)
		{
			c.withEnvironment("DOTNET_ROOT", dotnetRoot.toString());
		}
		//c.withEnvironment("MSBuildSDKsPath", new File(msBuildSdk.getHomePath(), "Sdks").getAbsolutePath());
		return c;
	}

	@Nonnull
	@Override
	public File getTargetFile()
	{
		File net4Dir = new File(PluginManager.getPluginPath(DotNetCoreMSBuildProcessProvider.class), "netcoreapp3.1");

		return new File(net4Dir, "Consulo.MSBuildBuilder.dll");
	}

	@Override
	public void fillGlobalProperties(@Nonnull Sdk msBuildSdk, @Nonnull Map<String, String> properties)
	{
		properties.put("MSBuildExtensionsPath", msBuildSdk.getHomePath());
		properties.put("MSBuildToolsPath32", msBuildSdk.getHomePath());
		properties.put("MSBuildToolsPath64", msBuildSdk.getHomePath());
		properties.put("NuGetRestoreTargets", msBuildSdk.getHomePath() + "/NuGet.targets");
	}

	@Override
	public void doAdditionalCopy(@Nonnull File targetFile, @Nonnull Path msBuildRunnerDir, @Nonnull Sdk msBuildSdk) throws IOException
	{
		String nameWithoutExtension = FileUtil.getNameWithoutExtension(targetFile);

		// both are of the machine of the bundle
		Path msBuildRuntimeJson = msBuildSdk.getHomeNioPath().resolve("MSBuild" + ourRuntimeJsonSuffix);
		if(Files.exists(msBuildRuntimeJson))
		{
			Files.copy(msBuildRuntimeJson, msBuildRunnerDir.resolve(nameWithoutExtension + ourRuntimeJsonSuffix), StandardCopyOption.REPLACE_EXISTING);
		}

		//		String depsFileName = "MSBuild" + ourDepsJsonSuffix;
		//		File msBuildDepsJson = new File(msBuildSdk.getHomePath(), depsFileName);
		//		if(msBuildDepsJson.exists())
		//		{
		//			String jsonText = FileUtil.loadFile(msBuildDepsJson);
		//
		//			String targetFileName = nameWithoutExtension + ourDepsJsonSuffix;
		//
		//			jsonText = jsonText.replace(depsFileName, targetFileName);
		//
		//			Gson gson = new GsonBuilder().setPrettyPrinting().create();
		//			Map map = gson.fromJson(jsonText, Map.class);
		//
		//			Map<String, Map> libraries = (Map) map.get("libraries");
		//
		//			Map<String, Object> libraryInfoProject = new LinkedHashMap<>();
		//			libraryInfoProject.put("type", "project");
		//			libraryInfoProject.put("serviceable", false);
		//			libraryInfoProject.put("sha512", "");
		//
		//			Map<String, Map> newLibraries = new LinkedHashMap<>();
		//			map.put("libraries", newLibraries);
		//
		//			for(String libraryName : libraries.keySet())
		//			{
		//				newLibraries.put(libraryName, libraryInfoProject);
		//			}
		//
		//			String newJson = gson.toJson(map);
		//			FileUtil.writeToFile(new File(msBuildRunnerDir, targetFileName), newJson);
		//		}
	}

	@Override
	public int getVersion()
	{
		return 7;
	}

	@Nonnull
	@Override
	public String getSolutionModuleExtensionId()
	{
		return "msbuild-dotnet-core";
	}
}
