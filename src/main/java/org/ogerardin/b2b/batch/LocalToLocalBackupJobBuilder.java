package org.ogerardin.b2b.batch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.B2BException;
import org.ogerardin.b2b.domain.*;
import org.ogerardin.b2b.backup.SingleFileProcessor;
import org.springframework.batch.core.Job;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.File;

@Component
public class LocalToLocalBackupJobBuilder extends BackupJobBuilderBase implements BackupJobBuilder {

    private static final Log logger = LogFactory.getLog(LocalToLocalBackupJobBuilder.class);

    @Override
    public Job newBackupJob(BackupSource source, BackupTarget target) throws B2BException {
        Assert.isInstanceOf(FilesystemSource.class, source);
        Assert.isInstanceOf(LocalTarget.class, target);
        Job job;
        try {
            job = newBackupJob(getClass().getSimpleName(), new SingleFileProcessor() {
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
