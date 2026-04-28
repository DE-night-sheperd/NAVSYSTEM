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
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JLabel logo = new JLabel("SCOSS", SwingConstants.CENTER);
        logo.setFont(new Font("SansSerif", Font.BOLD, 24));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(30));

        // Content
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Define Navigation based on role
        addNavItem(sidebar, "Home", "home", HomePanel.build());
        addNavItem(sidebar, "My Profile", "profile", ProfilePanel.build());
        addNavItem(sidebar, "Campus Map", "map", NavigationPanel.build());
        addNavItem(sidebar, "QR Scanner", "qr", QRPanel.build());

        if ("student".equals(Database.currentUser.role)) {
            addNavItem(sidebar, "My Requests", "requests", RequestsPanel.build());
        } else if ("staff".equals(Database.currentUser.role)) {
            addNavItem(sidebar, "Manage Tasks", "staff_tasks", StaffPanel.build(mainFrame));
        } else if ("manager".equals(Database.currentUser.role)) {
            addNavItem(sidebar, "Locations", "locations", LocationsPanel.build(mainFrame));
            addNavItem(sidebar, "Services", "services", ServicesPanel.build(mainFrame));
            addNavItem(sidebar, "Staff Tasks", "staff_tasks", StaffPanel.build(mainFrame));
        }

        sidebar.add(Box.createVerticalGlue());

        JButton logout = new JButton("Sign Out");
        logout.setFont(UIUtils.fontNormal);
        logout.setForeground(new Color(226, 232, 240));
        logout.setBackground(new Color(255, 255, 255, 20));
        logout.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        logout.setFocusPainted(false);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.addActionListener(e -> {
            Database.currentUser = null;
            mainFrame.getContentPane().removeAll();
            mainFrame.add(LoginPanel.build(mainFrame, () -> {
                mainFrame.getContentPane().removeAll();
                mainFrame.add(new MainDashboard(mainFrame));
                mainFrame.revalidate();
                mainFrame.repaint();
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
        btn.setFont(UIUtils.fontNormal);
        btn.setForeground(new Color(148, 163, 184));
        btn.setBackground(UIUtils.NAVY);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        btn.addActionListener(e -> showPage(key));
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(4));
        navButtons.put(key, btn);
    }

    private void showPage(String key) {
        cardLayout.show(contentPanel, key);
        navButtons.forEach((k, b) -> {
            if (k.equals(key)) {
                b.setForeground(Color.WHITE);
                b.setBackground(new Color(255, 255, 255, 30));
            } else {
                b.setForeground(new Color(148, 163, 184));
                b.setBackground(UIUtils.NAVY);
            }
        });
    }
}
