package org.ogerardin.b2b.worker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.B2BException;
import org.ogerardin.b2b.domain.BackupSource;
import org.ogerardin.b2b.domain.BackupTarget;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public class BackupWorkerFactory {

    private static final Log logger = LogFactory.getLog(BackupWorkerFactory.class);

    private static List<Class<? extends BackupWorker>> workerClasses = Arrays.asList(
            LocalBackupWorker.class,
            PeerBackupWorker.class
    );

    public static BackupWorker newWorker(BackupSource source, BackupTarget target) throws B2BException {
        logger.debug("Trying to instantiate LocalBackupWorker for " + source + " and " + target);
        for (Class<? extends BackupWorker> workerClass : workerClasses) {
            logger.debug("Trying " + workerClass);
            try {
                Constructor<? extends BackupWorker> workerClassConstructor = workerClass.getConstructor(source.getClass(), target.getClass());
                BackupWorker worker = workerClassConstructor.newInstance(source, target);
                logger.debug("Successfully instantiated " + workerClass);
                return worker;
            } catch (NoSuchMethodException e) {
                logger.debug("No suitable constructor");
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                logger.debug("Exception while invoking constructor", e);
            }
        }
        throw new B2BException("No LocalBackupWorker could be instantiated for "+ source + " and " + target);    }

}
