package org.ogerardin.update.action;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.ogerardin.process.control.ControlException;
import org.ogerardin.process.control.ControlHelper;
import org.ogerardin.process.control.ServiceController;
import org.ogerardin.update.UpdateAction;
import org.ogerardin.update.UpdateContext;
import org.ogerardin.update.UpdateException;

@Data
@AllArgsConstructor
public class StartServiceAction implements UpdateAction {

    private final String serviceName;

    @Override
    public void perform(UpdateContext context) throws UpdateException {
        try {
            ServiceController controller = ControlHelper.getPlatformServiceController(serviceName);
            controller.start();
        } catch (ControlException e) {
            throw new UpdateException("Failed to start service", e);
        }
    }
}
