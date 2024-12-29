/**
 * @author VISTALL
 * @since 11/01/2023
 */
module consulo.dotnet.core.msbuild.impl {
    requires consulo.dotnet.impl;
    requires consulo.msbuild.impl.dotnet;
    requires consulo.msbuild.lang.impl.csharp;
    requires consulo.dotnet.psi.impl;
    requires consulo.dotnet.core.api;
    requires consulo.dotnet.debugger.impl;
    requires consulo.execution.debugger.dap;

    // TODO remove in future
    requires java.desktop;
    requires consulo.dotnet.debugger.api;

    exports consulo.dotnet.core.msbuild.impl;
    exports consulo.dotnet.core.msbuild.impl.extension;
}