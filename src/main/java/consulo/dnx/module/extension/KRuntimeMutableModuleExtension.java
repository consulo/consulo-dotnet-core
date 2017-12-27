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

package consulo.dnx.module.extension;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.util.NullableFunction;
import consulo.dotnet.module.extension.DotNetSimpleMutableModuleExtension;
import consulo.extension.ui.ModuleExtensionSdkBoxBuilder;
import consulo.module.extension.MutableModuleInheritableNamedPointer;
import consulo.roots.ModuleRootLayer;

/**
 * @author VISTALL
 * @since 22.02.2015
 */
public class KRuntimeMutableModuleExtension extends KRuntimeModuleExtension implements DotNetSimpleMutableModuleExtension<KRuntimeModuleExtension>
{
	public KRuntimeMutableModuleExtension(@NotNull String id, @NotNull ModuleRootLayer rootModel)
	{
		super(id, rootModel);
	}

	@NotNull
	@Override
	public MutableModuleInheritableNamedPointer<Sdk> getInheritableSdk()
	{
		return (MutableModuleInheritableNamedPointer<Sdk>) super.getInheritableSdk();
	}

	@Nullable
	@Override
	@consulo.annotations.RequiredDispatchThread
	public JComponent createConfigurablePanel(@NotNull Runnable runnable)
	{
		ModuleExtensionSdkBoxBuilder<KRuntimeMutableModuleExtension> sdkBoxBuilder = ModuleExtensionSdkBoxBuilder.create(this, runnable);
		sdkBoxBuilder.sdkTypeClass(getSdkTypeClass());
		sdkBoxBuilder.sdkPointerFunc(new NullableFunction<KRuntimeMutableModuleExtension, MutableModuleInheritableNamedPointer<Sdk>>()
		{
			@Nullable
			@Override
			public MutableModuleInheritableNamedPointer<Sdk> fun(KRuntimeMutableModuleExtension mutableModuleExtension)
			{
				return mutableModuleExtension.getInheritableSdk();
			}
		});

		JPanel panel = new JPanel(new VerticalFlowLayout(true, false));
		panel.add(sdkBoxBuilder.build());
		return panel;
	}

	@Override
	public void setEnabled(boolean b)
	{
		myIsEnabled = b;
	}

	@Override
	public boolean isModified(@NotNull KRuntimeModuleExtension kModuleExtension)
	{
		return isModifiedImpl(kModuleExtension);
	}
}
