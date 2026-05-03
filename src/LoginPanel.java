import javax.swing.*;
import java.awt.*;

public class LoginPanel {
    public interface LoginListener {
        void onLoginSuccess();
        void onSignupClicked();
    }

    public static JPanel build(JFrame mainFrame, LoginListener listener) {
        JPanel root = new JPanel(new GridBagLayout()) {
            private Image bgImage;
            {
                try {
                    bgImage = new ImageIcon("backround.jpeg").getImage(); 
                } catch (Exception e) {}
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                    g.setColor(new Color(15, 23, 42, 160)); // Modern dark blue/slate overlay
                    g.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    g.setColor(new Color(15, 23, 42));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

        // Glass-morphism inspired card
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Semi-transparent white background
                g2.setColor(new Color(255, 255, 255, 240));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                
                // Subtle border
                g2.setColor(new Color(255, 255, 255, 50));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);
                
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(45, 40, 45, 40));
        card.setPreferredSize(new Dimension(420, 580));

        JLabel logo = new JLabel("SCOSS", SwingConstants.CENTER);
        logo.setFont(new Font("Inter", Font.BOLD, 42));
        logo.setForeground(UIUtils.NAVY);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Smart Campus Orientation", SwingConstants.CENTER);
        sub.setFont(new Font("Inter", Font.PLAIN, 14));
        sub.setForeground(UIUtils.TEXT2);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Input Styling
        JTextField email = createModernField("Email Address", "\uD83D\uDCE7");
        JPasswordField pass = createModernPasswordField("Password", "\uD83D\uDD12");

        JButton login = UIUtils.primaryBtn("Sign In");
        login.setPreferredSize(new Dimension(Integer.MAX_VALUE, 48));
        login.setFont(new Font("Inter", Font.BOLD, 15));

        JButton signup = new JButton("Don't have an account? Create one") {
            {
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setForeground(UIUtils.ACCENT);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setFont(new Font("Inter", Font.PLAIN, 13));
            }
        };
        signup.addActionListener(e -> listener.onSignupClicked());

        JLabel msg = new JLabel(" ", SwingConstants.CENTER);
        msg.setFont(new Font("Inter", Font.MEDIUM, 12));
        msg.setForeground(UIUtils.DANGER);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        login.addActionListener(e -> {
            String em = email.getText();
            String ps = new String(pass.getPassword());
            User u = Database.authenticate(em, ps);
            if (u != null) {
                Database.currentUser = u;
                listener.onLoginSuccess();
            } else {
                msg.setText("Invalid email or password.");
            }
        });

        // Layout Assembly
        card.add(logo); 
        card.add(Box.createVerticalStrut(8));
        card.add(sub); 
        card.add(Box.createVerticalStrut(45));
        
        card.add(email); 
        card.add(Box.createVerticalStrut(20));
        card.add(pass); 
        card.add(Box.createVerticalStrut(30));
        
        login.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(login); 
        card.add(Box.createVerticalStrut(20));
        
        signup.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(signup); 
        card.add(Box.createVerticalStrut(15));
        card.add(msg);

        root.add(card);
        return root;
    }

    private static JTextField createModernField(String placeholder, String icon) {
        JTextField f = new JTextField(placeholder) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        f.setOpaque(false);
        f.setFont(new Font("Inter", Font.PLAIN, 14));
        f.setForeground(new Color(100, 116, 139));
        f.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (f.getText().equals(placeholder)) { f.setText(""); f.setForeground(UIUtils.TEXT1); }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (f.getText().isEmpty()) { f.setText(placeholder); f.setForeground(new Color(100, 116, 139)); }
            }
        });
        return f;
    }

    private static JPasswordField createModernPasswordField(String placeholder, String icon) {
        JPasswordField f = new JPasswordField(placeholder) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        f.setOpaque(false);
        f.setEchoChar((char)0);
        f.setFont(new Font("Inter", Font.PLAIN, 14));
        f.setForeground(new Color(100, 116, 139));
        f.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                String pass = new String(f.getPassword());
                if (pass.equals(placeholder)) { f.setText(""); f.setEchoChar('\u2022'); f.setForeground(UIUtils.TEXT1); }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                String pass = new String(f.getPassword());
                if (pass.isEmpty()) { f.setText(placeholder); f.setEchoChar((char)0); f.setForeground(new Color(100, 116, 139)); }
            }
        });
        return f;
    }
}
}
