🛡️ VantaGuard: Real-Time Ransomware Interceptor
VantaGuard is a portable, kernel-aware security engine designed to detect and neutralize ransomware attacks in real-time. By monitoring file system events and analyzing data entropy, VantaGuard can identify unauthorized encryption processes—even if they are "Zero-Day" threats with no known signatures.

🚀 Key Features
Heuristic Entropy Analysis: Uses Shannon Entropy calculations to distinguish between normal file writes and high-entropy cryptographic operations.

Kernel-Level Process Interception: Leverages Java Native Access (JNA) to hook into Windows APIs (Kernel32.dll) to suspend and terminate malicious PIDs instantly.

Smart Whitelisting: Built-in intelligence to ignore common high-entropy files like .zip, .mp4, and .pdf to prevent false positives.

Stealth Background Operation: Runs as a native Windows service with a minimal footprint, accessible via the System Tray.

Portable Deployment: No installation required. Runs as an Administrator-elevated .exe for rapid deployment on high-risk machines.

🛠️ Technical Stack
Language: Java 23 (Modern Syntax)

Libraries: JNA (Java Native Access), JNA-Platform

Build Tool: Maven

Windows Integration: Windows Registry (Preferences API), User Account Control (UAC) Manifests, Win32 API

🛡️ How It Works
Monitor: VantaGuard watches a user-defined directory for ENTRY_MODIFY events.

Analyze: Upon a file change, the engine reads the file header and calculates its entropy level.

Identify: If the entropy exceeds a specific threshold and the extension isn't whitelisted, it's flagged as an "Encryption Event."

Neutralize: VantaGuard uses the Restart Manager API to find which PID is touching the file and executes an immediate Process.destroy() to kill the threat before it can spread.

📥 How to Run
Download the latest VantaGuard.exe from the Releases section.

Run the executable as Administrator.

Select the folder you wish to protect.

VantaGuard will minimize to your System Tray and begin active monitoring.
