package org.ogerardin.b2b.embeddedmongo;

import com.github.markusbernhardt.proxy.ProxySearch;
import de.flapdoodle.embed.mongo.config.DownloadConfigBuilder;
import de.flapdoodle.embed.process.config.store.HttpProxyFactory;
import de.flapdoodle.embed.process.config.store.IProxyFactory;
import de.flapdoodle.embed.process.config.store.NoProxyFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.*;
import java.text.MessageFormat;
import java.util.List;

@Slf4j
enum ProxyHelper {

    ; //memberless enum

    /**
     * Returns an {@link IProxyFactory} matching the system's proxy settings.
     * We use proxy-vole to detect proxy configuration, see: https://github.com/MarkusBernhardt/proxy-vole
     */
    static IProxyFactory detectProxyFactory() {
        // if the logger is enabled for debug, set an adapter to log debug output of proxy-vole
        if (log.isDebugEnabled()) {
            com.github.markusbernhardt.proxy.util.Logger.setBackend(new com.github.markusbernhardt.proxy.util.Logger.LogBackEnd() {
                @Override
                public void log(Class<?> clazz, com.github.markusbernhardt.proxy.util.Logger.LogLevel loglevel, String msg, Object... params) {
                    log.debug(MessageFormat.format(msg, params));
                }

                @Override
                public boolean isLogginEnabled(com.github.markusbernhardt.proxy.util.Logger.LogLevel logLevel) {
                    return true;
                }
            });
        }

        ProxySearch proxySearch = ProxySearch.getDefaultProxySearch();
        ProxySelector proxySelector = proxySearch.getProxySelector();
        if (proxySelector != null) {
            List<Proxy> proxies = proxySelector.select(URI.create("https://mongodb.org"));
            for (Proxy proxy : proxies) {
                try {
                    return getProxyFactory(proxy);
                } catch (Exception e) {
                    log.debug("Failed to convert " + proxy + " to IProxyFactory", e);
                }
            }
        }
        return new NoProxyFactory();
    }

    /**
     * Convert a Java {@link Proxy} object to a {@link IProxyFactory} suitable for
     * passing to {@link DownloadConfigBuilder#proxyFactory}
     *
     * @throws IllegalArgumentException if the proxy type is not supported
     */
    private static IProxyFactory getProxyFactory(Proxy proxy) {
        Proxy.Type proxyType = proxy.type();
        SocketAddress proxyAddress = proxy.address();

        switch (proxyType) {
            case DIRECT:
                return new NoProxyFactory();
            case HTTP: {
                if (proxyAddress == null) {
                    throw new IllegalArgumentException("Proxy address is null");
                }
                if (!(proxyAddress instanceof InetSocketAddress)) {
                    throw new IllegalArgumentException("Unsupported proxy address type: " + proxyAddress.getClass());
                }
                InetSocketAddress inetSocketAddress = (InetSocketAddress) proxyAddress;
                return new HttpProxyFactory(inetSocketAddress.getHostName(), inetSocketAddress.getPort());
            }
            default:
                throw new IllegalArgumentException("Unsupported proxy type: " + proxyType);
        }
    }
}