import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Main application dashboard that manages the sidebar navigation and content switching.
 * Uses a CardLayout to swap between different functional panels (Home, Profile, Map, etc.).
 */
public class MainDashboard extends JPanel {
    private final JFrame mainFrame; // Reference to the main window for logout/re-login
    private final JPanel contentPanel; // The container that swaps views
    private final CardLayout cardLayout; // Layout manager for swapping views
    private final Map<String, JButton> navButtons = new HashMap<>(); // Tracks sidebar buttons for styling

    public MainDashboard(JFrame frame) {
        this.mainFrame = frame;
        setLayout(new BorderLayout());

        // Sidebar: The vertical navigation rail on the left
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.BLACK); // Sidebar is black
        sidebar.setPreferredSize(new Dimension(80, 0)); // Narrow rail design
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // App Logo/Icon at the top of the sidebar
        JLabel logo = new JLabel("S", SwingConstants.CENTER); 
        logo.setFont(new Font("SansSerif", Font.BOLD, 28));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(30));

        // contentPanel: Holds all the different screens of the app
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Define Navigation items based on the current user's role
        // Common navigation for all roles
        addNavItem(sidebar, "\uD83C\uDFE0", "home", HomePanel.build(this::showPage));
        addNavItem(sidebar, "\uD83D\uDC64", "profile", ProfilePanel.build());
        addNavItem(sidebar, "\uD83D\uDDFA\uFE0F", "map", NavigationPanel.build());
        addNavItem(sidebar, "\uD83D\uDCF7", "qr", QRPanel.build());
        addNavItem(sidebar, "\uD83E\uDD16", "ai", AIPanel.build());

        // Role-specific navigation logic
        if ("student".equals(Database.currentUser.role)) {
            addNavItem(sidebar, "\uD83D\uDCDD", "requests", RequestsPanel.build());
            addNavItem(sidebar, "\u2795", "log_request", RequestsPanel.build(1)); // Shortcut to "New Request" tab
        } else if ("staff".equals(Database.currentUser.role)) {
            addNavItem(sidebar, "\uD83D\uDEE0\uFE0F", "staff_tasks", StaffPanel.build(mainFrame));
        } else if ("manager".equals(Database.currentUser.role)) {
            addNavItem(sidebar, "\uD83D\uDCCD", "locations", LocationsPanel.build(mainFrame));
            addNavItem(sidebar, "\u2699\uFE0F", "services", ServicesPanel.build(mainFrame));
            addNavItem(sidebar, "\uD83D\uDC77", "staff_tasks", StaffPanel.build(mainFrame));
        }

        sidebar.add(Box.createVerticalGlue()); // Pushes theme and logout buttons to the bottom
        
        // Dark/Light Mode Toggle Button
        JButton themeToggle = new JButton(UIUtils.isDarkMode ? "☀️" : "🌙");
        themeToggle.setToolTipText(UIUtils.isDarkMode ? "Light Mode" : "Dark Mode");
        themeToggle.setFont(new Font("SansSerif", Font.PLAIN, 20));
        themeToggle.setForeground(Color.WHITE);
        themeToggle.setBackground(Color.BLACK);
        themeToggle.setContentAreaFilled(true);
        themeToggle.setBorderPainted(false);
        themeToggle.setOpaque(true);
        themeToggle.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        themeToggle.setFocusPainted(false);
        themeToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        themeToggle.setAlignmentX(Component.CENTER_ALIGNMENT);
        themeToggle.setMaximumSize(new Dimension(80, 50));
        themeToggle.addActionListener(e -> {
            UIUtils.isDarkMode = !UIUtils.isDarkMode;
            themeToggle.setText(UIUtils.isDarkMode ? "☀️" : "🌙");
            themeToggle.setToolTipText(UIUtils.isDarkMode ? "Light Mode" : "Dark Mode");
            
            // Refresh the entire dashboard
            mainFrame.getContentPane().removeAll();
            mainFrame.add(new MainDashboard(mainFrame));
            mainFrame.revalidate();
            mainFrame.repaint();
        });
        sidebar.add(themeToggle);
        sidebar.add(Box.createVerticalStrut(5));

        // Logout Button: Clears session and returns to login screen
        JButton logout = new JButton("\uD83D\uDEAA");
        logout.setToolTipText("Sign Out");
        logout.setFont(new Font("SansSerif", Font.PLAIN, 20));
        logout.setForeground(Color.WHITE);
        logout.setBackground(Color.BLACK);
        logout.setContentAreaFilled(true);
        logout.setBorderPainted(false);
        logout.setOpaque(true);
        logout.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        logout.setFocusPainted(false);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.setAlignmentX(Component.CENTER_ALIGNMENT);
        logout.setMaximumSize(new Dimension(80, 50));
        logout.addActionListener(e -> {
            Database.currentUser = null; // Clear session
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
                            logout.doClick(); // Return to login after signup
                        }
                        @Override
                        public void onCancel() {
                            logout.doClick(); // Return to login
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

        // Show the home page by default on login
        showPage("home");
    }

    /**
     * Adds a navigation item to the sidebar and its corresponding panel to the content container.
     */
    private void addNavItem(JPanel sidebar, String label, String key, JPanel panel) {
        contentPanel.add(panel, key); // Register panel with CardLayout

        JButton btn = new JButton(label);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 24)); // Icon size
        btn.setForeground(Color.WHITE); // Bright white icons
        btn.setBackground(Color.BLACK); // Button background is black
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(80, 60));

        btn.addActionListener(e -> showPage(key));
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(2));
        navButtons.put(key, btn); // Store reference for highlighting
    }

    /**
     * Switches the visible page in the content panel and updates sidebar button styles.
     */
    private void showPage(String key) {
        // Remove old panels and rebuild to ensure fresh data
        try {
            java.awt.Component[] comps = contentPanel.getComponents();
            for (java.awt.Component c : comps) {
                contentPanel.remove(c);
            }
        } catch (Exception e) {}

        // Rebuild all panels fresh
        addNavItemToContent("home", HomePanel.build(this::showPage));
        addNavItemToContent("profile", ProfilePanel.build());
        addNavItemToContent("map", NavigationPanel.build());
        addNavItemToContent("qr", QRPanel.build());
        addNavItemToContent("ai", AIPanel.build());

        if ("student".equals(Database.currentUser.role)) {
            addNavItemToContent("requests", RequestsPanel.build());
            addNavItemToContent("log_request", RequestsPanel.build(1));
        } else if ("staff".equals(Database.currentUser.role)) {
            addNavItemToContent("staff_tasks", StaffPanel.build(mainFrame));
        } else if ("manager".equals(Database.currentUser.role)) {
            addNavItemToContent("locations", LocationsPanel.build(mainFrame));
            addNavItemToContent("services", ServicesPanel.build(mainFrame));
            addNavItemToContent("staff_tasks", StaffPanel.build(mainFrame));
        }

        cardLayout.show(contentPanel, key);
        // Highlight the active button and reset others
        navButtons.forEach((k, b) -> {
            if (k.equals(key)) {
                b.setForeground(Color.WHITE);
                b.setBackground(new Color(40, 40, 40)); // Dark highlight for selection
            } else {
                b.setForeground(Color.WHITE);
                b.setBackground(Color.BLACK);
            }
        });
    }
    
    private void addNavItemToContent(String key, JPanel panel) {
        contentPanel.add(panel, key);
    }
}
