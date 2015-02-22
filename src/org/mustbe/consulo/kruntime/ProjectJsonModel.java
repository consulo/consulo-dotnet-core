package org.mustbe.consulo.kruntime;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 22.02.2015
 */
public class ProjectJsonModel
{
	public static class Framework
	{
	}

	public Map<String, String> dependencies = new LinkedHashMap<String, String>();

	public Map<String, String> commands = new LinkedHashMap<String, String>();

	public Map<String, Framework> frameworks = new LinkedHashMap<String, Framework>();
}
