package org.ogerardin.b2b.mongo;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.extract.UserTempNaming;
import de.flapdoodle.embed.process.runtime.Network;

import java.io.IOException;

public class EmbeddedMongoRunner {

    public static void main(String[] args) throws IOException, InterruptedException {
        Command command = Command.MongoD;
        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
                .defaults(command)
                .artifactStore(new ExtractedArtifactStoreBuilder()
                        .defaults(command)
                        .download(new DownloadConfigBuilder()
                                .defaultsForCommand(command).build())
                        .executableNaming(new UserTempNaming()))
                .build();

        Storage replication = new Storage("mongo-storage",null,0);

        IMongodConfig mongodConfig = new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(27017, Network.localhostIsIPv6()))
                .replication(replication)
                .build();

        MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);

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
}
