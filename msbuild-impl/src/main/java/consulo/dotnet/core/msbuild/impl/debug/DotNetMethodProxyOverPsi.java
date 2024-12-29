package consulo.dotnet.core.msbuild.impl.debug;

import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.debugger.proxy.*;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;
import consulo.language.psi.PsiElement;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2024-12-25
 */
public class DotNetMethodProxyOverPsi implements DotNetMethodProxy {
    @Override
    public String getName() {
        return "method";
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isAnnotatedBy(@Nonnull String s) {
        return false;
    }

    @Nonnull
    @Override
    public DotNetTypeProxy getDeclarationType() {
        return new DotNetTypeProxyOverPsi();
    }

    @Nonnull
    @Override
    public DotNetMethodParameterProxy[] getParameters() {
        return new DotNetMethodParameterProxy[0];
    }

    @Nonnull
    @Override
    public DotNetLocalVariableProxy[] getLocalVariables(@Nonnull DotNetStackFrameProxy dotNetStackFrameProxy) {
        return new DotNetLocalVariableProxy[0];
    }

    @Nullable
    @Override
    public DotNetValueProxy invoke(@Nonnull DotNetStackFrameProxy dotNetStackFrameProxy, @Nullable DotNetValueProxy dotNetValueProxy, @Nonnull DotNetValueProxy[] dotNetValueProxies) throws DotNetThrowValueException, DotNetNotSuspendedException {
        return null;
    }

    @RequiredReadAction
    @Nullable
    @Override
    public PsiElement findExecutableElementFromDebugInfo(@Nonnull Project project, int i) {
        return null;
    }
}
