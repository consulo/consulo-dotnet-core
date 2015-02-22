package org.mustbe.consulo.kruntime.csharp.module.extension;

import javax.swing.JComponent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.csharp.module.extension.CSharpMutableModuleExtension;
import com.intellij.openapi.roots.ModuleRootLayer;

/**
 * @author VISTALL
 * @since 22.02.2015
 */
public class KRuntimeCSharpMutableModuleExtension extends KRuntimeCSharpModuleExtension implements
		CSharpMutableModuleExtension<KRuntimeCSharpModuleExtension>
{
	public KRuntimeCSharpMutableModuleExtension(@NotNull String id, @NotNull ModuleRootLayer module)
	{
		super(id, module);
	}

	@Nullable
	@Override
	public JComponent createConfigurablePanel(@NotNull Runnable runnable)
	{
		return null;
	}

	@NotNull
	@Override
	public CSharpLanguageVersion getLanguageVersion()
	{
		return CSharpLanguageVersion._5_0;
	}

	@Override
	public void setEnabled(boolean b)
	{
		myIsEnabled = b;
	}

	@Override
	public boolean isModified(@NotNull KRuntimeCSharpModuleExtension kRuntimeCSharpModuleExtension)
	{
		return isModifiedImpl(kRuntimeCSharpModuleExtension);
	}
}
