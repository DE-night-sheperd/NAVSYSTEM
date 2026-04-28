import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;

public class SplashPanel extends JPanel {

    private double angle = 0;
    private float logoAlpha = 0.0f;
    private float aiAlpha = 0.0f;
    private float textAlpha = 0.0f;
    
    private int phase = 0; 
    // 0: logo spin & fade in
    // 1: logo fade out, AI flies in & hovers
    // 2: AI message fades in ("LOG IN YOU'LL FIND ME IN THERE")
    // 3: everything fades out
    
    private double hoverOffset = 0;
    private final Timer animationTimer;
    private final Runnable onComplete;

    public SplashPanel(Runnable onComplete) {
        this.onComplete = onComplete;
        setBackground(UIUtils.BG);
        
        // Timer for 60fps animation
        animationTimer = new Timer(16, e -> {
            hoverOffset += 0.08;
            
            if (phase == 0) {
                angle += 0.05; // Spin speed
                logoAlpha += 0.02f;
                if (logoAlpha >= 1.0f) {
                    logoAlpha = 1.0f;
                }
                if (angle > Math.PI * 2.0) { // After 1 full rotation
                    phase = 1;
                }
            } 
            else if (phase == 1) {
                logoAlpha -= 0.03f;
                if (logoAlpha <= 0.0f) {
                    logoAlpha = 0.0f;
                    aiAlpha += 0.03f;
                    if (aiAlpha >= 1.0f) {
                        aiAlpha = 1.0f;
                        phase = 2;
                    }
                }
            }
            else if (phase == 2) {
                textAlpha += 0.02f;
                if (textAlpha >= 1.0f) {
                    textAlpha = 1.0f;
                    // Wait a bit, then fade out
                    if (hoverOffset > 15) { // Arbitrary delay based on hoverOffset
                        phase = 3;
                    }
                }
            }
            else if (phase == 3) {
                aiAlpha -= 0.03f;
                textAlpha -= 0.03f;
                if (aiAlpha <= 0.0f) {
                    aiAlpha = 0.0f;
                    textAlpha = 0.0f;
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

        // --- DRAW LOGO (Phase 0) ---
        if (logoAlpha > 0) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, logoAlpha));
            AffineTransform old = g2.getTransform();
            g2.translate(cx, cy - 30);
            g2.rotate(angle);
            
            int size = 80;
            g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(UIUtils.ACCENT);
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

        // --- DRAW AI (Phase 1, 2, 3) ---
        if (aiAlpha > 0) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, aiAlpha));
            
            // Hovering Y calculation
            int aiY = cy - 40 + (int)(Math.sin(hoverOffset) * 15);
            
            // Draw slick, glowing AI orb
            for (int i = 0; i < 5; i++) {
                int glowSize = 80 + (i * 15);
                g2.setColor(new Color(UIUtils.ACCENT.getRed(), UIUtils.ACCENT.getGreen(), UIUtils.ACCENT.getBlue(), 30 - (i * 5)));
                g2.fillOval(cx - glowSize/2, aiY - glowSize/2, glowSize, glowSize);
            }
            
            // Core
            g2.setColor(UIUtils.ACCENT);
            g2.fillOval(cx - 30, aiY - 30, 60, 60);
            
            // Inner Core
            g2.setColor(Color.WHITE);
            g2.fillOval(cx - 15, aiY - 15, 30, 30);
            
            // Orbiting ring
            g2.setStroke(new BasicStroke(3));
            g2.setColor(new Color(255, 255, 255, 150));
            g2.drawArc(cx - 45, aiY - 45, 90, 90, (int)(hoverOffset * 50) % 360, 100);
            g2.drawArc(cx - 45, aiY - 45, 90, 90, ((int)(hoverOffset * 50) + 180) % 360, 100);
            
            // AI Message
            if (textAlpha > 0) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textAlpha));
                g2.setColor(UIUtils.TEXT1);
                g2.setFont(new Font("SansSerif", Font.BOLD, 22));
                String msg = "LOG IN, YOU'LL FIND ME IN THERE.";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, cx - fm.stringWidth(msg) / 2, cy + 90);
            }
        }
    }
}