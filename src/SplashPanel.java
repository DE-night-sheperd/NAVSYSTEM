import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;

public class SplashPanel extends JPanel {

    private double angle = 0;
    private float logoAlpha = 0.0f;
    private float textAlpha = 0.0f;
    
    private int phase = 0; 
    // 0: logo spin & fade in
    // 1: wait and fade out
    
    private double hoverOffset = 0;
    private final Timer animationTimer;
    private final Runnable onComplete;

    public SplashPanel(Runnable onComplete) {
        this.onComplete = onComplete;
        setBackground(Color.WHITE); // White background as requested
        
        // Timer for 60fps animation
        animationTimer = new Timer(16, e -> {
            hoverOffset += 0.08;
            
            if (phase == 0) {
                angle += 0.05; // Spin speed
                logoAlpha += 0.02f;
                if (logoAlpha >= 1.0f) {
                    logoAlpha = 1.0f;
                }
                if (angle > Math.PI * 4.0) { // 2 rotations
                    phase = 1;
                }
            } 
            else if (phase == 1) {
                logoAlpha -= 0.03f;
                if (logoAlpha <= 0.0f) {
                    logoAlpha = 0.0f;
                    ((Timer)e.getSource()).stop();
                    if (this.onComplete != null) {
                        this.onComplete.run();
                    }
                }
            }
            repaint();
        });
    }

    public void start() {
        animationTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        // --- DRAW LOGO ---
        if (logoAlpha > 0) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, logoAlpha));
            AffineTransform old = g2.getTransform();
            g2.translate(cx, cy - 30);
            g2.rotate(angle);
            
            int size = 80;
            g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(UIUtils.ACCENT); // This will be the new red
            g2.draw(new RoundRectangle2D.Double(-size/2, -size/2, size, size, 30, 30));
            
            g2.setColor(UIUtils.ACCENT);
            g2.fillPolygon(new int[]{-10, 0, 10}, new int[]{-size/2+10, -size/2-10, -size/2+10}, 3);
            g2.fillPolygon(new int[]{-10, 0, 10}, new int[]{size/2-10, size/2+10, size/2-10}, 3);
            g2.fillPolygon(new int[]{-size/2+10, -size/2-10, -size/2+10}, new int[]{-10, 0, 10}, 3);
            g2.fillPolygon(new int[]{size/2-10, size/2+10, size/2-10}, new int[]{-10, 0, 10}, 3);

            g2.setColor(UIUtils.ACCENT);
            g2.fillOval(-15, -15, 30, 30);
            g2.setTransform(old);

            g2.setColor(UIUtils.TEXT1);
            g2.setFont(new Font("SansSerif", Font.BOLD, 36));
            String title = "SCOSS";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(title, cx - fm.stringWidth(title) / 2, cy + 80);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
            g2.setColor(UIUtils.TEXT2);
            String sub = "Smart Campus Online Service System";
            FontMetrics fm2 = g2.getFontMetrics();
            g2.drawString(sub, cx - fm2.stringWidth(sub) / 2, cy + 110);
        }
    }
}
}