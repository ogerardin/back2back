/*
 * Copyright (c) 2017 Olivier Gérardin
 */

/**
 * @author Olivier
 * @since 28/05/15
 */

import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.ui.JfxCollectionUI;
import javafx.application.Application;
import javafx.stage.Stage;
import org.ogerardin.b2b.domain.BackupSet;

import java.util.Arrays;

public class MainJfx extends Application {

    private static B2Bfacade b2Bfacade;

    @Override
    public void start(Stage primaryStage) {

        // instantiate UiManager for JavaFX
        JfxUiManager uiManager = new JfxUiManager();

        // build UI for a DomainManager and bind it to actual instance
        JfxCollectionUI<BackupSet> ui = uiManager.buildCollectionUi(BackupSet.class);
        BackupSet[] backupSets = b2Bfacade.getBackupSets();
        ui.bind(Arrays.asList(backupSets));

        //display UI in JavaFX primary Stage
        uiManager.display(ui, primaryStage, "back2back");
    }

    public static void main(String[] args) {

        // instantiate our main business object
        b2Bfacade = new B2BfacadeRest();

        // handoff to JavaFX; this will call the start() method
        launch(args);
    }
}

