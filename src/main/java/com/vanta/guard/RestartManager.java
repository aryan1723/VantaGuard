package com.vanta.guard;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinBase.FILETIME;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import java.util.List;

public interface RestartManager extends StdCallLibrary {
    int CCH_RM_MAX_APP_NAME = 255;
    int CCH_RM_MAX_SVC_NAME = 63;
    int CCH_RM_SESSION_KEY = 32;

    RestartManager INSTANCE = Native.load("Rstrtmgr", RestartManager.class, W32APIOptions.UNICODE_OPTIONS);

    int RmStartSession(IntByReference sessionHandle, int sessionFlags, char[] sessionKey);

    int RmRegisterResources(
            int sessionHandle,
            int fileCount,
            String[] fileNames,
            int applicationCount,
            RM_UNIQUE_PROCESS[] applications,
            int serviceCount,
            String[] serviceNames
    );

    int RmGetList(
            int sessionHandle,
            IntByReference procInfoNeeded,
            IntByReference procInfoCount,
            RM_PROCESS_INFO[] affectedApps,
            IntByReference rebootReasons
    );

    int RmEndSession(int sessionHandle);

    class RM_UNIQUE_PROCESS extends Structure {
        public int dwProcessId;
        public FILETIME ProcessStartTime;

        @Override
        protected List<String> getFieldOrder() {
            return List.of("dwProcessId", "ProcessStartTime");
        }
    }

    class RM_PROCESS_INFO extends Structure {
        public RM_UNIQUE_PROCESS Process;
        public char[] strAppName = new char[CCH_RM_MAX_APP_NAME + 1];
        public char[] strServiceShortName = new char[CCH_RM_MAX_SVC_NAME + 1];
        public int ApplicationType;
        public int AppStatus;
        public int TSSessionId;
        public int bRestartable;

        @Override
        protected List<String> getFieldOrder() {
            return List.of(
                    "Process",
                    "strAppName",
                    "strServiceShortName",
                    "ApplicationType",
                    "AppStatus",
                    "TSSessionId",
                    "bRestartable"
            );
        }
    }
}
