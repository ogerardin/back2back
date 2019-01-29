package org.ogerardin.b2b.embeddedmongo;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.process.runtime.Network;
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;

import java.io.IOException;

/**
 * A simple class to run a embedded MongoDB instance without the Spring machinery. The MongoDB version is
 * extracted from the application.properties file.
 */
public class StandaloneMongoRunner {


    public static void main(String[] args) throws IOException, InterruptedException {

        // register handler for "classpath:" URLs
        TomcatURLStreamHandlerFactory.register();

        IFeatureAwareVersion version = EmbeddedMongoInfo.getMongoVersion("config/application.properties");

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

}
