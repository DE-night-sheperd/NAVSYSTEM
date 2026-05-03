import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MainDashboard extends JPanel {
    private final JFrame mainFrame;
    private final JPanel contentPanel;
    private final CardLayout cardLayout;
    private final Map<String, JButton> navButtons = new HashMap<>();

    public MainDashboard(JFrame frame) {
        this.mainFrame = frame;
        setLayout(new BorderLayout());

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIUtils.NAVY);
        sidebar.setPreferredSize(new Dimension(80, 0)); // Narrower rail
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel logo = new JLabel("S", SwingConstants.CENTER); // Minimalist logo
        logo.setFont(new Font("SansSerif", Font.BOLD, 28));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(30));

        // Content
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Define Navigation based on role with icons
        addNavItem(sidebar, "\uD83C\uDFE0", "home", HomePanel.build());
        addNavItem(sidebar, "\uD83D\uDC64", "profile", ProfilePanel.build());
        addNavItem(sidebar, "\uD83D\uDDFA\uFE0F", "map", NavigationPanel.build());
        addNavItem(sidebar, "\uD83D\uDCF7", "qr", QRPanel.build());
        addNavItem(sidebar, "\uD83E\uDD16", "ai", AIPanel.build());

        if ("student".equals(Database.currentUser.role)) {
            addNavItem(sidebar, "\uD83D\uDCDD", "requests", RequestsPanel.build());
        } else if ("staff".equals(Database.currentUser.role)) {
            addNavItem(sidebar, "\uD83D\uDEE0\uFE0F", "staff_tasks", StaffPanel.build(mainFrame));
        } else if ("manager".equals(Database.currentUser.role)) {
            addNavItem(sidebar, "\uD83D\uDCCD", "locations", LocationsPanel.build(mainFrame));
            addNavItem(sidebar, "\u2699\uFE0F", "services", ServicesPanel.build(mainFrame));
            addNavItem(sidebar, "\uD83D\uDC77", "staff_tasks", StaffPanel.build(mainFrame));
        }

        sidebar.add(Box.createVerticalGlue());

        JButton logout = new JButton("\uD83D\uDEAA");
        logout.setToolTipText("Sign Out");
        logout.setFont(new Font("SansSerif", Font.PLAIN, 20));
        logout.setForeground(new Color(255, 255, 255, 180));
        logout.setBackground(UIUtils.NAVY);
        logout.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        logout.setFocusPainted(false);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.setAlignmentX(Component.CENTER_ALIGNMENT);
        logout.setMaximumSize(new Dimension(80, 50));
        logout.addActionListener(e -> {
            Database.currentUser = null;
            mainFrame.getContentPane().removeAll();
            mainFrame.add(LoginPanel.build(mainFrame, new LoginPanel.LoginListener() {
                @Override
                public void onLoginSuccess() {
                    mainFrame.getContentPane().removeAll();
                    mainFrame.add(new MainDashboard(mainFrame));
                    mainFrame.revalidate();
                    mainFrame.repaint();
                }
                @Override
                public void onSignupClicked() {
                    mainFrame.getContentPane().removeAll();
                    mainFrame.add(SignupPanel.build(mainFrame, new SignupPanel.SignupListener() {
                        @Override
                        public void onSignupComplete() {
                            logout.doClick();
                        }
                        @Override
                        public void onCancel() {
                            logout.doClick();
                        }
                    }));
                    mainFrame.revalidate();
                    mainFrame.repaint();
                }
            }));
            mainFrame.revalidate();
            mainFrame.repaint();
        });
        sidebar.add(logout);

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // Set default view
        showPage("home");
    }

    private void addNavItem(JPanel sidebar, String label, String key, JPanel panel) {
        contentPanel.add(panel, key);

        JButton btn = new JButton(label);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 24)); // Larger icons
        btn.setForeground(new Color(255, 255, 255, 140));
        btn.setBackground(UIUtils.NAVY);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(80, 60));

        btn.addActionListener(e -> showPage(key));
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(2));
        navButtons.put(key, btn);
    }

    private void showPage(String key) {
        cardLayout.show(contentPanel, key);
        navButtons.forEach((k, b) -> {
            if (k.equals(key)) {
                b.setForeground(Color.WHITE);
                b.setBackground(UIUtils.NAVY_LIGHT);
            } else {
                b.setForeground(new Color(255, 255, 255, 140));
                b.setBackground(UIUtils.NAVY);
            }
        });
    }
}
