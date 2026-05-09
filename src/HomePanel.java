import javax.swing.*;
import java.awt.*;

public class HomePanel {
    public static JPanel build() {
        return build(null);
    }

    public static JPanel build(java.util.function.Consumer<String> pageLoader) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.BG);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtils.NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(24,28,24,28));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(UIUtils.NAVY);
        
        String greeting = "Welcome";
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 12) greeting = "Good Morning";
        else if (hour < 17) greeting = "Good Afternoon";
        else greeting = "Good Evening";

        JLabel hl = new JLabel(greeting + ", " + Database.currentUser.name);
        hl.setFont(UIUtils.fontTitle); hl.setForeground(Color.WHITE);
        JLabel hs = new JLabel("Role: " + Database.currentUser.role.toUpperCase() + "  |  " + UIUtils.today());
        hs.setFont(UIUtils.fontSmall); hs.setForeground(new Color(148,163,184));
        titlePanel.add(hl, BorderLayout.NORTH);
        titlePanel.add(hs, BorderLayout.SOUTH);
        header.add(titlePanel, BorderLayout.WEST);

        if ("student".equals(Database.currentUser.role) && pageLoader != null) {
            JButton logBtn = UIUtils.primaryBtn("\u2795 Log New Request");
            logBtn.addActionListener(e -> pageLoader.accept("log_request"));
            header.add(logBtn, BorderLayout.EAST);
        }

        root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(UIUtils.BG);
        body.setBorder(BorderFactory.createEmptyBorder(20,24,20,24));

        // Stats row
        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setBackground(UIUtils.BG);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        long open = Database.requests.stream().filter(r -> r.userId == Database.currentUser.userId && "Submitted".equals(r.status)).count();
        long inProg = Database.requests.stream().filter(r -> r.userId == Database.currentUser.userId && "In Progress".equals(r.status)).count();
        long resolved = Database.requests.stream().filter(r -> r.userId == Database.currentUser.userId && "Resolved".equals(r.status)).count();

        stats.add(statCard(String.valueOf(Database.locations.size()), "Locations", UIUtils.ACCENT));
        stats.add(statCard(String.valueOf(open),             "My Open", UIUtils.DANGER));
        stats.add(statCard(String.valueOf(inProg),           "My In Progress", UIUtils.WARNING));
        stats.add(statCard(String.valueOf(resolved),         "My Resolved", UIUtils.SUCCESS));

        body.add(UIUtils.sectionLabel("Your Activity Overview"));
        body.add(Box.createVerticalStrut(8));
        body.add(stats);
        body.add(Box.createVerticalStrut(25));

        // Quick Actions & Announcements
        JPanel midRow = new JPanel(new GridLayout(1, 2, 20, 0));
        midRow.setBackground(UIUtils.BG);
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
        body.add(UIUtils.sectionLabel("Your Recent Requests"));
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
        sp.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(sp);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scroll, BorderLayout.CENTER);
        return root;
    }

    private static JPanel statCard(String num, String label, Color col) {
        JPanel p = UIUtils.card();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel n = new JLabel(num, SwingConstants.CENTER);
        n.setFont(new Font("SansSerif", Font.BOLD, 30));
        n.setForeground(col);
        n.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l = new JLabel(label, SwingConstants.CENTER);
        l.setFont(UIUtils.fontSmall);
        l.setForeground(UIUtils.TEXT2);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(n); p.add(Box.createVerticalStrut(4)); p.add(l);
        return p;
    }
}
