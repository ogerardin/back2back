import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.ogerardin.process.control.ControlHelper;
import org.ogerardin.process.control.ControlException;
import org.ogerardin.process.control.ServiceController;

@Slf4j
@Data
public class UpdatePerformer {

    public static void main(String[] args) throws ControlException {
        UpdatePerformer updater = new UpdatePerformer();
        updater.run();
    }

    private void run() throws ControlException {
        ServiceController controller = ControlHelper.getPlatformServiceController("back2back");

        log.info("Stopping service {}", controller);
        controller.stop();

        //TODO update files (scripting??)

        log.info("Restarting service {}", controller);
        controller.start();
    }

}
