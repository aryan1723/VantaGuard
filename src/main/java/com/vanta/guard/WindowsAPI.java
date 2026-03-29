package com.vanta.guard;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.win32.StdCallLibrary;

public interface WindowsAPI extends StdCallLibrary {
    // Load ntdll and kernel32 to access system-level commands
    WindowsAPI NTDLL = Native.load("ntdll", WindowsAPI.class);
    WindowsAPI KERNEL32 = Native.load("kernel32", WindowsAPI.class);

    // Methods to Pause, Resume, and Terminate
    int NtSuspendProcess(HANDLE hProcess);
    int NtResumeProcess(HANDLE hProcess);
    boolean TerminateProcess(HANDLE hProcess, int uExitCode);
}