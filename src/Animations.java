import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Animations {
    
    public static void typeWriterEffect(JLabel label, String text, int delayMs) {
        new Thread(() -> {
            try {
                for (int i = 0; i <= text.length(); i++) {
                    final String part = text.substring(0, i);
                    SwingUtilities.invokeLater(() -> label.setText(part));
                    Thread.sleep(delayMs);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    public static void fadeIn(Component c, int durationMs) {
        new Timer(20, new ActionListener() {
            private float alpha = 0.0f;
            private final float step = 1.0f / (durationMs / 20);
            
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha += step;
                if (alpha >= 1.0f) {
                    alpha = 1.0f;
                    ((Timer)e.getSource()).stop();
                }
                if (c instanceof JComponent) {
                    ((JComponent)c).setOpaque(false);
                }
                c.repaint();
            }
        }).start();
    }
    
    public static void bounceLabel(JLabel label, int bounceHeight) {
        new Timer(30, new ActionListener() {
            private int yOffset = 0;
            private boolean goingUp = true;
            private int maxBounce = bounceHeight;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (goingUp) {
                    yOffset++;
                    if (yOffset >= maxBounce) {
                        goingUp = false;
                    }
                } else {
                    yOffset--;
                    if (yOffset <= 0) {
                        ((Timer)e.getSource()).stop();
                    }
                }
                label.setBorder(BorderFactory.createEmptyBorder(yOffset, 0, -yOffset, 0));
                label.revalidate();
            }
        }).start();
    }
    
    public static void pulseButton(JButton btn, Color original, Color highlight) {
        new Timer(500, new ActionListener() {
            private boolean toggle = false;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                btn.setBackground(toggle ? original : highlight);
                toggle = !toggle;
            }
        }).start();
    }
}
