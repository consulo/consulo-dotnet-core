package org.mustbe.consulo.kruntime.csharp.module.extension;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.module.extension.BaseCSharpModuleExtension;
import org.mustbe.consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import com.intellij.openapi.roots.ModuleRootLayer;

/**
 * @author VISTALL
 * @since 22.02.2015
 */
public class KRuntimeCSharpModuleExtension extends BaseCSharpModuleExtension<KRuntimeCSharpModuleExtension>
{
	public KRuntimeCSharpModuleExtension(@NotNull String id, @NotNull ModuleRootLayer module)
	{
		super(id, module);
	}

	@NotNull
	@Override
	public DotNetCompilerOptionsBuilder createCompilerOptionsBuilder()
	{
		throw new IllegalArgumentException();
	}
}
