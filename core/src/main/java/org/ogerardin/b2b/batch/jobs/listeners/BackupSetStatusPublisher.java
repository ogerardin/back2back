package org.ogerardin.b2b.batch.jobs.listeners;

import org.ogerardin.b2b.domain.entity.BackupSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * A simple publisher that publishes a backupSet on a topic using STOMP
 */
@Component
public class BackupSetStatusPublisher {

    private static final String PUBLISH_TOPIC = "/topic/message";

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public void publishStatus(BackupSet backupSet) {
        //TODO post a spcific status change message instead of the full BackupSet
        simpMessagingTemplate.convertAndSend(PUBLISH_TOPIC, backupSet);
    }

}
