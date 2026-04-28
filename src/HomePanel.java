import javax.swing.*;
import java.awt.*;

public class HomePanel {
    public static JPanel build() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.BG);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtils.NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(24,28,24,28));
        JLabel hl = new JLabel("Welcome, " + Database.currentUser.name);
        hl.setFont(UIUtils.fontTitle); hl.setForeground(Color.WHITE);
        JLabel hs = new JLabel("Role: " + Database.currentUser.role.toUpperCase() + "  |  " + UIUtils.today());
        hs.setFont(UIUtils.fontSmall); hs.setForeground(new Color(148,163,184));
        header.add(hl, BorderLayout.NORTH);
        header.add(hs, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(UIUtils.BG);
        body.setBorder(BorderFactory.createEmptyBorder(20,24,20,24));

        // Stats row
        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setBackground(UIUtils.BG);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        long open = Database.requests.stream().filter(r -> "Submitted".equals(r.status)).count();
        long inProg = Database.requests.stream().filter(r -> "In Progress".equals(r.status)).count();
        long resolved = Database.requests.stream().filter(r -> "Resolved".equals(r.status)).count();

        stats.add(statCard(String.valueOf(Database.locations.size()), "Locations", UIUtils.ACCENT));
        stats.add(statCard(String.valueOf(open),             "Open Requests", UIUtils.DANGER));
        stats.add(statCard(String.valueOf(inProg),           "In Progress", UIUtils.WARNING));
        stats.add(statCard(String.valueOf(resolved),         "Resolved", UIUtils.SUCCESS));

        body.add(UIUtils.sectionLabel("System Overview"));
        body.add(Box.createVerticalStrut(8));
        body.add(stats);
        body.add(Box.createVerticalStrut(20));

        // Recent requests table
        body.add(UIUtils.sectionLabel("Recent Requests"));
        body.add(Box.createVerticalStrut(8));
        String[] cols = {"ID","Description","Location","Status","Date"};
        Object[][] data = new Object[Database.requests.size()][5];
        for (int i = 0; i < Database.requests.size(); i++) {
            Request r = Database.requests.get(i);
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
