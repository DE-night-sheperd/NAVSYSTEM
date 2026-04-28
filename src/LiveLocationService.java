import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LiveLocationService {
    public interface LocationListener {
        void onLocationUpdated(double lat, double lon);
        void onError(String message);
    }

    private static boolean permissionGranted = false;
    private static boolean hasAsked = false;
    private static Timer pollTimer;
    
    // For desktop simulation, since IP location doesn't move as you walk.
    // We will fetch the real location once, then simulate walking towards the destination
    // so the user can see the "moving" trail effect on desktop before mobile scaling.
    private static double currentLat = 0;
    private static double currentLon = 0;
    private static double targetLat = 0;
    private static double targetLon = 0;

    public static void setTarget(double lat, double lon) {
        targetLat = lat;
        targetLon = lon;
    }

    public static void requestLocation(java.awt.Component parent, LocationListener listener) {
        if (!hasAsked) {
            int choice = JOptionPane.showConfirmDialog(parent,
                "SCOSS needs to access your device's location for real-time navigation.\nAllow location access?",
                "Device Location Permission",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            permissionGranted = (choice == JOptionPane.YES_OPTION);
            hasAsked = true;
        }

        if (permissionGranted) {
            if (pollTimer != null) pollTimer.stop();
            
            // Fetch real location first
            fetchRealLocation(listener);
            
            // Poll every 2 seconds to simulate live GPS updates
            pollTimer = new Timer(2000, e -> {
                if (currentLat != 0 && targetLat != 0) {
                    // Simulate movement towards target for desktop testing
                    double dLat = targetLat - currentLat;
                    double dLon = targetLon - currentLon;
                    double dist = Math.sqrt(dLat*dLat + dLon*dLon);
                    
                    if (dist > 0.0001) {
                        double step = Math.min(0.00005, dist);
                        currentLat += (dLat / dist) * step;
                        currentLon += (dLon / dist) * step;
                    }
                    listener.onLocationUpdated(currentLat, currentLon);
                } else if (currentLat != 0) {
                    listener.onLocationUpdated(currentLat, currentLon);
                }
            });
            pollTimer.start();
        } else {
            listener.onError("Location permission denied by user.");
        }
    }

    public static void stop() {
        if (pollTimer != null) pollTimer.stop();
        targetLat = 0;
        targetLon = 0;
    }

    private static void fetchRealLocation(LocationListener listener) {
        new Thread(() -> {
            try {
                URL url = new URL("http://ip-api.com/json/");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                con.disconnect();

                String json = content.toString();
                double lat = extractDouble(json, "\"lat\":");
                double lon = extractDouble(json, "\"lon\":");

                if (lat != 0 && lon != 0) {
                    // If it's the first fetch, set current location
                    if (currentLat == 0) {
                        currentLat = lat;
                        currentLon = lon;
                        
                        // For campus testing: If the real IP location is too far from SPU (e.g. > 10km),
                        // clamp it near SPU so the user can actually see the campus map features!
                        double distToSPU = Math.sqrt(Math.pow(lat - (-28.7460), 2) + Math.pow(lon - 24.7675, 2));
                        if (distToSPU > 0.1) { 
                            currentLat = -28.7430; // SPU Entrance roughly
                            currentLon = 24.7660;
                            System.out.println("Real location is far from SPU. Clamping to campus for testing.");
                        }
                    }
                    SwingUtilities.invokeLater(() -> listener.onLocationUpdated(currentLat, currentLon));
                } else {
                    SwingUtilities.invokeLater(() -> listener.onError("Could not resolve real-time coordinates."));
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> listener.onError("Network error fetching live location."));
            }
        }).start();
    }

    private static double extractDouble(String json, String key) {
        int idx = json.indexOf(key);
        if (idx == -1) return 0;
        int start = idx + key.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        try {
            return Double.parseDouble(json.substring(start, end).trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
