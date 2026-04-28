import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NavigationPanel {
    public static JPanel build() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.BG);
        root.add(UIUtils.pageHeader("Campus Navigation", "View Sol Plaatje University venues and get directions"), BorderLayout.NORTH);

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
        List<Location> currentLocations = new ArrayList<>(Database.locations);
        
        for (Location l : currentLocations) listModel.addElement(l.locationName + " — " + l.category);
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
        details.setLayout(new BorderLayout());
        
        JPanel detailsText = new JPanel();
        detailsText.setLayout(new BoxLayout(detailsText, BoxLayout.Y_AXIS));
        detailsText.setBackground(UIUtils.CARD);
        
        JLabel detTitle = new JLabel("Select a location to see details");
        detTitle.setFont(UIUtils.fontBold); detTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel detInfo  = new JLabel(" ");
        detInfo.setFont(UIUtils.fontNormal); detInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel detCoord = new JLabel(" ");
        detCoord.setFont(UIUtils.fontSmall); detCoord.setForeground(UIUtils.TEXT2);
        detCoord.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsText.add(detTitle); detailsText.add(Box.createVerticalStrut(6));
        detailsText.add(detInfo);  detailsText.add(Box.createVerticalStrut(4));
        detailsText.add(detCoord);
        
        details.add(detailsText, BorderLayout.CENTER);
        
        // Buttons and Voice Guide
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
        btnPanel.setBackground(UIUtils.CARD);
        
        JCheckBox voiceToggle = new JCheckBox("Enable Voice Guide");
        voiceToggle.setBackground(UIUtils.CARD);
        voiceToggle.setFont(UIUtils.fontSmall);
        voiceToggle.addActionListener(e -> VoiceGuide.isEnabled = voiceToggle.isSelected());
        
        JButton navBtn = UIUtils.primaryBtn("Navigate Here");
        navBtn.setEnabled(false);
        navBtn.addActionListener(e -> {
            if (locList.getSelectedIndex() >= 0) {
                Location loc = currentLocations.get(locList.getSelectedIndex());
                mapCanvas.startNavigation(loc);
            }
        });

        JButton toggleView = UIUtils.secondaryBtn("Switch to Map View");
        toggleView.addActionListener(e -> {
            if (toggleView.getText().contains("Map")) {
                mapCanvas.setSatelliteView(false);
                toggleView.setText("Switch to Satellite View");
            } else {
                mapCanvas.setSatelliteView(true);
                toggleView.setText("Switch to Map View");
            }
        });
        
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonRow.setBackground(UIUtils.CARD);
        buttonRow.add(toggleView);
        buttonRow.add(navBtn);
        
        JPanel voiceRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        voiceRow.setBackground(UIUtils.CARD);
        voiceRow.add(voiceToggle);

        btnPanel.add(voiceRow);
        btnPanel.add(Box.createVerticalStrut(5));
        btnPanel.add(buttonRow);
        
        details.add(btnPanel, BorderLayout.EAST);

        right.add(details, BorderLayout.SOUTH);
        body.add(right, BorderLayout.CENTER);

        locList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && locList.getSelectedIndex() >= 0) {
                Location loc = currentLocations.get(locList.getSelectedIndex());
                mapCanvas.highlight(loc);
                mapCanvas.stopNavigation();
                detTitle.setText(loc.locationName + " — " + loc.category);
                detInfo.setText("Building: " + loc.building + "   QR: " + loc.qrCodeData);
                detCoord.setText("Lat: " + loc.latitude + "   Lon: " + loc.longitude);
                navBtn.setEnabled(true);
            } else {
                navBtn.setEnabled(false);
            }
        });

        search.addActionListener(e -> {
            String q = search.getText().toLowerCase();
            listModel.clear();
            currentLocations.clear();
            for (Location l : Database.locations) {
                if (l.locationName.toLowerCase().contains(q) || l.category.toLowerCase().contains(q)) {
                    listModel.addElement(l.locationName + " — " + l.category);
                    currentLocations.add(l);
                }
            }
        });

        root.add(body, BorderLayout.CENTER);
        return root;
    }
}
