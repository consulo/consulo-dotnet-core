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

package consulo.dnx.jom;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import consulo.json.jom.JomElement;
import consulo.json.jom.JomPropertyGetter;

/**
 * @author VISTALL
 * @since 13.11.2015
 */
public interface ProjectElement extends JomElement
{
	@JomPropertyGetter
	String[] getAuthors();

	@NotNull
	@JomPropertyGetter
	Map<String, String> getDependencies();

	@NotNull
	@JomPropertyGetter
	Map<String, String> getCommands();

	@NotNull
	@JomPropertyGetter
	Map<String, FrameworkElement> getFrameworks();
}
