package org.ogerardin.b2b.batch;

import org.ogerardin.b2b.util.MetaInvocationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class BackupBuilderInvocationHandler extends MetaInvocationHandler<BackupJobBuilder> {

    @Autowired
    public BackupBuilderInvocationHandler(LocalToPeerBackupJobBuilder localToPeerBackupJobBuilder,
                                          LocalToLocalBackupJobBuilder localToLocalBackupJobBuilder) {
        super(
                Arrays.asList(
                        localToPeerBackupJobBuilder,
                        localToLocalBackupJobBuilder
                ));
    }

}
