import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.ogerardin.process.control.ControlException;
import org.ogerardin.process.control.ControlHelper;
import org.ogerardin.process.control.ServiceController;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Data
public class UpdatePerformer {

    private final Path updateSourceDir;
    private final Path targetDir;

    public static void main(String[] args) throws ControlException {
        Path updateSourceDir = Paths.get(args[0]);
        Path targetDir = Paths.get(args[1]);
        UpdatePerformer updater = new UpdatePerformer(updateSourceDir, targetDir);
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
