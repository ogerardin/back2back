package org.ogerardin.b2b.embeddedmongo;

import de.flapdoodle.embed.mongo.MongodExecutable;
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
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

/**
 * A simple app to start an embedded Mongo without all the Spring machinery.
 */
public class EmbeddedMongoRunner {

    public static void main(String[] args) throws IOException, InterruptedException {

        // register handler for "classpath:" URLs
        TomcatURLStreamHandlerFactory.register();

        // read mongo version and features from application.properties
        URL propertiesUrl = ClassLoader.getSystemResource("config/application.properties");
        Properties properties = new Properties();
        properties.load(propertiesUrl.openStream());

        String mongoVersion = properties.getProperty("spring.mongodb.embedded.version");
        String mongoFeatures = properties.getProperty("spring.mongodb.embedded.features");
        Feature[] featureArray = Arrays.stream(mongoFeatures.split(","))
                .map(String::trim)
                .map(Feature::valueOf)
                .toArray(Feature[]::new);

        IMongodConfig mongodConfig = new MongodConfigBuilder()
                .version(determineVersion(mongoVersion, featureArray))
                .net(new Net("localhost",27017, Network.localhostIsIPv6()))
                .replication(new Storage("mongo-storage",null,0))
                .build();

//        IRuntimeConfig runtimeConfig = new CustomEmbeddedMongoConfiguration.AlternateRuntimeConfigConfiguration().runtimeConfig();
        IRuntimeConfig runtimeConfig = new CustomEmbeddedMongoConfiguration.BundledRuntimeConfigConfiguration().runtimeConfig();
        MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);

        MongodExecutable mongodExecutable = null;
        try {
            mongodExecutable = runtime.prepare(mongodConfig);
            mongodExecutable.start();

            Thread.sleep(Long.MAX_VALUE);

        } finally {
            if (mongodExecutable != null)
                mongodExecutable.stop();
        }
    }

    private static IFeatureAwareVersion determineVersion(String ver, Feature... features) {
            for (Version version : Version.values()) {
                if (version.asInDownloadPath().equals(ver)) {
                    return version;
                }
            }
        return Versions.withFeatures(new GenericVersion(ver), features);
    }

}
