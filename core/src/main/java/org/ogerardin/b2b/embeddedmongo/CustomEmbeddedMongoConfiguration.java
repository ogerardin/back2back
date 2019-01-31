package org.ogerardin.b2b.embeddedmongo;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.DownloadConfigBuilder;
import de.flapdoodle.embed.mongo.config.ExtractedArtifactStoreBuilder;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.distribution.BitSize;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Platform;
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

import java.nio.file.Path;
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

    static final Path MONGODB_DISTRIBUTION_DIR = Paths.get("mongodb");

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
     * Tries the following strategies (in this order) to obtain the MongoDB distributable archive (first to succeed wins)
     * - Bundled (archive as a resource in the classpath)
     * - File (archive as a file on filesystem)
     * - Default behaviour (download from internet)
     */
    static MongodExecutable getMongodExecutable(IMongodConfig mongodConfig) {
        // Try from classpath (bundled MongoDB)
        try {
            val runtimeConfig1 = new CustomRuntimeConfigBuilder()
                    .fromClasspath(Command.MongoD, MONGO_LOGGER)
                    .build();
            return getMongodExecutable(mongodConfig, runtimeConfig1);
        } catch (DistributionException e) {
            log.info("MongoDB not available as bundled: {}", e.toString());
        }

        // Try from filesystem
        try {
            val runtimeConfig2 = new CustomRuntimeConfigBuilder()
                    .fromFilesystem(Command.MongoD, MONGO_LOGGER)
                    .build();
            return getMongodExecutable(mongodConfig, runtimeConfig2);
        } catch (DistributionException e) {
            log.info("MongoDB not available in filesystem: {}", e.toString());
        }

        // If previous attempts failed, try to use the default downloader
        val runtimeConfig0 = new CustomRuntimeConfigBuilder()
                .defaultsWithLoggerAndProxy(Command.MongoD, MONGO_LOGGER)
                .build();
        return getMongodExecutable(mongodConfig, runtimeConfig0);
    }

    private static MongodExecutable getMongodExecutable(IMongodConfig mongodConfig, IRuntimeConfig runtimeConfig) {
        val mongodStarter = MongodStarter.getInstance(runtimeConfig);
        val distribution = detectDistribution(mongodConfig.version());
        val executable = mongodStarter.prepare(mongodConfig, distribution);
        return executable;
    }

    /**
     * Replacement for {@link Distribution#detectFor}
     * See https://github.com/flapdoodle-oss/de.flapdoodle.embed.process/issues/32
     */
    static Distribution detectDistribution(IFeatureAwareVersion version) {
        return new Distribution(version, Platform.detect(), getOsBitness());
    }

    private static BitSize getOsBitness() {
        if (Platform.detect() == Platform.Windows) {
            // We want the atual OS bitness (not the JVM bitness)
            // See https://stackoverflow.com/a/5940770/170637
            String arch = System.getenv("PROCESSOR_ARCHITECTURE");
            String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
            return (((arch != null) && arch.endsWith("64"))
                    || ((wow64Arch != null) && wow64Arch.endsWith("64")))
                    ? BitSize.B64 : BitSize.B32;
        }
        return BitSize.detect();
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
            String baseUrl = MONGODB_DISTRIBUTION_DIR.toUri().toString() + "/";
            return defaultsWithBaseUrl(command, logger, null, baseUrl);
        }

        /**
         * @return A {@link RuntimeConfigBuilder} set to use a custom output procesor and
         *          obtain the distributable as a resource from a given path.
         */
        public RuntimeConfigBuilder defaultsWithBaseUrl(Command command, Logger logger, Downloader downloader, String path) {
            defaultsWithLogger(command, logger);

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
