package org.ogerardin.b2b.system_tray;

import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.EngineClient;
import org.ogerardin.process.control.ControlException;
import org.ogerardin.process.control.ControlHelper;
import org.ogerardin.process.control.NativeProcessController;
import org.ogerardin.process.control.ServiceController;
import org.ogerardin.update.Release;
import org.ogerardin.update.ReleaseChannel;
import org.ogerardin.update.UpdateException;
import org.ogerardin.update.UpdateManager;
import org.ogerardin.update.channel.GithubReleaseChannel;
import org.ogerardin.update.jar.JarVersionExtractor;
import org.springframework.web.client.RestClientException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.text.MessageFormat;

@Slf4j
public class B2BTrayIcon {

    private static final int POLL_PERIOD_MILLIS = 10000;

    private static EngineClient engineClient;
    private static ServiceController serviceController;
    private static NativeProcessController processController;
    private static UpdateManager updateManager;

    private static TrayIcon trayIcon;
    private static MenuItem openWebUIMenuItem;
    private static MenuItem startMenuItem;
    private static MenuItem stopMenuItem;
    private static CheckboxMenuItem startAutomaticallyMenuItem;

    private static Boolean engineAvailable = null;
    private static Boolean serviceAvailable = null;


    public static void main(String[] args) throws IOException {

        log.info("Starting tray icon...");

        // Hide the dock icon on Mac OS
        System.setProperty("apple.awt.UIElement", "true");

        // initialize engine client. Used to communicate with engine using REST API
        log.info("Engine home directory: {}", Config.getHomeDirectory());
        engineClient = new EngineClient(Config.getHomeDirectory());

        // initialize service controller. Used to control engine autostart and (if available) for manual start/stop
        serviceController = null;
        try {
            serviceController = ControlHelper.getPlatformServiceController(Config.getServiceName());
        } catch (ControlException e) {
            log.error("Error while getting platform service controller", e);
        }
        if (serviceController != null) {
            log.info("Using service controller: {}", serviceController);
        }
        else {
            log.warn("Not using service controller");
        }

        // initialize process controller. Used for manual start/stop when service controller is not available
        Path coreJarPath = Config.getHomeDirectory().resolve(Config.getCoreJar());
        processController = ControlHelper.buildJarProcessController(coreJarPath);

        // initialize update manager
        Path jar = Config.getHomeDirectory().resolve(Config.getCoreJar());
        String version = JarVersionExtractor.getImplementationVersion(jar);
        if (version == null) {
            log.warn("Failed to obtain current version, update manager disabled");
        }
        else {
            ReleaseChannel releaseChannel = new GithubReleaseChannel("ogerardin", "back2back");
            updateManager = UpdateManager.builder()
                    .currentVersion(version)
                    .homeDir(Config.getHomeDirectory())
                    .releaseChannel(releaseChannel)
                    .build();
            log.info("Update manager instantiated: {}", updateManager);
        }

        // set UI options
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
//            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException | ClassNotFoundException ex) {
            log.error("Failed to set look-and-feel", ex);
        }
        // Turn off metal's use of bold fonts
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        // Schedule GUI construction for the event-dispatching thread
        SwingUtilities.invokeLater(B2BTrayIcon::createAndShowGUI);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {
        }
    }

    private static void createAndShowGUI() {
        // Check the SystemTray support
        if (!SystemTray.isSupported()) {
            log.error("SystemTray is not supported");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();

        Image icon = createImage("/images/b2b.gif", "tray icon");
        trayIcon = new TrayIcon(icon);
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("back2back");

        PopupMenu popupMenu = new PopupMenu();
        {
            openWebUIMenuItem = new MenuItem("Open back2back web interface");
            openWebUIMenuItem.addActionListener(B2BTrayIcon::openWebUI);
            popupMenu.add(openWebUIMenuItem);
        }
        popupMenu.addSeparator();
        {
            startAutomaticallyMenuItem = new CheckboxMenuItem("Start automatically with system");
            startAutomaticallyMenuItem.addItemListener(e -> {
                int newState = e.getStateChange();
                setAutoStart(newState == ItemEvent.SELECTED);
            });
            startAutomaticallyMenuItem.setEnabled(serviceController != null);
            popupMenu.add(startAutomaticallyMenuItem);
        }
        popupMenu.addSeparator();
        {
            startMenuItem = new MenuItem("Start back2back engine");
            startMenuItem.setEnabled(false);
            startMenuItem.addActionListener(evt -> {
                try {
                    startEngine();
                } catch (ControlException e) {
                    log.error("Failed to start engine", e);
                    trayIcon.displayMessage("back2back", "Failed to start engine: " + e.toString(), TrayIcon.MessageType.ERROR);
                }
            });
            popupMenu.add(startMenuItem);
        }
        {
            stopMenuItem = new MenuItem("Stop back2back engine");
            stopMenuItem.setEnabled(false);
            stopMenuItem.addActionListener(evt -> {
                try {
                    stopEngine();
                } catch (ControlException e) {
                    trayIcon.displayMessage("back2back", "Failed to stop engine: " + e.toString(), TrayIcon.MessageType.ERROR);
                }
            });
            popupMenu.add(stopMenuItem);
        }
        popupMenu.addSeparator();
        {
            MenuItem item = new MenuItem("About");
            item.addActionListener(B2BTrayIcon::about);
            popupMenu.add(item);
        }
        {
            MenuItem item = new MenuItem("Check for update");
            item.setEnabled(updateManager != null);
            item.addActionListener(B2BTrayIcon::checkForUpdate);
            popupMenu.add(item);
        }
        {
            MenuItem item = new MenuItem("Close tray icon");
            item.addActionListener(e -> {
                tray.remove(trayIcon);
                System.exit(0);
            });
            popupMenu.add(item);
        }

//        popupMenu.addActionListener(e -> log.debug("POPUP ACTION"));
        trayIcon.setPopupMenu(popupMenu);

        trayIcon.addActionListener(e -> log.debug("TRAY ACTION"));

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            log.error("TrayIcon could not be added.", e);
            return;
        }

        //trayIcon.displayMessage("back2back", "Tray icon ready", TrayIcon.MessageType.INFO);

        // start status update on background thread
        Thread thread = new Thread(B2BTrayIcon::pollStatus);
        thread.setDaemon(true);
        thread.start();

        log.info("Tray icon ready.");
    }

    private static void openWebUI(ActionEvent actionEvent) {
        String baseUrl = engineClient.getBaseUrl();
        try {
            Desktop.getDesktop().browse(new URI(baseUrl));
        } catch (IOException | URISyntaxException e) {
            log.error(String.format("Failed to open %s", baseUrl), e);
        }
    }

    private static void startEngine() throws ControlException {
        if (serviceController != null && serviceAvailable) {
            log.debug("[service controller] starting engine");
            serviceController.start();
        }
        else {
            log.debug("Service controller not available, using process controller to start engine");

            //handle processController events
            processController.setProcessListener(evt -> {
                log.error("Process terminated: {}", evt);
                trayIcon.displayMessage("Engine exited prematurely", evt.toString(), TrayIcon.MessageType.ERROR);
            });

            processController.start();
        }
    }

    private static void stopEngine() throws ControlException {
        if (serviceController != null && serviceAvailable && serviceController.isRunning()) {
            log.debug("[service controller] stopping engine...");
            serviceController.stop();
        }
        else {
            processController.setProcessListener(null);
            log.debug("Shutting down engine...");
            // to exit cleanly we call the shutdown API
            engineClient.shutdown();
        }


    }

    private static void pollStatus() {
        //noinspection InfiniteLoopStatement
        while (true) {
            // check connectivity with engine
            try {
                String engineStatus = engineClient.apiStatus();
                if (engineAvailable == null || !engineAvailable) {
                    // only log after first check or if previous status was unavailable
                    log.debug("Engine available, API status: {}", engineStatus);
                    trayIcon.displayMessage("back2back Engine is available", null, TrayIcon.MessageType.INFO);
                }
                engineAvailable = true;
            } catch (RestClientException e) {
                if (engineAvailable == null || engineAvailable) {
                    // only log after first check or if previous status was available
                    log.error("Engine API not available: {}", e.toString());
                    trayIcon.displayMessage("back2back Engine is not available", null, TrayIcon.MessageType.WARNING);
                }
                engineAvailable = false;
            }

            // enable or disable actions accordingly
            openWebUIMenuItem.setEnabled(engineAvailable);
            startMenuItem.setEnabled(!engineAvailable);
            stopMenuItem.setEnabled(engineAvailable);

            // check service status
            if (serviceController != null) {
                serviceAvailable = false;
                boolean serviceAutostart = false;
                try {
                    serviceAvailable = serviceController.isInstalled();
                    if (serviceAvailable) {
                        serviceAutostart = serviceController.isAutostart();
                    }
                } catch (ControlException e) {
                    log.error("Failed to get service status: {}", e.toString());
                }
                startAutomaticallyMenuItem.setState(serviceAutostart);
            }
            startAutomaticallyMenuItem.setEnabled(serviceAvailable);

            try {
                Thread.sleep(POLL_PERIOD_MILLIS);
            } catch (InterruptedException e) {
                log.info("Interrupted");
            }
        }
    }


    private static void setAutoStart(boolean autoStart) {
        try {
            serviceController.setAutostart(autoStart);
        } catch (ControlException e) {
            e.printStackTrace();
        }
    }

    //Obtain the image URL
    private static Image createImage(String path, String description) {
        URL imageURL = B2BTrayIcon.class.getResource(path);
        if (imageURL == null) {
            throw new RuntimeException("Resource not found: " + path);
        }
        return new ImageIcon(imageURL, description).getImage();
    }

    private static void about(ActionEvent evt) {
        try {
            String version = engineClient.version();
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format("back2back: peer to peer backup\nEngine version {0} up and running", version));
        } catch (RestClientException e1) {
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format("back2back: peer to peer backup\nEngine is not running\n{0}", e1.toString()));
        }

    }

    private static void checkForUpdate(ActionEvent evt) {
        Release update = updateManager.checkForUpdate();
        if (update == null) {
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format("You have version {0}\nYou have the latest version.",
                            updateManager.getCurrentVersion()),
                    "No update available", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(null,
                MessageFormat.format("You have version {0}\nLatest version is {1}\n\nUpdate now?",
                        updateManager.getCurrentVersion(),
                        update.getVersion()
                ), "Update available!", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                updateManager.update(update);
            } catch (IOException | UpdateException | InterruptedException e) {
                log.error("Update failed", e);
            }
        }
    }
}
