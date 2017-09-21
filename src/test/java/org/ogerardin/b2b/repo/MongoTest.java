package org.ogerardin.b2b.repo;

import com.github.markusbernhardt.proxy.ProxySearch;
import com.github.markusbernhardt.proxy.ProxySearch.Strategy;
import com.github.markusbernhardt.proxy.ProxySearchStrategy;
import com.github.markusbernhardt.proxy.util.Logger.LogBackEnd;
import com.github.markusbernhardt.proxy.util.PlatformUtil;
import com.github.markusbernhardt.proxy.util.PlatformUtil.Platform;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.config.DownloadConfigBuilder;
import de.flapdoodle.embed.mongo.config.ExtractedArtifactStoreBuilder;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.config.store.*;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.Slf4jLevel;
import de.flapdoodle.embed.process.io.progress.Slf4jProgressListener;
import de.flapdoodle.embed.process.store.ArtifactStoreBuilder;
import org.junit.runner.RunWith;
import org.ogerardin.b2b.domain.FileSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.net.*;
import java.text.MessageFormat;
import java.util.List;

@Configuration
@EnableAutoConfiguration
public class MongoTest implements CommandLineRunner {

    /**
     * Override default configuration provided by
     * {@link EmbeddedMongoAutoConfiguration.RuntimeConfigConfiguration#embeddedMongoRuntimeConfig()}
     * to set a proxy if required
     */
    @Bean
    public static IRuntimeConfig runtimeConfig() {
        Logger logger = LoggerFactory.getLogger(MongoTest.class.getPackage().getName() + ".EmbeddedMongo");

        ProcessOutput processOutput = new ProcessOutput(
                Processors.logTo(logger, Slf4jLevel.INFO),
                Processors.logTo(logger, Slf4jLevel.ERROR),
                Processors.named("[console>]", Processors.logTo(logger, Slf4jLevel.DEBUG)));

        return new RuntimeConfigBuilder()
                .defaultsWithLogger(Command.MongoD, logger)
                .processOutput(processOutput)
                .artifactStore(getArtifactStore(logger))
                .build();
    }

    private static ArtifactStoreBuilder getArtifactStore(Logger logger) {
        IProxyFactory proxyFactory = detectProxyFactory(logger);

        return new ExtractedArtifactStoreBuilder().defaults(Command.MongoD)
                .download(new DownloadConfigBuilder()
                        .defaultsForCommand(Command.MongoD)
                        .proxyFactory(proxyFactory)
                        .progressListener(new Slf4jProgressListener(logger))
                        .build());
    }

    /**
     * Return an {@link IProxyFactory} matching the system's proxy settings.
     * We use proxy-vole to get proxy configuration, see: https://github.com/MarkusBernhardt/proxy-vole
     */
    private static IProxyFactory detectProxyFactory(Logger logger) {
        com.github.markusbernhardt.proxy.util.Logger.setBackend(new LogBackEnd() {
            @Override
            public void log(Class<?> clazz, com.github.markusbernhardt.proxy.util.Logger.LogLevel loglevel, String msg, Object... params) {
                logger.info(MessageFormat.format(msg, params));
            }

            @Override
            public boolean isLogginEnabled(com.github.markusbernhardt.proxy.util.Logger.LogLevel logLevel) {
                return true;
            }
        });

        ProxySearch proxySearch = ProxySearch.getDefaultProxySearch();
        ProxySelector proxySelector = proxySearch.getProxySelector();
        if (proxySelector != null) {
            List<Proxy> proxies = proxySelector.select(URI.create("https://mongodb.org"));
            for (Proxy proxy : proxies) {
                try {
                    return getProxyFactory(proxy);
                } catch (Exception e) {
                    logger.debug("Failed to convert " + proxy + " to IProxyFactory", e);
                }
            }
        }
        return new NoProxyFactory();
    }

    /**
     * Convert a Java {@link Proxy} object to a {@link IProxyFactory} suitable for
     * passing to {@link DownloadConfigBuilder#proxyFactory}
     * @throws IllegalArgumentException if the proxy type is not supported
     */
    private static IProxyFactory getProxyFactory(Proxy proxy) {
        switch (proxy.type()) {
            case DIRECT:
                return new NoProxyFactory();
            case HTTP: {
                if (! (proxy.address() instanceof InetSocketAddress)) {
                    throw new IllegalArgumentException("proxy.address() must be an InetSocketAddress");
                }
                InetSocketAddress inetSocketAddress = (InetSocketAddress) proxy.address();
                if (inetSocketAddress == null) {
                    throw new IllegalArgumentException("proxy.address() is null");
                }
                return new HttpProxyFactory(inetSocketAddress.getHostName(), inetSocketAddress.getPort());
            }
            default:
                throw new IllegalArgumentException("proxy.type() must be DIRECT or HTTP");
        }
    }

    @Autowired
    SourceRepository repo;

    public static void main(String[] args) {
        new SpringApplicationBuilder(MongoTest.class)
                .web(false)
                .run(args);
    }


    @Override
    public void run(String... args) throws Exception {

        repo.deleteAll();

        repo.save(new FileSource("/tmp","/Users/Olivier/Documents"));

        repo.findAll().forEach(System.out::println);


    }
}
