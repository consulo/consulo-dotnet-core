/*
 * Copyright 2013-2017 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.dnx.csharp.module.extension;

import javax.annotation.Nonnull;
import javax.swing.JComponent;

import javax.annotation.Nullable;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpMutableModuleExtension;
import consulo.roots.ModuleRootLayer;

/**
 * @author VISTALL
 * @since 22.02.2015
 */
public class KRuntimeCSharpMutableModuleExtension extends KRuntimeCSharpModuleExtension implements CSharpMutableModuleExtension<KRuntimeCSharpModuleExtension>
{
	public KRuntimeCSharpMutableModuleExtension(@Nonnull String id, @Nonnull ModuleRootLayer module)
	{
		super(id, module);
	}

	@Nullable
	@Override
	public JComponent createConfigurablePanel(@Nonnull Runnable runnable)
	{
		return null;
	}

	@Nonnull
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
	public boolean isModified(@Nonnull KRuntimeCSharpModuleExtension kRuntimeCSharpModuleExtension)
	{
		return isModifiedImpl(kRuntimeCSharpModuleExtension);
	}
}
