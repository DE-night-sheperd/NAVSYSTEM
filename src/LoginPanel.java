import javax.swing.*;
import javax.swing.border.Border;
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
                Graphics2D g2 = (Graphics2D) g.create();
                if (bgImage != null) {
                    g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                    // Premium deep blue gradient overlay
                    GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42, 180), 
                                                        0, getHeight(), new Color(30, 41, 59, 220));
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    g2.setColor(new Color(15, 23, 42));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
            }
        };

        // Premium Login Card
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2.setColor(new Color(0, 0, 0, 80));
                g2.fillRoundRect(5, 5, getWidth()-10, getHeight()-10, 40, 40);
                
                // Card Body
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-5, getHeight()-5, 40, 40);
                
                // Decorative Accent Bar at the top
                g2.setColor(UIUtils.NAVY);
                g2.fillRoundRect(0, 0, getWidth()-5, 12, 40, 40);
                g2.fillRect(0, 6, getWidth()-5, 6);
                
                g2.dispose();
            }
        };
        card.setLayout(new GridBagLayout());
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(450, 620));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(50, 45, 50, 45));

        // Logo Section
        JLabel logo = new JLabel("SCOSS", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 48));
        logo.setForeground(new Color(15, 23, 42));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("SOL PLAATJE UNIVERSITY", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sub.setForeground(UIUtils.NAVY);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcome = new JLabel("Welcome Back", SwingConstants.CENTER);
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        welcome.setForeground(new Color(100, 116, 139));
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Input Fields
        JPanel emailGroup = createInputGroup("Email Address", "Enter your SPU email");
        JTextField emailField = (JTextField) emailGroup.getClientProperty("field");
        
        JPanel passGroup = createInputGroup("Password", "Enter your password");
        JPasswordField passField = (JPasswordField) passGroup.getClientProperty("field");
        passField.setEchoChar('\u2022');

        // Login Button
        JButton loginBtn = new JButton("SIGN IN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIUtils.NAVY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 50));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        loginBtn.setContentAreaFilled(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Signup Link
        JButton signupBtn = new JButton("New student? Create an account") {
            {
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setForeground(new Color(71, 85, 105));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setFont(new Font("Segoe UI", Font.PLAIN, 13)); 
            }
        };
        signupBtn.addActionListener(e -> listener.onSignupClicked());

        JLabel errorMsg = new JLabel(" ", SwingConstants.CENTER);
        errorMsg.setFont(new Font("Segoe UI", Font.PLAIN, 12)); 
        errorMsg.setForeground(new Color(220, 38, 38));
        errorMsg.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginBtn.addActionListener(e -> {
            String em = emailField.getText().trim();
            String ps = new String(passField.getPassword());
            User u = Database.authenticate(em, ps);
            if (u != null) {
                Database.currentUser = u;
                listener.onLoginSuccess();
            } else {
                errorMsg.setText("Invalid credentials. Please try again.");
            }
        });

        // Assemble Content
        content.add(logo);
        content.add(Box.createVerticalStrut(4));
        content.add(sub);
        content.add(Box.createVerticalStrut(30));
        content.add(welcome);
        content.add(Box.createVerticalStrut(40));
        
        content.add(emailGroup);
        content.add(Box.createVerticalStrut(20));
        content.add(passGroup);
        content.add(Box.createVerticalStrut(35));
        
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(loginBtn);
        content.add(Box.createVerticalStrut(20));
        
        signupBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(signupBtn);
        content.add(Box.createVerticalStrut(15));
        content.add(errorMsg);

        card.add(content);
        root.add(card);
        return root;
    }

    private static JPanel createInputGroup(String labelText, String placeholder) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);
        group.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(51, 65, 85));
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 8, 0));

        JTextField field = labelText.contains("Password") ? new JPasswordField() : new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(new Color(30, 41, 59));
        field.setPreferredSize(new Dimension(360, 45));
        field.setMaximumSize(new Dimension(360, 45));
        
        field.setBorder(BorderFactory.createCompoundBorder(
            new Border() {
                @Override
                public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(226, 232, 240));
                    g2.drawRoundRect(x, y, width-1, height-1, 12, 12);
                    g2.dispose();
                }
                @Override public Insets getBorderInsets(Component c) { return new Insets(10, 15, 10, 15); }
                @Override public boolean isBorderOpaque() { return false; }
            },
            BorderFactory.createEmptyBorder(0, 5, 0, 5)
        ));

        group.add(label);
        group.add(field);
        group.putClientProperty("field", field);
        return group;
    }
}
