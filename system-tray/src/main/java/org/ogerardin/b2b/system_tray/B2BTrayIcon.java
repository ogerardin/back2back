package org.ogerardin.b2b.system_tray;

import com.sun.jna.Platform;
import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.system_tray.processcontrol.ControlException;
import org.ogerardin.b2b.system_tray.processcontrol.MacLaunchctlDaemonController;
import org.ogerardin.b2b.system_tray.processcontrol.ServiceController;
import org.ogerardin.b2b.system_tray.processcontrol.WindowsNssmServiceController;
import org.springframework.web.client.RestClientException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.text.MessageFormat;

@Slf4j
public class B2BTrayIcon {

    private static final int POLL_PERIOD_MILLIS = 10000;

    private static final String WINDOWS_SERVICE_NAME = "back2back";
    private static final String MAC_JOB_NAME = "back2back";

    private static MenuItem startMenuItem;
    private static MenuItem stopMenuItem;

    private static EngineControl engineControl;

    private static ServiceController serviceController;

    public static void main(String[] args) throws IOException {

        log.info("Starting tray icon...");

        // Hide the dock icon on Mac OS
        System.setProperty("apple.awt.UIElement", "true");

        String installDir = System.getProperty("installDir");
        if (installDir == null) {
            installDir = ".";
        }
        engineControl = new EngineControl(Paths.get(installDir));

        serviceController = getPlatformServiceController();
        if (serviceController != null) {
            log.info("Using service controller: {}", serviceController);
            try {
                // check access
                String controllerInfo = serviceController.getControllerInfo();
                log.info("Service controller information: {}", controllerInfo);
            } catch (ControlException e) {
                log.error("Exception accessing service controller", e);
                serviceController = null;
            }
        }
        if (serviceController == null) {
            log.warn("No service controller available");
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
//            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException | ClassNotFoundException ex) {
            log.error("Failed to set look-and-feel", ex);
        }
        // Turn off metal's use of bold fonts
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        // Schedule a job for the event-dispatching thread: adding TrayIcon.
        SwingUtilities.invokeLater(B2BTrayIcon::createAndShowGUI);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {
        }
    }

    private static ServiceController getPlatformServiceController() {
        switch (Platform.getOSType()) {
            case Platform.WINDOWS:
                return new WindowsNssmServiceController(WINDOWS_SERVICE_NAME);
            case Platform.MAC:
                return new MacLaunchctlDaemonController(MAC_JOB_NAME);
        }
        log.warn("No service controller available for platform");
        return null;
    }

    private static void createAndShowGUI() {
        // Check the SystemTray support
        if (!SystemTray.isSupported()) {
            log.error("SystemTray is not supported");
            return;
        }

        final SystemTray tray = SystemTray.getSystemTray();

        Image icon = createImage("/images/b2b.gif", "tray icon");
        final TrayIcon trayIcon = new TrayIcon(icon);
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("back2back");

        final PopupMenu popup = new PopupMenu();
        {
            MenuItem item = new MenuItem("About");
            popup.add(item);
            item.addActionListener(B2BTrayIcon::about);
        }
        popup.addSeparator();
        {
            CheckboxMenuItem item = new CheckboxMenuItem("Start automatically with system");
            popup.add(item);
            item.addItemListener(e -> {
                int newState = e.getStateChange();
                setAutoStart(newState == ItemEvent.SELECTED);
            });
            item.setEnabled(serviceController != null);
        }
        popup.addSeparator();
        {
            startMenuItem = new MenuItem("Start back2back engine");
            startMenuItem.setEnabled(false);
            popup.add(startMenuItem);
        }
        {
            stopMenuItem = new MenuItem("Stop back2back engine");
            stopMenuItem.setEnabled(false);
            popup.add(stopMenuItem);
        }
        popup.addSeparator();
        {
            MenuItem item = new MenuItem("Hide tray icon");
            popup.add(item);
            item.addActionListener(e -> {
                tray.remove(trayIcon);
                System.exit(0);
            });
        }

        trayIcon.setPopupMenu(popup);

        trayIcon.addActionListener(e -> JOptionPane.showMessageDialog(null,
                "Double-click"));

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            log.error("TrayIcon could not be added.", e);
            return;
        }

        //trayIcon.displayMessage("back2back", "Tray icon ready", TrayIcon.MessageType.INFO);

        // start status update on background thread
        Thread thread = new Thread(B2BTrayIcon::pollEngineApiStatus);
        thread.setDaemon(true);
        thread.start();

        log.info("Tray icon ready.");
    }

    private static void pollEngineApiStatus() {
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                String engineStatus = engineControl.apiStatus();
                log.debug("Engine API status: {}", engineStatus);
                engineAvailable(true);
            } catch (RestClientException e) {
                log.debug("Engine API not available: {}", e.toString());
                engineAvailable(false);
            }

            try {
                Thread.sleep(POLL_PERIOD_MILLIS);
            } catch (InterruptedException e) {
                log.info("Interrupted");
            }
        }
    }

    private static void engineAvailable(boolean available) {
        startMenuItem.setEnabled(!available);
        stopMenuItem.setEnabled(available);
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

    private static void about(ActionEvent e) {
        try {
            String version = engineControl.version();
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format("back2back: peer to peer backup\nEngine version {0} up and running", version));
        } catch (RestClientException e1) {
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format("back2back: peer to peer backup\nEngine is not running\n{0}", e1.toString()));
        }

    }
}
