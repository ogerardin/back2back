package org.ogerardin.b2b.embeddedmongo;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.config.DownloadConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Feature;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.distribution.Versions;
import de.flapdoodle.embed.process.distribution.GenericVersion;
import de.flapdoodle.embed.process.store.Downloader;
import lombok.NonNull;
import lombok.val;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

/**
 * Provides information about the version of MongoDB required by Embedded Mongo.
 * The version and features are read from the resource config/application.properties.
 * <p>
 * This class intentionally doesn't use any Spring feature so that it can be used outside of a Spring context.
 *
 * @see #main
 */
public class EmbeddedMongoInfo {

    private static final String MONGODB_EMBEDDED_VERSION_PROPERTY = "spring.mongodb.embedded.version";
    private static final String MONGODB_EMBEDDED_FEATURES_PROPERTY = "spring.mongodb.embedded.features";

    /**
     * Prints the following information to {@link System#out} (one per line):
     * - download URL of the MongoDB distributable archive for the current platform
     * - local path where the archive should be saved to be picked up by {@link CustomEmbeddedMongoConfiguration}
     * This is intended to be used by a setup wizard at install time  to give the user a chance to download
     * MongoDB ahead of time.
     * <p>
     * To invoke this class from a repackaged jar generated by spring-boot-maven-plugin, use this:
     * java -cp xxx.jar -Dloader.main=org.ogerardin.b2b.embeddedmongo.EmbeddedMongoInfo org.springframework.boot.loader.PropertiesLauncher
     */
    public static void main(String args[]) throws IOException {
        val downloadConfig = new DownloadConfigBuilder()
                .defaultsForCommand(Command.MongoD)
                .build();

        val mongoVersion = getMongoVersion("config/application.properties");
        val distribution = CustomEmbeddedMongoConfiguration.detectDistribution(mongoVersion);

        String distributionPath = downloadConfig.getPackageResolver().getPath(distribution);
        String downloadUrl = new Downloader().getDownloadUrl(downloadConfig, distribution);

        System.out.println(downloadUrl);
        System.out.println(CustomEmbeddedMongoConfiguration.MONGODB_DISTRIBUTION_DIR.resolve(distributionPath)
                .toString());
    }

    /**
     * Returns a {@link IFeatureAwareVersion} that matches the MongoDB version and features in the specified properties
     * file resource
     */
    static IFeatureAwareVersion getMongoVersion(String resource) throws IOException {
        Properties properties = getProperties(resource);

        String mongoVersion = properties.getProperty(MONGODB_EMBEDDED_VERSION_PROPERTY);
        String mongoFeatures = properties.getProperty(MONGODB_EMBEDDED_FEATURES_PROPERTY);

        Feature[] featureArray = null;
        if (mongoFeatures != null) {
            featureArray = Arrays.stream(mongoFeatures.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Feature::valueOf)
                    .toArray(Feature[]::new);
        }

        return determineVersion(mongoVersion, featureArray);
    }

    /**
     * Retruns a {@link Properties} object initialized from the specified properties file resource
     */
    private static Properties getProperties(String resource) throws IOException {
        URL propertiesUrl = StandaloneMongoRunner.class.getClassLoader().getResource(resource);
        Properties properties = new Properties();
        properties.load(propertiesUrl.openStream());
        return properties;
    }

    /**
     * Returns a {@link IFeatureAwareVersion} that matches the specidied version and features.
     * Adapted from {@link EmbeddedMongoAutoConfiguration#determineVersion()} which is unfortunately private.
     *
     * @param ver      MongoDB version string, e.g. "4.0.4*
     * @param features an array of desired MongoDB {@link Feature}s.
     */
    private static IFeatureAwareVersion determineVersion(@NonNull String ver, Feature... features) {
        if (features == null) {
            for (Version version : Version.values()) {
                if (version.asInDownloadPath().equals(ver)) {
                    return version;
                }
            }
            return Versions.withFeatures(new GenericVersion(ver));
        }
        return Versions.withFeatures(new GenericVersion(ver), features);
    }

}
