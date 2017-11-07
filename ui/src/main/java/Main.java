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
import java.util.Collection;

public class Main extends Application {

    private static Collection<BackupSet> backupSets;

    @Override
    public void start(Stage primaryStage) {

        // instantiate UiManager for JavaFX
        JfxUiManager uiManager = new JfxUiManager();

        // build UI for a DomainManager and bind it to actual instance
        JfxCollectionUI<BackupSet> ui = uiManager.buildCollectionUi(BackupSet.class);
        ui.bind(backupSets);

        //display UI in JavaFX primary Stage
        uiManager.display(ui, primaryStage, "back2back");
    }

    public static void main(String[] args) {

        // instantiate our main business object
        B2BfacadeRest facade = new B2BfacadeRest();
        backupSets = Arrays.asList(facade.getBackupSets());

        // handoff to JavaFX; this will call the start() method
        launch(args);
    }
}

