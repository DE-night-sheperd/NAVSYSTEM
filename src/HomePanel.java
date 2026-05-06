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
        JLabel hl = new JLabel("Welcome, " + Database.currentUser.name);
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
        body.add(Box.createVerticalStrut(20));

        // Recent requests table
        body.add(UIUtils.sectionLabel("Your Recent Requests"));
        body.add(Box.createVerticalStrut(8));
        String[] cols = {"ID","Description","Location","Status","Date"};
        
        java.util.List<Request> myRequests = Database.requests.stream()
            .filter(r -> r.userId == Database.currentUser.userId)
            .sorted((r1, r2) -> r2.requestDate.compareTo(r1.requestDate))
            .limit(10)
            .collect(java.util.stream.Collectors.toList());

        Object[][] data = new Object[myRequests.size()][5];
        for (int i = 0; i < myRequests.size(); i++) {
            Request r = myRequests.get(i);
            Location loc = Database.findLocation(r.locationId);
            data[i] = new Object[]{"REQ-"+String.format("%04d",r.requestId),
                r.description.length()>50?r.description.substring(0,47)+"...":r.description,
                loc != null ? loc.locationName : "N/A", r.status, r.requestDate};
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
