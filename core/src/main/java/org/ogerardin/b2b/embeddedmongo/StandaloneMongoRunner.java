package org.ogerardin.b2b.embeddedmongo;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Feature;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.distribution.Versions;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.GenericVersion;
import de.flapdoodle.embed.process.runtime.Network;
import de.flapdoodle.embed.process.store.Downloader;
import lombok.val;
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

/**
 * A simple class to run a embedded MongoDB instance without the Spring machinery. The MongoDB version is
 * extracted from the application.properties file.
 */
public class StandaloneMongoRunner {

    /**
     * Return a MongoDB version that matches the
     */
    static IFeatureAwareVersion getMongoVersion() throws IOException {
        Properties properties = getProperties("config/application.properties");

        String mongoVersion = properties.getProperty("spring.mongodb.embedded.version");
        String mongoFeatures = properties.getProperty("spring.mongodb.embedded.features");
        Feature[] featureArray = Arrays.stream(mongoFeatures.split(","))
                .map(String::trim)
                .map(Feature::valueOf)
                .toArray(Feature[]::new);

        return determineVersion(mongoVersion, featureArray);
    }

    private static Properties getProperties(@SuppressWarnings("SameParameterValue") String propertiesFile) throws IOException {
        // read mongo version and features from application.properties
        URL propertiesUrl = ClassLoader.getSystemResource(propertiesFile);
        Properties properties = new Properties();
        properties.load(propertiesUrl.openStream());
        return properties;
    }

    private static IFeatureAwareVersion determineVersion(String ver, Feature... features) {
            for (Version version : Version.values()) {
                if (version.asInDownloadPath().equals(ver)) {
                    return version;
                }
            }
        return Versions.withFeatures(new GenericVersion(ver), features);
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        // register handler for "classpath:" URLs
        TomcatURLStreamHandlerFactory.register();

        IFeatureAwareVersion version = getMongoVersion();

        IMongodConfig mongodConfig = new MongodConfigBuilder()
                .version(version)
                .net(new Net("localhost",27017, Network.localhostIsIPv6()))
                .replication(new Storage("mongo-storage",null,0))
                .build();

/*
        IRuntimeConfig runtimeConfig = new CustomEmbeddedMongoConfiguration.CustomRuntimeConfigBuilder()
                .defaultsWithLogger(Command.MongoD, log)
//                .defaultsWithBundled(Command.MongoD, log)
                .build();
        MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
*/

        MongodExecutable mongodExecutable = null;
        try {
//            mongodExecutable = runtime.prepare(mongodConfig);
            mongodExecutable =  CustomEmbeddedMongoConfiguration.getMongodExecutable(mongodConfig);
            mongodExecutable.start();

            Thread.sleep(Long.MAX_VALUE);

        } finally {
            if (mongodExecutable != null)
                mongodExecutable.stop();
        }
    }

    public static class Info {

        public static void main(String args[]) throws IOException {
            val downloadConfig = new DownloadConfigBuilder()
                    .defaultsForCommand(Command.MongoD)
                    .build();

            IFeatureAwareVersion mongoVersion = getMongoVersion();
            Distribution distribution = Distribution.detectFor(mongoVersion);

            String distributionPath = downloadConfig.getPackageResolver().getPath(distribution);
            String downloadUrl = new Downloader().getDownloadUrl(downloadConfig, distribution);

            System.out.printf("distributionPath=%s%n", distributionPath);
            System.out.printf("downloadUrl=%s%n", downloadUrl);
        }
    }
}
