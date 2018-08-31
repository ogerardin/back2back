package org.ogerardin.b2b.update;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.control.ControlHelper;
import org.ogerardin.processcontrol.ControlException;
import org.ogerardin.processcontrol.ServiceController;

@Slf4j
@Data
public class UpdatePerformerr {

    public static void main(String[] args) throws ControlException {
        UpdatePerformerr updater = new UpdatePerformerr();
        updater.run();
    }

    private void run() throws ControlException {
        ServiceController controller = ControlHelper.getPlatformServiceController();

        log.info("Stopping service {}", controller);
        controller.stop();

        //TODO copy new version in place of core jar

        log.info("Restarting service {}", controller);
        controller.start();
    }

}
