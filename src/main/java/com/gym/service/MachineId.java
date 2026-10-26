package com.gym.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * The "Machine ID" of the computer the application runs on, e.g. 3F9A-0C72-B1D4-5E88.
 *
 * It is a hash of the operating system's own machine id (Windows MachineGuid, Linux /etc/machine-id,
 * macOS IOPlatformUUID). Only if none of these can be read, the hardware (MAC) addresses are used.
 * The value does not change when the network card, Wi-Fi or VPN changes, but it is different on
 * another computer, which is what a license tied to a computer needs.
 */
public final class MachineId {

    private static final Pattern FORMAT = Pattern.compile("^[0-9A-F]{4}(-[0-9A-F]{4}){3}$");
    private static volatile String cached;

    private MachineId() {
    }

    /** The Machine ID of this computer (computed once). */
    public static String current() {
        String id = cached;
        if (id == null) {
            id = compute();
            cached = id;
        }
        return id;
    }

    /** Upper-cases and trims an ID typed by a person; returns null if it does not look like an ID. */
    public static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim().toUpperCase(Locale.ROOT);
        return FORMAT.matcher(v).matches() ? v : null;
    }

    static String compute() {
        String source = osMachineId();
        if (source == null || source.isBlank()) {
            source = "mac:" + hardwareAddresses();
        }
        return format(source);
    }

    static String format(String source) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            String hex = HexFormat.of().withUpperCase()
                    .formatHex(sha.digest(("gym-system|" + source.trim().toLowerCase(Locale.ROOT))
                            .getBytes(StandardCharsets.UTF_8)));
            return hex.substring(0, 4) + "-" + hex.substring(4, 8) + "-" + hex.substring(8, 12) + "-" + hex.substring(12, 16);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String osMachineId() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        try {
            if (os.contains("win")) {
                String out = run("reg", "query", "HKLM\\SOFTWARE\\Microsoft\\Cryptography", "/v", "MachineGuid");
                for (String line : out.split("\\R")) {
                    if (line.contains("MachineGuid")) {
                        String[] parts = line.trim().split("\\s+");
                        return parts[parts.length - 1];
                    }
                }
            } else if (os.contains("mac")) {
                String out = run("ioreg", "-rd1", "-c", "IOPlatformExpertDevice");
                for (String line : out.split("\\R")) {
                    if (line.contains("IOPlatformUUID")) {
                        return line.substring(line.indexOf('=') + 1).replace("\"", "").trim();
                    }
                }
            } else {
                for (String file : new String[]{"/etc/machine-id", "/var/lib/dbus/machine-id"}) {
                    Path p = Path.of(file);
                    if (Files.isReadable(p)) {
                        String id = Files.readString(p).trim();
                        if (!id.isEmpty()) {
                            return id;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // fall through to the MAC address fallback
        }
        return null;
    }

    private static String run(String... command) throws Exception {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        StringBuilder out = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line).append('\n');
            }
        }
        if (!process.waitFor(5, TimeUnit.SECONDS)) {
            process.destroyForcibly();
        }
        return out.toString();
    }

    private static String hardwareAddresses() {
        List<String> macs = new ArrayList<>();
        try {
            for (NetworkInterface nic : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                byte[] mac = nic.getHardwareAddress();
                if (nic.isLoopback() || nic.isVirtual() || mac == null || mac.length != 6) {
                    continue;
                }
                macs.add(HexFormat.of().formatHex(mac));
            }
        } catch (Exception e) {
            // no network information available
        }
        Collections.sort(macs);
        return String.join(",", macs);
    }
}
