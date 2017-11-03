/*
 * Copyright (c) 2017 Olivier Gérardin
 */

/**
 * @author Olivier
 * @since 28/05/15
 */

import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.application.Application;
import javafx.stage.Stage;
import org.ogerardin.b2b.domain.FilesystemSource;

public class Main extends Application {

    private static FilesystemSource domainManager;

    @Override
    public void start(Stage primaryStage) {

        // instantiate UiManager for JavaFX
        JfxUiManager uiManager = new JfxUiManager();

        // build UI for a DomainManager and bind it to actual instance
        JfxInstanceUI<FilesystemSource> ui = uiManager.buildInstanceUI(FilesystemSource.class);
        ui.bind(domainManager);

        //display UI in JavaFX primary Stage
        uiManager.display(ui, primaryStage, "back2back");
    }

    public static void main(String[] args) {

        // instantiate our main business object
        domainManager = new FilesystemSource();

        // handoff to JavaFX; this will call the start() method
        launch(args);
    }
}

