package org.ogerardin.b2b.system_tray;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.net.URL;

public class B2BTrayIcon {

    public static void main(String[] args) {
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
            item.addActionListener(e -> JOptionPane.showMessageDialog(null,
                    "back2back: peer to peer backup"));
        }
        popup.addSeparator();
        {
            CheckboxMenuItem item = new CheckboxMenuItem("Start with Windows");
            popup.add(item);
            item.addItemListener(e -> {
                int newState = e.getStateChange();
                setAutoStart(newState == ItemEvent.SELECTED);
            });
        }
        popup.addSeparator();
        {
            MenuItem item = new MenuItem("Start back2back engine");
            popup.add(item);
        }
        {
            MenuItem item = new MenuItem("Stop back2back engine");
            popup.add(item);
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

    }

    private static void setAutoStart(boolean autoStart) {
        //TODO
    }

    //Obtain the image URL
    protected static Image createImage(String path, String description) {
        URL imageURL = B2BTrayIcon.class.getResource(path);
        if (imageURL == null) {
            throw new RuntimeException("Resource not found: " + path);
        }
        return new ImageIcon(imageURL, description).getImage();
    }
}
