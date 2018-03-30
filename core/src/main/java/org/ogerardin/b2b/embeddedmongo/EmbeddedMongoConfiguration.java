package org.ogerardin.b2b.embeddedmongo;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.config.DownloadConfigBuilder;
import de.flapdoodle.embed.mongo.config.ExtractedArtifactStoreBuilder;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.config.store.IProxyFactory;
import de.flapdoodle.embed.process.io.IStreamProcessor;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.Slf4jLevel;
import de.flapdoodle.embed.process.io.progress.Slf4jProgressListener;
import de.flapdoodle.embed.process.store.ArtifactStoreBuilder;
import org.ogerardin.b2b.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

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
                logTo(MONGO_LOGGER, Slf4jLevel.INFO), // stdout logs an INFO message
                logTo(MONGO_LOGGER, Slf4jLevel.ERROR), // stderr logs an ERROR message
                Processors.named("[console>]", logTo(MONGO_LOGGER, Slf4jLevel.DEBUG)));

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

    private static IStreamProcessor logTo(org.slf4j.Logger logger, Slf4jLevel level) {
        return new MongoSlf4jStreamProcessor(logger, level);
    }

    /**
     * Stream processor for feeding MongoDB log output to a specified {@link Logger}.
     * Each line is handled as follows:
     * -remove line breaks
     * -strip leading timestamp
     * -map log severity (one character) to a Slf4j level and log to this level
     */
    private static class MongoSlf4jStreamProcessor implements IStreamProcessor {
        private static final Map<Character, Slf4jLevel> LEVEL_MAP = Maps.mapOf(
                'D', Slf4jLevel.DEBUG,
                'I', Slf4jLevel.INFO,
                'W', Slf4jLevel.WARN,
                'E', Slf4jLevel.ERROR,
                'F', Slf4jLevel.ERROR
        );

        private final Logger logger;
        private final Slf4jLevel defaultLevel;

        MongoSlf4jStreamProcessor(Logger logger, Slf4jLevel defaultLevel) {
            this.logger = logger;
            this.defaultLevel = defaultLevel;
        }

        @Override
        public void process(String block) {
            String line = block
                    .replaceAll("[\n\r]+", "")
                    // remove timestamps
                    .replaceAll("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}(\\+\\d{4})? ", "");

            Slf4jLevel level = defaultLevel;
            if (line.charAt(1) == ' ') {
                Character mongoLevel = line.charAt(0);
                Slf4jLevel mappedLevel = LEVEL_MAP.get(mongoLevel);
                if (mappedLevel != null) {
                    level = mappedLevel;
                    line = line.substring(2);
                }
            }
            level.log(logger, line);
        }

        @Override
        public void onProcessed() {
        }

    }

}
