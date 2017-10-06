package org.ogerardin.b2b.batch;

import org.ogerardin.b2b.domain.FilesystemSource;
import org.ogerardin.b2b.util.Maps;
import org.springframework.batch.core.JobParametersValidator;

public class LocalSourceBackupJob extends BackupJob {

    private static final BatchJobParametersValidator PARAMETERS_VALIDATOR = new BatchJobParametersValidator(
            new String[]{"source.root"},
            new String[]{},
            Maps.mapOf(
                    "source.type", FilesystemSource.class.getName()
            )
    );

    protected JobParametersValidator validator() {
        return PARAMETERS_VALIDATOR;
    }

}
