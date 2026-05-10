import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ToastManager {
    private static final List<Toast> activeToasts = new ArrayList<>();
    private static JFrame mainFrame;

    public static void setMainFrame(JFrame frame) {
        mainFrame = frame;
    }

    public static void showToast(String message, int durationMs, ToastType type) {
        if (mainFrame == null) return;

        SwingUtilities.invokeLater(() -> {
            Toast toast = new Toast(message, durationMs, type);
            activeToasts.add(toast);
            toast.start();
        });
    }

    public static void showInfo(String message) {
        showToast(message, 3000, ToastType.INFO);
    }

    public static void showSuccess(String message) {
        showToast(message, 3000, ToastType.SUCCESS);
    }

    public static void showWarning(String message) {
        showToast(message, 4000, ToastType.WARNING);
    }

    public static void showError(String message) {
        showToast(message, 5000, ToastType.ERROR);
    }

    public enum ToastType {
        INFO, SUCCESS, WARNING, ERROR
    }

    private static class Toast extends JWindow {
        private final String message;
        private final int duration;
        private final ToastType type;
        private float alpha = 0.0f;

        public Toast(String message, int duration, ToastType type) {
            super(mainFrame);
            this.message = message;
            this.duration = duration;
            this.type = type;

            setSize(400, 60);
            setAlwaysOnTop(true);
            setLayout(new BorderLayout());

            JPanel panel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    
                    Color bg = getBackgroundColor();
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                }
            };
            panel.setOpaque(false);
            panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

            JLabel iconLabel = new JLabel(getIcon());
            iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));

            JLabel msgLabel = new JLabel(message);
            msgLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            msgLabel.setForeground(Color.WHITE);

            panel.add(iconLabel, BorderLayout.WEST);
            panel.add(msgLabel, BorderLayout.CENTER);

            add(panel);

            // Position the toast
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = screenSize.width - getWidth() - 30;
            int y = screenSize.height - getHeight() - 80 - (activeToasts.size() * 70);
            setLocation(x, y);
        }

        private String getIcon() {
            switch (type) {
                case INFO: return "ℹ️";
                case SUCCESS: return "✅";
                case WARNING: return "⚠️";
                case ERROR: return "❌";
                default: return "ℹ️";
            }
        }

        private Color getBackgroundColor() {
            switch (type) {
                case INFO: return new Color(59, 130, 246);
                case SUCCESS: return UIUtils.SUCCESS;
                case WARNING: return UIUtils.WARNING;
                case ERROR: return UIUtils.DANGER;
                default: return new Color(59, 130, 246);
            }
        }

        public void start() {
            setVisible(true);
            
            Timer fadeInTimer = new Timer(20, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    alpha += 0.08f;
                    if (alpha >= 1.0f) {
                        alpha = 1.0f;
                        ((Timer)e.getSource()).stop();
                        
                        Timer stayTimer = new Timer(duration, new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                ((Timer)e.getSource()).stop();
                                fadeOut();
                            }
                        });
                        stayTimer.setRepeats(false);
                        stayTimer.start();
                    }
                    repaint();
                }
            });
            fadeInTimer.setRepeats(true);
            fadeInTimer.start();
        }

        private void fadeOut() {
            Timer fadeOutTimer = new Timer(20, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    alpha -= 0.08f;
                    if (alpha <= 0.0f) {
                        alpha = 0.0f;
                        ((Timer)e.getSource()).stop();
                        setVisible(false);
                        dispose();
                        activeToasts.remove(Toast.this);
                    }
                    repaint();
                }
            });
            fadeOutTimer.setRepeats(true);
            fadeOutTimer.start();
        }
    }
}
