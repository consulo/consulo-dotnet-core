/**
 * @author VISTALL
 * @since 24/04/2023
 */
module consulo.dotnet.core.api
{
	requires transitive consulo.ide.api;
	requires transitive consulo.dotnet.api;
	requires consulo.dotnet.impl;

	exports consulo.dotnet.core.bundle;
}