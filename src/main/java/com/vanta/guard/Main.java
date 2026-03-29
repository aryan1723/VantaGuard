package com.vanta.guard;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Paths;
import java.util.prefs.Preferences;
import java.io.File;

public class Main {
    // This acts as VantaGuard's memory bank in the OS
    private static final String PREF_KEY = "VantaGuard_Target_Folder";
    private static final Preferences prefs = Preferences.userNodeForPackage(Main.class);

    public static void main(String[] args) {
        // --------------------------------------------------

        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}

        String savedFolder = prefs.get(PREF_KEY, null);

        if (savedFolder == null || !new File(savedFolder).exists()) {
            savedFolder = runFirstTimeSetup();
            if (savedFolder == null) {
                System.out.println("Setup cancelled. Exiting.");
                System.exit(0);
            }
            prefs.put(PREF_KEY, savedFolder);
        }

        setupSystemTray();

        GuardService guard = new GuardService();
        try {
            guard.startShield(Paths.get(savedFolder));
        } catch (Exception e) {
            System.err.println("FATAL ERROR IN SHIELD: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "VantaGuard Engine Error:\n" + e.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private static String runFirstTimeSetup() {
        JOptionPane.showMessageDialog(null,
                "Welcome to VantaGuard Security.\n\nPlease select the folder you want to protect from Ransomware.",
                "VantaGuard Initial Setup", JOptionPane.INFORMATION_MESSAGE);

        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(System.getProperty("user.home")));
        chooser.setDialogTitle("Select Folder to Protect");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    private static void setupSystemTray() {
        if (!SystemTray.isSupported()) return;

        // Create a temporary Red Square icon for the taskbar (You can replace this with a real .png logo later)
        Image image = new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, 16, 16);
        g2d.dispose();

        PopupMenu popup = new PopupMenu();

        MenuItem changeFolderItem = new MenuItem("Change Protected Folder");
        changeFolderItem.addActionListener(e -> {
            // Erase the memory and kill the app so it asks again next time
            prefs.remove(PREF_KEY);
            JOptionPane.showMessageDialog(null, "Folder reset. Please restart VantaGuard to select a new folder.", "VantaGuard", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        });

        MenuItem exitItem = new MenuItem("Exit VantaGuard");
        exitItem.addActionListener(e -> {
            System.out.println("VantaGuard shutting down...");
            System.exit(0);
        });

        popup.add(changeFolderItem);
        popup.addSeparator();
        popup.add(exitItem);

        TrayIcon trayIcon = new TrayIcon(image, "VantaGuard Security [Active]", popup);
        trayIcon.setImageAutoSize(true);

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            System.err.println("TrayIcon could not be added.");
        }
    }
}