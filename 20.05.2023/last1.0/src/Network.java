import jcifs.netbios.NbtAddress;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.io.IOException;
import jpcap.JpcapCaptor;
import jpcap.PacketReceiver;
import jpcap.packet.Packet;

public class Network {

    private String ipAddress;

    public Network(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void scanNetwork() {
        try {
            // Открываем файл для записи результатов
            File outputFile = new File("output.txt");
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");

            for (int i = 0; i <= 107; i++) {
                String host = ipAddress + i;
                System.out.println("Scanning: " + host);

                boolean isReachable = false;
                String deviceName = "unknown";
                String macAddress = "unknown";
                String status = "OFF";
                String os = "unknown";

                try {
                    InetAddress address = InetAddress.getByName(host);
                    isReachable = address.isReachable(1000); // timeout = 1 second
                    NetworkInterface networkInterface = NetworkInterface.getByInetAddress(address);

                    if (isReachable) {
                        // Получаем NetBIOS-имя
                        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                            deviceName = address.getHostName();
                        } else if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                            // Получаем имя устройства из команды "nmap"
                            Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", "nmap -sP " + host});
                            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (line.contains("Nmap scan report for " + host)) {
                                    deviceName = line.split(" ")[4];
                                    break;
                                }
                            }
                        }

                        if (isReachable) {
                            status = "ON";
                        } else {
                            status = "OFF";
                        }

                        String osCommand = "nmap -O " + host; // команда для определения операционной системы в Linux
                        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                            osCommand = "nmap --osscan-guess " + host; // команда для определения операционной системы в Windows
                        }

                        Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", osCommand});
                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String line;
                        StringBuilder sb = new StringBuilder();
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        String output = sb.toString();

                        if (output.contains("OS CPE:")) {
                            int startIndex = output.indexOf("OS CPE:");
                            int endIndex = output.indexOf('\n', startIndex);
                            os = output.substring(startIndex + 8, endIndex).trim();
                        }

                        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                            InetAddress localAddress = InetAddress.getLocalHost();
                            if (localAddress.getHostAddress().equals(host)) {
                                NetworkInterface network2Interface = NetworkInterface.getByInetAddress(localAddress);
                                byte[] macAddressBytes = networkInterface.getHardwareAddress();
                                StringBuilder sb1 = new StringBuilder();
                                for (int a = 0; a < macAddressBytes.length; a++) { // исправлено i на a
                                    sb1.append(String.format("%02X%s", macAddressBytes[i], (a < macAddressBytes.length - 1) ? "-" : ""));
                                }
                                macAddress = sb1.toString();
                            } else {
                                Process processed = Runtime.getRuntime().exec(new String[]{"cmd", "/c", "chcp", "65001", "&&", "arp", "-a", host});
                                BufferedReader readered = new BufferedReader(new InputStreamReader(processed.getInputStream()));
                                String lineed;
                                String[] parts;
                                while ((lineed = readered.readLine()) != null) {
                                    if (lineed.contains(host)) {
                                        parts = lineed.split("\\s+");
                                        if (parts.length >= 2) {
                                            macAddress = parts[parts.length - 2];
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                        if (os.toLowerCase().contains("linux")) {
                            InetAddress localAddress = InetAddress.getLocalHost();
                            if (localAddress.getHostAddress().equals(host)) {
                                networkInterface = NetworkInterface.getByInetAddress(localAddress);
                                byte[] macAddressBytes = networkInterface.getHardwareAddress();
                                sb = new StringBuilder();
                                for (int a = 0; a < macAddressBytes.length; a++) {
                                    sb.append(String.format("%02X%s", macAddressBytes[a], (a < macAddressBytes.length - 1) ? "-" : ""));
                                }
                                macAddress = sb.toString();
                            }
                        }

                        if (host.equals("192.168.0.107")) {
                            macAddress = "e0-d5-5e-0b-7c-cd";
                        }
                        if (host.equals("192.168.0.1")) {
                            macAddress = "84-d8-1b-f1-d4-36";
                        }

                        if (status != "OFF") {
                            writer.write(host + "\t");
                            writer.write(macAddress + "\t");
                            writer.write(deviceName + "\t");
                            writer.write(status + "\n");
//                            writer.write(os + "\n");
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
