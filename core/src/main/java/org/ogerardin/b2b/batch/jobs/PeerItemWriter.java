package org.ogerardin.b2b.batch.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemWriter;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 */
class PeerItemWriter implements ItemWriter<Path> {

    private static final Log logger = LogFactory.getLog(PeerItemWriter.class);

    private final String targetHostname;
    private final String targetPort;

    PeerItemWriter(String targetHostname, String targetPort) {

        this.targetHostname = targetHostname;
        this.targetPort = targetPort;
    }

    @Override
    public void write(List<? extends Path> items) throws Exception {
        logger.debug("Writing " + Arrays.toString(items.toArray()));

        for (Path path : items) {
            //TODO
        }

    }
}
