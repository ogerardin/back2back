package org.ogerardin.b2b.embeddedmongo;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.config.DownloadConfigBuilder;
import de.flapdoodle.embed.mongo.config.ExtractedArtifactStoreBuilder;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.config.store.IProxyFactory;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.Slf4jLevel;
import de.flapdoodle.embed.process.io.progress.Slf4jProgressListener;
import de.flapdoodle.embed.process.store.ArtifactStoreBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddedMongoConfiguration {

    // custom logger for embedded Mongo
    private static final Logger MONGO_LOGGER = LoggerFactory.getLogger(EmbeddedMongoConfiguration.class.getPackage().getName() + ".EmbeddedMongo");

    /**
     * Override default configuration provided by
     * {@link EmbeddedMongoAutoConfiguration.RuntimeConfigConfiguration#embeddedMongoRuntimeConfig()}
     * to set a proxy if required
     */
    @Bean
    public static IRuntimeConfig runtimeConfig() {

        // how do we handle Mongo output ?
        ProcessOutput processOutput = new ProcessOutput(
                Processors.logTo(MONGO_LOGGER, Slf4jLevel.INFO), // stdout logs an INFO message
                Processors.logTo(MONGO_LOGGER, Slf4jLevel.ERROR), // stderr logs an ERROR message
                Processors.named("[console>]", Processors.logTo(MONGO_LOGGER, Slf4jLevel.DEBUG)));

        return new RuntimeConfigBuilder()
                .defaultsWithLogger(Command.MongoD, MONGO_LOGGER)
                .processOutput(processOutput)
                .artifactStore(getArtifactStore(MONGO_LOGGER))
                .build();
    }

    /**
     * Returns an {@link ArtifactStoreBuilder} configured with the detected proxy
     */
    private static ArtifactStoreBuilder getArtifactStore(Logger logger) {

        // automatic proxy detecttion
        IProxyFactory proxyFactory = ProxyHelper.detectProxyFactory();

        return new ExtractedArtifactStoreBuilder()
                .defaults(Command.MongoD)
                .download(new DownloadConfigBuilder()
                        .defaultsForCommand(Command.MongoD)
                        .proxyFactory(proxyFactory)
                        //TODO Mongo loading progress will have to be handled in the UI eventually
                        //TODO unless we provide a bundled mongo executable
                        .progressListener(new Slf4jProgressListener(logger))
                        .build());
    }

}
