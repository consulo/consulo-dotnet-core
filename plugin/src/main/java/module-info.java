/**
 * @author VISTALL
 * @since 09/01/2023
 */
module consulo.dotnet.core {
  requires consulo.ide.api;
  requires consulo.dotnet.api;
  requires consulo.dotnet.impl;
  requires consulo.dotnet.core.api;
  requires consulo.dotnet.core.msbuild.impl;

  requires consulo.msbuild.api;
  requires consulo.msbuild.daemon.impl;
}