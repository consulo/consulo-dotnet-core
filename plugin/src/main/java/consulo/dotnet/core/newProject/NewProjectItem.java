package consulo.dotnet.core.newProject;

import java.util.Set;

/**
 * @author VISTALL
 * @since 23/04/2023
 */
public record NewProjectItem(String name, String shortName, Set<String> languages, String defaultLanguage) {
}
