package consulo.dotnet.core.msbuild.impl.debug;

import consulo.dotnet.debugger.proxy.DotNetTypeProxy;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxyVisitor;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2025-01-03
 */
public class DotNetValueProxyImpl implements DotNetValueProxy {
    @Nullable
    @Override
    public DotNetTypeProxy getType() {
        return null;
    }

    @Nonnull
    @Override
    public Object getValue() {
        return this;
    }

    @Override
    public void accept(DotNetValueProxyVisitor dotNetValueProxyVisitor) {

    }
}
