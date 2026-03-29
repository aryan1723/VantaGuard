package com.vanta.guard;

import com.sun.jna.platform.win32.WinError;
import com.sun.jna.ptr.IntByReference;

import java.nio.file.Path;

public class ProcessFinder {

    public static int getPidTouchingFile(Path file) {
        RestartManager rm = RestartManager.INSTANCE;
        IntByReference sessionHandle = new IntByReference();
        char[] sessionKey = new char[RestartManager.CCH_RM_SESSION_KEY + 1];

        // Start a Windows Restart Manager session
        if (rm.RmStartSession(sessionHandle, 0, sessionKey) != WinError.ERROR_SUCCESS) return -1;

        try {
            String[] files = { file.toAbsolutePath().toString() };
            // Register the specific file to see who is using it
            int registerResult = rm.RmRegisterResources(sessionHandle.getValue(), files.length, files, 0, null, 0, null);
            if (registerResult != WinError.ERROR_SUCCESS) return -1;

            IntByReference procInfoNeeded = new IntByReference();
            IntByReference procInfoCount = new IntByReference();
            IntByReference rebootReasons = new IntByReference();

            // First call to check how many processes are touching the file
            int listResult = rm.RmGetList(sessionHandle.getValue(), procInfoNeeded, procInfoCount, null, rebootReasons);

            if (listResult == WinError.ERROR_MORE_DATA && procInfoNeeded.getValue() > 0) {
                procInfoCount.setValue(procInfoNeeded.getValue());
                RestartManager.RM_PROCESS_INFO[] procInfos =
                        (RestartManager.RM_PROCESS_INFO[]) new RestartManager.RM_PROCESS_INFO().toArray(procInfoNeeded.getValue());

                // Second call to actually get the process list
                int res = rm.RmGetList(sessionHandle.getValue(), procInfoNeeded, procInfoCount, procInfos, rebootReasons);

                if (res == WinError.ERROR_SUCCESS && procInfoCount.getValue() > 0) {
                    // Return the Process ID (PID) of the first application found
                    return procInfos[0].Process.dwProcessId;
                }
            }
        } finally {
            // Always close the session
            rm.RmEndSession(sessionHandle.getValue());
        }
        return -1;
    }
}
