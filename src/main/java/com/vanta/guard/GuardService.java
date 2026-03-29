package com.vanta.guard;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import java.nio.file.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class GuardService {

    // THE MEMORY BANK: Stores PIDs that the user has already approved
    private final Set<Integer> authorizedPids = new HashSet<>();

    public void startShield(Path directory) throws Exception {
        WatchService watchService = FileSystems.getDefault().newWatchService();

        directory.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);

        System.out.println("VantaGuard Pillar 3: Smart Shield [ACTIVE]");
        System.out.println("Monitoring: " + directory.toAbsolutePath());

        while (true) {
            WatchKey key = watchService.take();
            for (WatchEvent<?> event : key.pollEvents()) {
                Path changedFile = directory.resolve((Path) event.context());

                if (changedFile.toString().endsWith(".vanta") || isLikelyEncrypted(changedFile)) {

                    int pid = ProcessFinder.getPidTouchingFile(changedFile);

                    if (pid > 4 && pid != Kernel32.INSTANCE.GetCurrentProcessId()) {

                        // NEW LOGIC: Check if we already trust this process!
                        if (authorizedPids.contains(pid)) {
                            // Silently allow it to continue without freezing
                            continue;
                        }

                        System.out.println("\n[!] Suspicious Activity Detected on: " + changedFile.getFileName());
                        System.out.println("[*] Scout identified Culprit PID: " + pid);

                        // If not trusted, spring the trap!
                        intercept(pid, changedFile.toString());
                    }
                }
            }
            key.reset();
        }
    }

    private boolean isLikelyEncrypted(Path file) {
        try {
            if (!Files.exists(file) || Files.isDirectory(file)) return false;

            byte[] data;
            try (InputStream in = Files.newInputStream(file)) {
                data = in.readNBytes(2000);
            }

            if (data.length < 100) return false;

            double entropy = 0;
            int[] counts = new int[256];
            for (byte b : data) counts[b & 0xFF]++;

            for (int count : counts) {
                if (count > 0) {
                    double p = (double) count / data.length;
                    entropy -= p * (Math.log(p) / Math.log(2));
                }
            }

            return entropy > 7.5;
        } catch (IOException e) {
            return false;
        }
    }

    private void intercept(int pid, String fileName) {
        System.out.println("[!] Attempting to FREEZE process...");
        HANDLE hProcess = Kernel32.INSTANCE.OpenProcess(0x0801, false, pid);

        if (hProcess == null) {
            System.err.println("[X] Access Denied. Could not hook into PID: " + pid);
            return;
        }

        WindowsAPI.NTDLL.NtSuspendProcess(hProcess);
        System.out.println("[+] Process Frozen in RAM. Waiting for User Input...");

        JFrame alwaysOnTopFrame = new JFrame();
        alwaysOnTopFrame.setAlwaysOnTop(true);
        alwaysOnTopFrame.setLocationRelativeTo(null);

        String message = "VANTA PROJECT ALERT: UNAUTHORIZED ENCRYPTION DETECTED!\n\n" +
                "Process ID: " + pid + "\n" +
                "Detected File: " + fileName + "\n\n" +
                "This process is attempting to encrypt files.\n" +
                "Do you want to ALLOW this process to continue?"; // Updated wording

        int choice = JOptionPane.showConfirmDialog(alwaysOnTopFrame, message,
                "VantaGuard Security Alert", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);

        alwaysOnTopFrame.dispose();

        if (choice == JOptionPane.YES_OPTION) {
            // NEW LOGIC: Add the PID to the memory bank so we don't ask again!
            authorizedPids.add(pid);

            WindowsAPI.NTDLL.NtResumeProcess(hProcess);
            System.out.println("[>] User Authorized. Process Resumed and added to Whitelist.");
        } else {
            WindowsAPI.KERNEL32.TerminateProcess(hProcess, 1);
            System.out.println("[X] THREAT TERMINATED: PID " + pid + " was killed.");
        }

        Kernel32.INSTANCE.CloseHandle(hProcess);
    }
}