package org.ogerardin.b2b.embeddedmongo;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.distribution.Feature;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.distribution.Versions;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.distribution.GenericVersion;
import de.flapdoodle.embed.process.runtime.Network;

import java.io.IOException;

/**
 * A simple app to start an embedded Mongo without all the Spring machinery.
 */
public class EmbeddedMongoRunner {

    private static final IRuntimeConfig RUNTIME_CONFIG = CustomEmbeddedMongoConfiguration.runtimeConfig();

    public static void main(String[] args) throws IOException, InterruptedException {

        Storage replication = new Storage("mongo-storage",null,0);

        IMongodConfig mongodConfig = new MongodConfigBuilder()
//                .version(Version.V3_5_5)
                //TODO get these values from application.properties
                .version(determineVersion("4.0.4", Feature.ONLY_WINDOWS_2008_SERVER, Feature.ONLY_WITH_SSL, Feature.NO_HTTP_INTERFACE_ARG))
                .net(new Net("localhost",27017, Network.localhostIsIPv6()))
                .replication(replication)
                .build();

        MongodStarter runtime = MongodStarter.getInstance(RUNTIME_CONFIG);

        MongodExecutable mongodExecutable = null;
        try {
            mongodExecutable = runtime.prepare(mongodConfig);
            MongodProcess mongod = mongodExecutable.start();

            Thread.sleep(Long.MAX_VALUE);

        } finally {
            if (mongodExecutable != null)
                mongodExecutable.stop();
        }
    }

    private static IFeatureAwareVersion determineVersion(String ver, Feature... features) {
            for (Version version : Version.values()) {
                if (version.asInDownloadPath()
                        .equals(ver)) {
                    return version;
                }
            }
        return Versions.withFeatures(new GenericVersion(ver), features);
    }

}
