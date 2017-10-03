package org.ogerardin.b2b.batch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.B2BException;
import org.ogerardin.b2b.domain.BackupSource;
import org.ogerardin.b2b.domain.BackupTarget;
import org.ogerardin.b2b.worker.SingleFileProcessor;
import org.springframework.batch.core.Job;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class LocalToPeerBackupJobBuilder extends BackupJobBuilderBase implements BackupJobBuilder {

    private static final Log logger = LogFactory.getLog(LocalToPeerBackupJobBuilder.class);

    @Override
    public Job newBackupJob(BackupSource source, BackupTarget target) throws B2BException {
        Job job = null;
        try {
            job = newBackupJob(new SingleFileProcessor() {
                @Override
                public void process(File f) {
                    //nop
                }
            }, listener());
        } catch (Exception e) {
            throw new B2BException("Exception while instantiating backup job", e);
        }
        return job;
    }
}
