import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;

public class SplashPanel extends JPanel {

    private double angle = 0;
    private float alpha = 0.0f;
    private boolean fadingIn = true;
    private final Timer animationTimer;
    private final Runnable onComplete;

    public SplashPanel(Runnable onComplete) {
        this.onComplete = onComplete;
        setBackground(UIUtils.BACKGROUND);
        
        // Timer for 60fps animation
        animationTimer = new Timer(16, e -> {
            angle += 0.05; // Spin speed
            
            if (fadingIn) {
                alpha += 0.02f;
                if (alpha >= 1.0f) {
                    alpha = 1.0f;
                    fadingIn = false;
                }
            } else {
                // After it spins a bit, start fading out to transition
                if (angle > Math.PI * 2.5) { // About 1.25 full rotations before fade out
                    alpha -= 0.03f;
                    if (alpha <= 0.0f) {
                        alpha = 0.0f;
                        animationTimer.stop();
                        if (this.onComplete != null) {
                            this.onComplete.run();
                        }
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

        // Apply alpha composite for fading
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // Draw spinning logo
        AffineTransform old = g2.getTransform();
        g2.translate(cx, cy - 30);
        g2.rotate(angle);
        
        // Draw custom animated logo (e.g., stylized compass or target)
        int size = 80;
        
        // Outer ring
        g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(UIUtils.PRIMARY);
        g2.draw(new RoundRectangle2D.Double(-size/2, -size/2, size, size, 30, 30));
        
        // Inner spinning arrows
        g2.setColor(UIUtils.ACCENT);
        g2.fillPolygon(new int[]{-10, 0, 10}, new int[]{-size/2+10, -size/2-10, -size/2+10}, 3);
        g2.fillPolygon(new int[]{-10, 0, 10}, new int[]{size/2-10, size/2+10, size/2-10}, 3);
        g2.fillPolygon(new int[]{-size/2+10, -size/2-10, -size/2+10}, new int[]{-10, 0, 10}, 3);
        g2.fillPolygon(new int[]{size/2-10, size/2+10, size/2-10}, new int[]{-10, 0, 10}, 3);

        // Center dot
        g2.setColor(UIUtils.PRIMARY);
        g2.fillOval(-15, -15, 30, 30);

        g2.setTransform(old);

        // Draw Title text below
        g2.setColor(UIUtils.TEXT1);
        g2.setFont(new Font("SansSerif", Font.BOLD, 32));
        String title = "SCOSS";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, cx - fm.stringWidth(title) / 2, cy + 80);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g2.setColor(UIUtils.TEXT2);
        String sub = "Smart Campus Orientation & Support";
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(sub, cx - fm2.stringWidth(sub) / 2, cy + 110);
    }
}