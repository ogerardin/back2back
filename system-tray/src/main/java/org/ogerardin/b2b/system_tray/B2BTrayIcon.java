package org.ogerardin.b2b.system_tray;

import lombok.extern.slf4j.Slf4j;
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

    private static MenuItem startMenuItem;
    private static MenuItem stopMenuItem;

    private static EnginePilot enginePilot;

    public static void main(String[] args) throws IOException {

        String installDir = System.getProperty("installDir");
        if (installDir == null) {
            installDir = ".";
        }
        enginePilot = new EnginePilot(Paths.get(installDir));

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
//            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        // Turn off metal's use of bold fonts
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        // Schedule a job for the event-dispatching thread: adding TrayIcon.
        SwingUtilities.invokeLater(B2BTrayIcon::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        // Check the SystemTray support
        if (!SystemTray.isSupported()) {
            throw new RuntimeException("SystemTray is not supported");
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
            throw new RuntimeException("TrayIcon could not be added.");
        }

        trayIcon.displayMessage("back2back", "Tray icon ready", TrayIcon.MessageType.INFO);

        // update status on background thread
        Thread thread = new Thread(B2BTrayIcon::getEngineStatus);
        thread.setDaemon(true);
        thread.start();
    }

    private static void getEngineStatus() {
        try {
            String engineStatus = enginePilot.getEngineStatus();
            engineAvailable(true);
        } catch (RestClientException e1) {
            engineAvailable(false);
        }
    }

    private static void engineAvailable(boolean available) {
        startMenuItem.setEnabled(!available);
        stopMenuItem.setEnabled(available);
    }

    private static void setAutoStart(boolean autoStart) {
        //TODO
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
            String version = enginePilot.version();
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format("back2back: peer to peer backup\nEngine version {0} up and running", version));
        } catch (RestClientException e1) {
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format("back2back: peer to peer backup\nEngine is not running\n{0}", e1.toString()));
        }

    }
}
