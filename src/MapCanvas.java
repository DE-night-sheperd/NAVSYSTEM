import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapCanvas extends JPanel {
    public Location highlighted;
    private final Color[] pinColors = {
        UIUtils.ACCENT, UIUtils.SUCCESS, UIUtils.WARNING, UIUtils.DANGER, 
        new Color(139,92,246), new Color(236,72,153)
    };

    // Map state
    private double centerLat = -28.7460;
    private double centerLon = 24.7675;
    private int zoom = 16;
    private final int TILE_SIZE = 256;
    private boolean satelliteView = true;

    // Navigation tracking
    private boolean isNavigating = false;
    private double navCurrentLat;
    private double navCurrentLon;
    private double navDestLat;
    private double navDestLon;
    private Timer navTimer;
    private Location navDestLocation;

    // Interaction state
    private Point lastMousePos;

    // Tile cache and fetching
    private final Map<String, Image> tileCache = new LinkedHashMap<String, Image>(100, 0.75f, true) {
        protected boolean removeEldestEntry(Map.Entry<String, Image> eldest) {
            return size() > 150;
        }
    };
    private final ExecutorService tileFetcher = Executors.newFixedThreadPool(4);

    public MapCanvas() { 
        setBackground(new Color(230,240,255));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePos = e.getPoint();
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePos != null) {
                    int dx = lastMousePos.x - e.getX();
                    int dy = lastMousePos.y - e.getY();
                    
                    // Calculate pixels to lat/lon
                    double n = Math.pow(2.0, zoom);
                    double lonDiff = (dx / (double)TILE_SIZE) * 360.0 / n;
                    
                    // Y diff is more complex due to Mercator projection
                    double latRad = Math.toRadians(centerLat);
                    double y = (1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI) / 2.0 * n;
                    y += (dy / (double)TILE_SIZE);
                    double newLatRad = Math.atan(Math.sinh(Math.PI * (1.0 - 2.0 * y / n)));
                    
                    centerLon += lonDiff;
                    centerLat = Math.toDegrees(newLatRad);
                    
                    lastMousePos = e.getPoint();
                    repaint();
                }
            }
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() < 0) {
                    if (zoom < 19) zoom++;
                } else {
                    if (zoom > 2) zoom--;
                }
                repaint();
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);
    }

    public void setSatelliteView(boolean sat) {
        this.satelliteView = sat;
        repaint();
    }

    public void highlight(Location l) { 
        highlighted = l;
        if (l != null) {
            centerLat = l.latitude;
            centerLon = l.longitude;
            if (zoom < 17) zoom = 17;
        }
        repaint(); 
    }

    public void startNavigation(Location dest) {
        if (dest == null) return;
        this.navDestLocation = dest;
        this.navDestLat = dest.latitude;
        this.navDestLon = dest.longitude;

        // Set a simulated start location (e.g., SPU Main Entrance)
        this.navCurrentLat = -28.7430;
        this.navCurrentLon = 24.7660;

        // Center map to see both points
        this.centerLat = (navCurrentLat + navDestLat) / 2.0;
        this.centerLon = (navCurrentLon + navDestLon) / 2.0;
        this.zoom = 16;

        this.isNavigating = true;

        if (navTimer != null) navTimer.stop();

        VoiceGuide.speak("Navigating to " + dest.locationName + ". Proceed along the highlighted path.");

        navTimer = new Timer(150, e -> {
            // Move current location 1% closer to destination per tick
            double dLat = navDestLat - navCurrentLat;
            double dLon = navDestLon - navCurrentLon;
            
            // Distance formula
            double dist = Math.sqrt(dLat*dLat + dLon*dLon);
            
            if (dist < 0.0001) {
                navTimer.stop();
                isNavigating = false;
                VoiceGuide.speak("You have arrived at " + dest.locationName);
                repaint();
                return;
            }

            // Move a fixed step size (simulating walking speed)
            double step = 0.00005;
            if (dist < step) {
                navCurrentLat = navDestLat;
                navCurrentLon = navDestLon;
            } else {
                navCurrentLat += (dLat / dist) * step;
                navCurrentLon += (dLon / dist) * step;
            }
            
            // Keep map centered on user
            this.centerLat = navCurrentLat;
            this.centerLon = navCurrentLon;

            repaint();
        });
        navTimer.start();
    }

    public void stopNavigation() {
        if (navTimer != null) navTimer.stop();
        isNavigating = false;
        repaint();
    }

    private Point2D.Double latLonToTileXY(double lat, double lon, int z) {
        double n = Math.pow(2.0, z);
        double x = ((lon + 180.0) / 360.0) * n;
        double latRad = Math.toRadians(lat);
        double y = (1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI) / 2.0 * n;
        return new Point2D.Double(x, y);
    }

    private void requestTile(String key, String urlString) {
        if (tileCache.containsKey(key)) return;
        
        // Put a placeholder so we don't request again
        tileCache.put(key, new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB));
        
        tileFetcher.submit(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "SCOSS-App/1.0");
                Image img = ImageIO.read(conn.getInputStream());
                if (img != null) {
                    SwingUtilities.invokeLater(() -> {
                        tileCache.put(key, img);
                        repaint();
                    });
                }
            } catch (Exception ex) {
                // Ignore download errors
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Calculate center tile coordinates
        Point2D.Double centerTile = latLonToTileXY(centerLat, centerLon, zoom);
        
        int centerX = (int)Math.floor(centerTile.x);
        int centerY = (int)Math.floor(centerTile.y);
        
        double offsetX = centerTile.x - centerX;
        double offsetY = centerTile.y - centerY;

        int startX = centerX - (w / TILE_SIZE) / 2 - 1;
        int endX   = centerX + (w / TILE_SIZE) / 2 + 1;
        int startY = centerY - (h / TILE_SIZE) / 2 - 1;
        int endY   = centerY + (h / TILE_SIZE) / 2 + 1;

        // Draw Tiles
        for (int tx = startX; tx <= endX; tx++) {
            for (int ty = startY; ty <= endY; ty++) {
                int screenX = (int)((tx - centerTile.x) * TILE_SIZE) + w/2;
                int screenY = (int)((ty - centerTile.y) * TILE_SIZE) + h/2;

                String key = satelliteView ? "sat_" + zoom + "_" + tx + "_" + ty : "osm_" + zoom + "_" + tx + "_" + ty;
                Image img = tileCache.get(key);

                if (img != null && img instanceof BufferedImage && ((BufferedImage)img).getType() != BufferedImage.TYPE_INT_ARGB) {
                    g2.drawImage(img, screenX, screenY, TILE_SIZE, TILE_SIZE, null);
                } else if (img == null) {
                    String url = satelliteView ?
                        "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/" + zoom + "/" + ty + "/" + tx :
                        "https://tile.openstreetmap.org/" + zoom + "/" + tx + "/" + ty + ".png";
                    requestTile(key, url);
                    
                    // Draw loading rect
                    g2.setColor(new Color(240, 240, 240));
                    g2.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
                    g2.setColor(new Color(200, 200, 200));
                    g2.drawRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
                } else {
                    // It's the placeholder, draw loading rect
                    g2.setColor(new Color(240, 240, 240));
                    g2.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        // Draw Navigation Trail
        if (isNavigating && navDestLocation != null) {
            Point2D.Double pCurrent = latLonToTileXY(navCurrentLat, navCurrentLon, zoom);
            Point2D.Double pDest = latLonToTileXY(navDestLat, navDestLon, zoom);
            
            int curX = (int)((pCurrent.x - centerTile.x) * TILE_SIZE) + w/2;
            int curY = (int)((pCurrent.y - centerTile.y) * TILE_SIZE) + h/2;
            int dstX = (int)((pDest.x - centerTile.x) * TILE_SIZE) + w/2;
            int dstY = (int)((pDest.y - centerTile.y) * TILE_SIZE) + h/2;

            // Draw line
            g2.setColor(new Color(59, 130, 246, 180)); // Blue, semi-transparent
            g2.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(curX, curY, dstX, dstY);

            // Draw Current Location Marker
            g2.setColor(new Color(59, 130, 246, 50));
            g2.fillOval(curX - 15, curY - 15, 30, 30); // Pulsing radius
            
            g2.setColor(new Color(255, 255, 255));
            g2.fillOval(curX - 6, curY - 6, 12, 12); // Outer white ring
            
            g2.setColor(new Color(37, 99, 235));
            g2.fillOval(curX - 4, curY - 4, 8, 8); // Inner blue dot
        }

        // Draw Locations
        for (int i = 0; i < Database.locations.size(); i++) {
            Location l = Database.locations.get(i);
            Point2D.Double p = latLonToTileXY(l.latitude, l.longitude, zoom);
            int px = (int)((p.x - centerTile.x) * TILE_SIZE) + w/2;
            int py = (int)((p.y - centerTile.y) * TILE_SIZE) + h/2;

            // Only draw if on screen
            if (px > -50 && px < w + 50 && py > -50 && py < h + 50) {
                boolean isHighlighted = highlighted != null && highlighted.locationId == l.locationId;
                Color col = pinColors[i % pinColors.length];

                // Draw Pin background
                g2.setColor(UIUtils.lighter(col));
                g2.fillRoundRect(px-14, py-14, 28, 28, 6, 6);
                g2.setColor(col); 
                g2.setStroke(new BasicStroke(isHighlighted ? 2.5f : 1.5f));
                g2.drawRoundRect(px-14, py-14, 28, 28, 6, 6);

                // Initial letter
                g2.setColor(col.darker());
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                String init = l.locationName.substring(0,1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(init, px - fm.stringWidth(init)/2, py + fm.getAscent()/2 - 2);

                // Label
                if (isHighlighted) {
                    g2.setColor(UIUtils.CARD);
                    String name = l.locationName.length()>18 ? l.locationName.substring(0,15)+"..." : l.locationName;
                    FontMetrics fm2 = g2.getFontMetrics(new Font("SansSerif",Font.PLAIN,10));
                    int lw = fm2.stringWidth(name) + 8;
                    g2.fillRoundRect(px - lw/2, py - 38, lw, 18, 4, 4);
                    g2.setColor(UIUtils.TEXT2); 
                    g2.setStroke(new BasicStroke(0.5f));
                    g2.drawRoundRect(px - lw/2, py - 38, lw, 18, 4, 4);
                    g2.setFont(new Font("SansSerif",Font.PLAIN,10));
                    g2.setColor(UIUtils.TEXT1);
                    g2.drawString(name, px - fm2.stringWidth(name)/2, py - 24);
                } else {
                    g2.setFont(new Font("SansSerif",Font.PLAIN,11));
                    g2.setColor(Color.WHITE);
                    FontMetrics fm2 = g2.getFontMetrics();
                    String short_ = l.locationName.length()>12 ? l.locationName.substring(0,10)+".." : l.locationName;
                    
                    // Add text shadow for visibility on satellite map
                    g2.drawString(short_, px - fm2.stringWidth(short_)/2 + 1, py + 26 + 1);
                    g2.setColor(Color.BLACK);
                    g2.drawString(short_, px - fm2.stringWidth(short_)/2, py + 26);
                }
            }
        }

        // Legend / UI
        g2.setFont(new Font("SansSerif",Font.BOLD,12));
        g2.setColor(Color.BLACK);
        g2.drawString("Sol Plaatje University Campus Map", 11, 21);
        g2.setColor(Color.WHITE);
        g2.drawString("Sol Plaatje University Campus Map", 10, 20);

        g2.setFont(new Font("SansSerif",Font.PLAIN,10));
        g2.setColor(Color.BLACK);
        g2.drawString("Drag to pan, scroll to zoom", 11, 36);
        g2.setColor(Color.WHITE);
        g2.drawString("Drag to pan, scroll to zoom", 10, 35);
    }
}