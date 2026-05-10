import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HomePanel {
    public static JPanel build() {
        return build(null);
    }

    public static JPanel build(java.util.function.Consumer<String> pageLoader) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.BG());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtils.NAVY());
        header.setBorder(BorderFactory.createEmptyBorder(24,28,24,28));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(UIUtils.NAVY());
        
        String greeting;
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 12) greeting = "Good Morning";
        else if (hour < 17) greeting = "Good Afternoon";
        else greeting = "Good Evening";

        JLabel hl = new JLabel("");
        hl.setFont(UIUtils.fontTitle); hl.setForeground(Color.WHITE);
        
        JLabel hs = new JLabel("");
        hs.setFont(UIUtils.fontSmall); hs.setForeground(new Color(148,163,184));
        
        titlePanel.add(hl, BorderLayout.NORTH);
        titlePanel.add(hs, BorderLayout.SOUTH);
        header.add(titlePanel, BorderLayout.WEST);

        JButton logBtn = null;
        if ("student".equals(Database.currentUser.role) && pageLoader != null) {
            logBtn = UIUtils.primaryBtn("\u2795 Log New Request");
            logBtn.addActionListener(e -> pageLoader.accept("log_request"));
            header.add(logBtn, BorderLayout.EAST);
        }

        root.add(header, BorderLayout.NORTH);
        
        // Center Panel: Search + Body
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(UIUtils.BG());
        
        // Search Bar
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(UIUtils.BG());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(15, 24, 0, 24));
        
        JTextField searchField = UIUtils.styledField("🔍 Search locations, services...");
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        centerPanel.add(searchPanel, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(UIUtils.BG());
        body.setBorder(BorderFactory.createEmptyBorder(10,24,20,24));
        
        centerPanel.add(body, BorderLayout.CENTER);
        root.add(centerPanel, BorderLayout.CENTER);

        // Stats row
        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setBackground(UIUtils.BG());
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        long open = Database.requests.stream().filter(r -> r.userId == Database.currentUser.userId && "Submitted".equals(r.status)).count();
        long inProg = Database.requests.stream().filter(r -> r.userId == Database.currentUser.userId && "In Progress".equals(r.status)).count();
        long resolved = Database.requests.stream().filter(r -> r.userId == Database.currentUser.userId && "Resolved".equals(r.status)).count();

        stats.add(animatedStatCard(String.valueOf(Database.locations.size()), "Locations", UIUtils.ACCENT, 0));
        stats.add(animatedStatCard(String.valueOf(open),             "My Open", UIUtils.DANGER, 300));
        stats.add(animatedStatCard(String.valueOf(inProg),           "My In Progress", UIUtils.WARNING, 600));
        stats.add(animatedStatCard(String.valueOf(resolved),         "My Resolved", UIUtils.SUCCESS, 900));

        JLabel overviewLabel = UIUtils.sectionLabel("Your Activity Overview");
        overviewLabel.setForeground(UIUtils.TEXT1());
        body.add(overviewLabel);
        body.add(Box.createVerticalStrut(8));
        body.add(stats);
        body.add(Box.createVerticalStrut(25));

        // Quick Actions & Announcements
        JPanel midRow = new JPanel(new GridLayout(1, 2, 20, 0));
        midRow.setBackground(UIUtils.BG());
        midRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        // Quick Actions
        JPanel actions = UIUtils.card();
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        JLabel actTitle = new JLabel("Quick Actions");
        actTitle.setFont(UIUtils.fontBold);
        actions.add(actTitle); actions.add(Box.createVerticalStrut(10));
        
        String[][] actionItems = {
            {"\uD83D\uDCCD Find Nearest Lab", "map"},
            {"\uD83D\uDCDD New Support Ticket", "log_request"},
            {"\uD83D\uDC64 Update Profile", "profile"}
        };

        for (String[] item : actionItems) {
            JButton b = new JButton(item[0]);
            b.setFont(UIUtils.fontNormal);
            b.setForeground(UIUtils.ACCENT);
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            if (pageLoader != null) b.addActionListener(e -> pageLoader.accept(item[1]));
            actions.add(b);
        }
        midRow.add(actions);

        // Announcements
        JPanel announc = UIUtils.card();
        announc.setLayout(new BoxLayout(announc, BoxLayout.Y_AXIS));
        JLabel annTitle = new JLabel("Campus Announcements");
        annTitle.setFont(UIUtils.fontBold);
        announc.add(annTitle); announc.add(Box.createVerticalStrut(10));
        
        String[] news = {
            "\u2022 System maintenance on Sunday 2AM",
            "\u2022 New IT Lab opened in Building C006",
            "\u2022 Library hours extended for exams"
        };
        for (String n : news) {
            JLabel nl = new JLabel(n);
            nl.setFont(UIUtils.fontSmall);
            nl.setForeground(UIUtils.TEXT2);
            nl.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 0));
            announc.add(nl);
        }
        midRow.add(announc);

        body.add(midRow);
        body.add(Box.createVerticalStrut(25));

        // Recent requests table
        JLabel recentLabel = UIUtils.sectionLabel("Your Recent Requests");
        recentLabel.setForeground(UIUtils.TEXT1());
        body.add(recentLabel);
        body.add(Box.createVerticalStrut(8));
        String[] cols = {"ID","Description","Location","Status","Date","Est. Response"};
        
        java.util.List<Request> myRequests = Database.requests.stream()
            .filter(r -> r.userId == Database.currentUser.userId)
            .sorted((r1, r2) -> r2.requestDate.compareTo(r1.requestDate))
            .limit(10)
            .collect(java.util.stream.Collectors.toList());

        Object[][] data = new Object[myRequests.size()][6];
        for (int i = 0; i < myRequests.size(); i++) {
            Request r = myRequests.get(i);
            Location loc = Database.findLocation(r.locationId);
            data[i] = new Object[]{"REQ-"+String.format("%04d",r.requestId),
                r.description.length()>50?r.description.substring(0,47)+"...":r.description,
                loc != null ? loc.locationName : "N/A", r.status, r.requestDate,
                UIUtils.getEstimatedResponse(r)};
        }
        JTable table = UIUtils.styledTable(data, cols);
        UIUtils.styleStatusColumn(table, 3);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER()));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(sp);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scroll, BorderLayout.CENTER);

        // Start all animations after UI is built
        final JButton finalLogBtn = logBtn;
        SwingUtilities.invokeLater(() -> {
            Animations.typeWriterEffect(hl, greeting + ", " + Database.currentUser.name, 75);
            Timer subTimer = new Timer(2000, e -> {
                Animations.typeWriterEffect(hs, "Role: " + Database.currentUser.role.toUpperCase() + "  |  " + UIUtils.today(), 30);
            });
            subTimer.setRepeats(false);
            subTimer.start();
            
            Timer overviewTimer = new Timer(1500, e -> {
                fadeInLabel(overviewLabel, 500);
            });
            overviewTimer.setRepeats(false);
            overviewTimer.start();
            
            Timer recentTimer = new Timer(2800, e -> {
                fadeInLabel(recentLabel, 500);
            });
            recentTimer.setRepeats(false);
            recentTimer.start();
            
            if (finalLogBtn != null) {
                Animations.pulseButton(finalLogBtn, UIUtils.ACCENT, UIUtils.ACCENT2);
            }
        });

        return root;
    }

    private static JPanel animatedStatCard(String num, String label, Color col, int delay) {
        JPanel p = UIUtils.card();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        
        JLabel n = new JLabel("0", SwingConstants.CENTER);
        n.setFont(new Font("SansSerif", Font.BOLD, 30));
        n.setForeground(col);
        n.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel l = new JLabel(label, SwingConstants.CENTER);
        l.setFont(UIUtils.fontSmall);
        l.setForeground(UIUtils.TEXT2);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        p.add(n); p.add(Box.createVerticalStrut(4)); p.add(l);
        
        // Animate counting up
        try {
            final int finalNum = Integer.parseInt(num);
            Timer timer = new Timer(50, new ActionListener() {
                private int current = 0;
                @Override
                public void actionPerformed(ActionEvent e) {
                    current++;
                    n.setText(String.valueOf(current));
                    if (current >= finalNum) {
                        ((Timer)e.getSource()).stop();
                    }
                }
            });
            timer.setInitialDelay(delay);
            timer.start();
        } catch (Exception e) {
            n.setText(num);
        }
        
        return p;
    }
    
    private static void fadeInLabel(JLabel label, int duration) {
        label.setForeground(UIUtils.TEXT2);
        Timer timer = new Timer(30, new ActionListener() {
            float alpha = 0.0f;
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha += 0.05f;
                if (alpha >= 1.0f) {
                    alpha = 1.0f;
                    label.setForeground(UIUtils.TEXT2);
                    ((Timer)e.getSource()).stop();
                }
                label.repaint();
            }
        });
        timer.start();
    }
}
