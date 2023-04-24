/**
 * @author VISTALL
 * @since 09/01/2023
 */
module consulo.dotnet.core {
  requires consulo.ide.api;
  requires consulo.dotnet.api;
  requires consulo.dotnet.impl;

  exports consulo.dotnet.core.bundle;
}