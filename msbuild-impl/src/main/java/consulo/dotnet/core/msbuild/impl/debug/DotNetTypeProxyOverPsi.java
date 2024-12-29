package consulo.dotnet.core.msbuild.impl.debug;

import consulo.dotnet.debugger.proxy.DotNetFieldProxy;
import consulo.dotnet.debugger.proxy.DotNetMethodProxy;
import consulo.dotnet.debugger.proxy.DotNetPropertyProxy;
import consulo.dotnet.debugger.proxy.DotNetTypeProxy;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2024-12-25
 */
public class DotNetTypeProxyOverPsi implements DotNetTypeProxy {
    @Override
    public boolean isAnnotatedBy(@Nonnull String s) {
        return false;
    }

    @Nullable
    @Override
    public DotNetTypeProxy getDeclarationType() {
        return null;
    }

    @Nonnull
    @Override
    public String getName() {
        return "test"; // TODO
    }

    @Nonnull
    @Override
    public String getFullName() {
        return "todo";  // TODO
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Nullable
    @Override
    public DotNetTypeProxy getBaseType() {
        return null;
    }

    @Nonnull
    @Override
    public DotNetTypeProxy[] getInterfaces() {
        return new DotNetTypeProxy[0];
    }

    @Nonnull
    @Override
    public DotNetFieldProxy[] getFields() {
        return new DotNetFieldProxy[0];
    }

    @Nonnull
    @Override
    public DotNetPropertyProxy[] getProperties() {
        return new DotNetPropertyProxy[0];
    }

    @Nonnull
    @Override
    public DotNetMethodProxy[] getMethods() {
        return new DotNetMethodProxy[0];
    }

    @Override
    public boolean isNested() {
        return false;
    }

    @Nullable
    @Override
    public DotNetMethodProxy findMethodByName(@Nonnull String s, boolean b, DotNetTypeProxy[] dotNetTypeProxies) {
        return null;
    }

    @Override
    public boolean isAssignableFrom(@Nonnull DotNetTypeProxy dotNetTypeProxy) {
        return false;
    }
}
