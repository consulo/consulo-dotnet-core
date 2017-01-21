/*
 * Copyright 2013-2017 must-be.org
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

import org.jetbrains.annotations.NotNull;
import consulo.csharp.module.extension.BaseCSharpModuleExtension;
import consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import consulo.roots.ModuleRootLayer;

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
