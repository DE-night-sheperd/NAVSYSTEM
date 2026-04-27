import javax.swing.*;
import java.awt.*;

public class NavigationPanel {
    public static JPanel build() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.BG);
        root.add(UIUtils.pageHeader("Campus Navigation", "View venue locations and get directions"), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(16, 16));
        body.setBackground(UIUtils.BG);
        body.setBorder(BorderFactory.createEmptyBorder(16,20,16,20));

        // Left: location list
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(UIUtils.BG);
        left.setPreferredSize(new Dimension(280, 500));

        JTextField search = UIUtils.styledField("Search venue...");
        search.setAlignmentX(Component.LEFT_ALIGNMENT);
        search.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        left.add(search); left.add(Box.createVerticalStrut(10));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (Location l : Database.locations) listModel.addElement(l.locationName + " — " + l.category);
        JList<String> locList = new JList<>(listModel);
        locList.setFont(UIUtils.fontNormal);
        locList.setSelectionBackground(UIUtils.lighter(UIUtils.ACCENT));
        locList.setSelectionForeground(UIUtils.ACCENT.darker());
        locList.setFixedCellHeight(38);
        locList.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
        JScrollPane listScroll = new JScrollPane(locList);
        listScroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER));
        listScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(listScroll);
        body.add(left, BorderLayout.WEST);

        // Right: map + details
        JPanel right = new JPanel(new BorderLayout(0, 12));
        right.setBackground(UIUtils.BG);

        // Canvas map
        MapCanvas mapCanvas = new MapCanvas();
        mapCanvas.setPreferredSize(new Dimension(600, 340));
        mapCanvas.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER));
        right.add(mapCanvas, BorderLayout.CENTER);

        // Details card
        JPanel details = UIUtils.card();
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        JLabel detTitle = new JLabel("Select a location to see details");
        detTitle.setFont(UIUtils.fontBold); detTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel detInfo  = new JLabel(" ");
        detInfo.setFont(UIUtils.fontNormal); detInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel detCoord = new JLabel(" ");
        detCoord.setFont(UIUtils.fontSmall); detCoord.setForeground(UIUtils.TEXT2);
        detCoord.setAlignmentX(Component.LEFT_ALIGNMENT);
        details.add(detTitle); details.add(Box.createVerticalStrut(6));
        details.add(detInfo);  details.add(Box.createVerticalStrut(4));
        details.add(detCoord);
        right.add(details, BorderLayout.SOUTH);
        body.add(right, BorderLayout.CENTER);

        locList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && locList.getSelectedIndex() >= 0) {
                Location loc = Database.locations.get(locList.getSelectedIndex());
                mapCanvas.highlight(loc);
                detTitle.setText(loc.locationName + " — " + loc.category);
                detInfo.setText("Building: " + loc.building + "   QR: " + loc.qrCodeData);
                detCoord.setText("Lat: " + loc.latitude + "   Lon: " + loc.longitude);
            }
        });

        search.addActionListener(e -> {
            String q = search.getText().toLowerCase();
            listModel.clear();
            for (Location l : Database.locations) {
                if (l.locationName.toLowerCase().contains(q) || l.category.toLowerCase().contains(q))
                    listModel.addElement(l.locationName + " — " + l.category);
            }
        });

        root.add(body, BorderLayout.CENTER);
        return root;
    }
}
