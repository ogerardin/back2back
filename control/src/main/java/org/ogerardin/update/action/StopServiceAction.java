package org.ogerardin.update.action;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.ogerardin.process.control.ControlException;
import org.ogerardin.process.control.ControlHelper;
import org.ogerardin.process.control.ServiceController;
import org.ogerardin.update.UpdateAction;
import org.ogerardin.update.UpdateException;

@Data
@AllArgsConstructor
public class StopServiceAction implements UpdateAction {

    private final String serviceName;

    @Override
    public void perform(UpdateContext context) throws UpdateException {
        try {
            ServiceController controller = ControlHelper.getPlatformServiceController(serviceName);
            controller.stop();
        } catch (ControlException e) {
            throw new UpdateException("Failed to stop service", e);
        }
    }
}
