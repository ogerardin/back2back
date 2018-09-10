import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.ogerardin.update.UpdatePerformer;
import org.ogerardin.update.action.ReplaceFileAction;
import org.ogerardin.update.action.StartServiceAction;
import org.ogerardin.update.action.StopServiceAction;
import org.ogerardin.update.action.UpdateContext;

@Slf4j
@Data
public class Updater {

    public static void main(String[] args) {
        UpdateContext context = new UpdateContext(args);

        UpdatePerformer updatePerformer = UpdatePerformer.builder()
                .context(context)
                .step(new StopServiceAction("back2back"))
                .step(new ReplaceFileAction("back2back-bundle-repackaged.jar"))
                .step(new StartServiceAction("back2back"))
                .build();

        updatePerformer.run();

    }

}
