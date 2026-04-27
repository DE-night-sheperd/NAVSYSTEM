import javax.swing.*;
import java.awt.*;

public class MapCanvas extends JPanel {
    public Location highlighted;
    private final Color[] pinColors = {
        UIUtils.ACCENT, UIUtils.SUCCESS, UIUtils.WARNING, UIUtils.DANGER, 
        new Color(139,92,246), new Color(236,72,153)
    };

    public MapCanvas() { 
        setBackground(new Color(230,240,255)); 
    }

    public void highlight(Location l) { 
        highlighted = l; 
        repaint(); 
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Grid
        g2.setColor(new Color(200,215,240));
        g2.setStroke(new BasicStroke(0.5f));
        for (int x = 0; x < getWidth(); x += 40) g2.drawLine(x, 0, x, getHeight());
        for (int y = 0; y < getHeight(); y += 40) g2.drawLine(0, y, getWidth(), y);

        // Campus outline
        g2.setColor(new Color(180,210,240)); 
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(40, 30, getWidth()-80, getHeight()-60, 20, 20);

        // Road
        g2.setColor(new Color(220,225,235)); 
        g2.fillRect(0, getHeight()/2-12, getWidth(), 24);
        g2.setColor(new Color(200,210,225)); 
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(0, getHeight()/2, getWidth(), getHeight()/2);

        // Find bounds
        double minLat=-25.7485, maxLat=-25.7450, minLon=28.1865, maxLon=28.1900;
        int margin = 60;

        for (int i = 0; i < Database.locations.size(); i++) {
            Location l = Database.locations.get(i);
            double nx = (l.longitude - minLon)/(maxLon - minLon);
            double ny = (l.latitude  - maxLat)/(minLat - maxLat);
            int px = margin + (int)(nx * (getWidth()  - 2*margin));
            int py = margin + (int)(ny * (getHeight() - 2*margin));

            boolean isHighlighted = highlighted != null && highlighted.locationId == l.locationId;
            Color col = pinColors[i % pinColors.length];

            // Building block
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
                g2.setFont(new Font("SansSerif",Font.PLAIN,9));
                g2.setColor(UIUtils.TEXT2);
                FontMetrics fm2 = g2.getFontMetrics();
                String short_ = l.locationName.length()>12 ? l.locationName.substring(0,10)+".." : l.locationName;
                g2.drawString(short_, px - fm2.stringWidth(short_)/2, py + 26);
            }
        }

        // Legend
        g2.setFont(new Font("SansSerif",Font.BOLD,10));
        g2.setColor(UIUtils.TEXT2);
        g2.drawString("Campus Map — Location Overview", 8, 16);
    }
}
