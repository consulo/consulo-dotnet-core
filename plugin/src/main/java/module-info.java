/**
 * @author VISTALL
 * @since 09/01/2023
 */
module consulo.dotnet.core {
  requires consulo.ide.api;
  requires consulo.ui.ex.api;
  requires consulo.build.ui.api;
  requires consulo.file.editor.api;
  requires consulo.module.ui.api;
  requires consulo.process.api;
  requires consulo.project.ui.view.api;
  requires consulo.dotnet.api;
  requires consulo.dotnet.impl;
  requires consulo.dotnet.core.api;
  requires consulo.dotnet.core.msbuild.impl;

  requires consulo.msbuild.api;
  requires consulo.msbuild.daemon.impl;
}