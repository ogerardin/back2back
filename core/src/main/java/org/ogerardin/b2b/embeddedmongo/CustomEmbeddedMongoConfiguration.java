package org.ogerardin.b2b.embeddedmongo;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.DownloadConfigBuilder;
import de.flapdoodle.embed.mongo.config.ExtractedArtifactStoreBuilder;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.exceptions.DistributionException;
import de.flapdoodle.embed.process.io.IStreamProcessor;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.Slf4jLevel;
import de.flapdoodle.embed.process.io.progress.Slf4jProgressListener;
import de.flapdoodle.embed.process.store.Downloader;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * A custom configuration class for Embedded Mongo. Much of the code is copied from
 * {@link org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration} due to methods being private...
 */
@Configuration
@EnableConfigurationProperties(MongoProperties.class)
@Slf4j
public class CustomEmbeddedMongoConfiguration {

    private static final Logger MONGO_LOGGER = LoggerFactory
            .getLogger(CustomEmbeddedMongoConfiguration.class.getPackage().getName() + ".EmbeddedMongo");

    private final ApplicationContext context;

    private final MongoProperties properties;


    public CustomEmbeddedMongoConfiguration(ApplicationContext context, MongoProperties properties) {
        this.context = context;
        this.properties = properties;
    }


    /**
     * Override default configuration bean.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public MongodExecutable embeddedMongoServer(IMongodConfig mongodConfig) {

        // if a port is specified, configure embedded Mongo to listen on that port
        Integer configuredPort = this.properties.getPort();
        if (configuredPort == null || configuredPort == 0) {
            setEmbeddedPort(mongodConfig.net().getPort());
        }

        return getMongodExecutable(mongodConfig);

    }

    /**
     * Return an instance of {@link MongodExecutable}.
     * First try to obtain MongoDB from classpath, then if it failed try default.
     */
    public static MongodExecutable getMongodExecutable(IMongodConfig mongodConfig) {
        // Try from classpath (bundled MongoDB)
        try {
            val runtimeConfig = new CustomRuntimeConfigBuilder()
                    .fromClasspath(Command.MongoD, MONGO_LOGGER)
                    .build();
            val mongodStarter = MongodStarter.getInstance(runtimeConfig);
            val executable = mongodStarter.prepare(mongodConfig);
            return executable;
        } catch (DistributionException e) {
            // failed to start using bundle configuration
            log.info("MongoDB not available as bundled: {}", e.toString());
        }

        // Try from filesystem
        try {
            val runtimeConfig = new CustomRuntimeConfigBuilder()
                    .fromFilesystem(Command.MongoD, MONGO_LOGGER)
                    .build();
            val mongodStarter = MongodStarter.getInstance(runtimeConfig);
            val executable = mongodStarter.prepare(mongodConfig);
            return executable;
        } catch (DistributionException e) {
            // failed to start using bundle configuration
            log.info("MongoDB not available in filesystem: {}", e.toString());
        }

        // If it failed, try to use the default downloader
        val runtimeConfig = new CustomRuntimeConfigBuilder()
                .defaultsWithLoggerAndProxy(Command.MongoD, MONGO_LOGGER)
                .build();
        val mongodStarter = MongodStarter.getInstance(runtimeConfig);
        val executable = mongodStarter.prepare(mongodConfig);
        return executable;
    }

    // copied from org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration.setEmbeddedPort
    private void setEmbeddedPort(int port) {
        setPortProperty(this.context, port);
    }

    // copied from org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration.setPortProperty
    private void setPortProperty(ApplicationContext currentContext, int port) {
        if (currentContext instanceof ConfigurableApplicationContext) {
            MutablePropertySources sources = ((ConfigurableApplicationContext) currentContext)
                    .getEnvironment().getPropertySources();
            getMongoPorts(sources).put("local.mongo.port", port);
        }
        if (currentContext.getParent() != null) {
            setPortProperty(currentContext.getParent(), port);
        }
    }

    // copied from org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration.getMongoPorts
    @SuppressWarnings("unchecked")
    private Map<String, Object> getMongoPorts(MutablePropertySources sources) {
        PropertySource<?> propertySource = sources.get("mongo.ports");
        if (propertySource == null) {
            propertySource = new MapPropertySource("mongo.ports", new HashMap<>());
            sources.addFirst(propertySource);
        }
        return (Map<String, Object>) propertySource.getSource();
    }


    static class CustomRuntimeConfigBuilder extends de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder {
        /**
         * @return A {@link RuntimeConfigBuilder} set to use a custom output procesor and
         *          a proxy obtained through {@link ProxyHelper}
         */
        public RuntimeConfigBuilder defaultsWithLoggerAndProxy(Command command, Logger logger) {
            super.defaultsWithLogger(command, logger);
            processOutput().overwriteDefault(getOutputProcessor(logger));

            // automatic proxy detecttion
            val proxyFactory = ProxyHelper.detectProxyFactory();

            val downloadConfig = new DownloadConfigBuilder()
                    .defaultsForCommand(command)
                    .proxyFactory(proxyFactory)
                    .progressListener(new Slf4jProgressListener(logger))
                    .build();

            val artifactStore = new ExtractedArtifactStoreBuilder()
                    .defaults(command)
                    .download(downloadConfig)
                    .build();

            artifactStore().overwriteDefault(artifactStore);

            return this;
        }

        public RuntimeConfigBuilder fromClasspath(Command command, Logger logger) {
            String baseUrl = "classpath:/bundled/mongodb/";
            return defaultsWithBaseUrl(command, logger, null, baseUrl);
        }

        public RuntimeConfigBuilder fromFilesystem(Command command, Logger logger) {
            String baseUrl = Paths.get("mongodb").toUri().toString() + "/";
            return defaultsWithBaseUrl(command, logger, null, baseUrl);
        }

        /**
         * @return A {@link RuntimeConfigBuilder} set to use a custom output procesor and
         *          obtain the distributable as a resource from a given path.
         */
        public RuntimeConfigBuilder defaultsWithBaseUrl(Command command, Logger logger, Downloader downloader, String path) {
            defaultsWithLoggerAndProxy(command, logger);

            // a custom download config where the base URL points to a classpath resource
            val downloadConfig = new DownloadConfigBuilder()
                    .defaultsForCommand(command)
                    .downloadPath(path)
                    .progressListener(new NopProgressListener())
                    .build();

            val artifactStoreBuilder = new ExtractedArtifactStoreBuilder()
                    .defaults(command)
                    .download(downloadConfig);
            if (downloader != null) {
                artifactStoreBuilder.downloader(downloader);
            }
            artifactStore().overwriteDefault(artifactStoreBuilder.build());

            return this;
        }

        private static ProcessOutput getOutputProcessor(Logger logger) {
            return new ProcessOutput(
                    logTo(logger, Slf4jLevel.INFO), // stdout logs an INFO message
                    logTo(logger, Slf4jLevel.ERROR), // stderr logs an ERROR message
                    Processors.named("[console>]", logTo(logger, Slf4jLevel.DEBUG)));
        }

        private static IStreamProcessor logTo(org.slf4j.Logger logger, Slf4jLevel level) {
            return new MongoSlf4jStreamProcessor(logger, level);
        }

    }

}
