import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;

public class LocationsPanel {
    public static JPanel build(JFrame mainFrame) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.BG);

        JPanel header = UIUtils.pageHeader("Campus Locations", "Search and explore key campus venues");
        root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(UIUtils.BG);
        body.setBorder(BorderFactory.createEmptyBorder(16,20,16,20));

        // Search + filter toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setBackground(UIUtils.BG);
        JTextField search = UIUtils.styledField("Search locations...");
        search.setPreferredSize(new Dimension(240, 34));

        String[] cats = {"All","Lecture Hall","IT Lab","Library","Admin Office","Residence","Support"};
        JComboBox<String> catFilter = new JComboBox<>(cats);
        catFilter.setFont(UIUtils.fontNormal);
        catFilter.setPreferredSize(new Dimension(160, 34));

        JButton addBtn = UIUtils.primaryBtn("+ Add Location");

        toolbar.add(new JLabel("Search:")); toolbar.add(search);
        toolbar.add(new JLabel(" Category:")); toolbar.add(catFilter);
        if ("manager".equals(Database.currentUser.role)) toolbar.add(addBtn);
        body.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID","Venue Name","Building","Category","Latitude","Longitude","QR Code"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        refreshLocTable(model, "", "All");
        JTable table = UIUtils.styledTable(model);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(12,0,0,0),
            BorderFactory.createLineBorder(UIUtils.BORDER)
        ));
        body.add(sp, BorderLayout.CENTER);

        // Refresh on filter
        ActionListener filter = e -> refreshLocTable(model,
            search.getText().equals("Search locations...") ? "" : search.getText(),
            (String) catFilter.getSelectedItem());
        search.addActionListener(filter);
        catFilter.addActionListener(filter);

        // Buttons panel
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        btnRow.setBackground(UIUtils.BG);
        JButton editBtn   = UIUtils.secondaryBtn("Edit Selected");
        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setFont(UIUtils.fontNormal); deleteBtn.setForeground(UIUtils.DANGER);
        deleteBtn.setBackground(UIUtils.lighter(UIUtils.DANGER)); deleteBtn.setBorderPainted(false);
        deleteBtn.setFocusPainted(false); deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteBtn.setBorder(BorderFactory.createEmptyBorder(7,14,7,14));

        if ("manager".equals(Database.currentUser.role)) {
            btnRow.add(editBtn); btnRow.add(deleteBtn);
        }
        body.add(btnRow, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> showLocationDialog(mainFrame, null, model));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(root,"Please select a location."); return; }
            int id = (int) model.getValueAt(row, 0);
            showLocationDialog(mainFrame, Database.findLocation(id), model);
        });
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(root,"Please select a location."); return; }
            int id = (int) model.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(root,"Delete this location?","Confirm",JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                Database.locations.removeIf(l -> l.locationId == id);
                refreshLocTable(model,"","All");
            }
        });

        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(null);
        root.add(bodyScroll, BorderLayout.CENTER);
        return root;
    }

    private static void refreshLocTable(DefaultTableModel m, String search, String cat) {
        m.setRowCount(0);
        for (Location l : Database.locations) {
            boolean matchSearch = search.isEmpty() || l.locationName.toLowerCase().contains(search.toLowerCase())
                || l.building.toLowerCase().contains(search.toLowerCase());
            boolean matchCat = "All".equals(cat) || l.category.equals(cat);
            if (matchSearch && matchCat)
                m.addRow(new Object[]{l.locationId,l.locationName,l.building,l.category,l.latitude,l.longitude,l.qrCodeData});
        }
    }

    private static void showLocationDialog(JFrame mainFrame, Location loc, DefaultTableModel model) {
        boolean isNew = (loc == null);
        JDialog d = new JDialog(mainFrame, isNew ? "Add Location" : "Edit Location", true);
        d.setSize(420, 400);
        d.setLocationRelativeTo(mainFrame);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(20,24,20,24));
        p.setBackground(UIUtils.CARD);

        JTextField nameF = UIUtils.styledField("Location name");
        JTextField bldF  = UIUtils.styledField("Building");
        String[] cats = {"Lecture Hall","IT Lab","Library","Admin Office","Residence","Support"};
        JComboBox<String> catB = new JComboBox<>(cats);
        JTextField latF  = UIUtils.styledField("Latitude e.g. -25.7461");
        JTextField lonF  = UIUtils.styledField("Longitude e.g. 28.1881");
        JTextField qrF   = UIUtils.styledField("QR Code data");

        if (!isNew) {
            nameF.setText(loc.locationName); nameF.setForeground(UIUtils.TEXT1);
            bldF.setText(loc.building);  bldF.setForeground(UIUtils.TEXT1);
            catB.setSelectedItem(loc.category);
            latF.setText(String.valueOf(loc.latitude)); latF.setForeground(UIUtils.TEXT1);
            lonF.setText(String.valueOf(loc.longitude)); lonF.setForeground(UIUtils.TEXT1);
            qrF.setText(loc.qrCodeData); qrF.setForeground(UIUtils.TEXT1);
        }

        for (JComponent c : new JComponent[]{nameF,bldF,catB,latF,lonF,qrF}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        }

        JButton save = UIUtils.primaryBtn(isNew ? "Add Location" : "Save Changes");
        save.setAlignmentX(Component.LEFT_ALIGNMENT);
        save.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        save.addActionListener(e -> {
            try {
                String name = nameF.getText(); String bld = bldF.getText();
                String cat  = (String)catB.getSelectedItem();
                double lat  = Double.parseDouble(latF.getText());
                double lon  = Double.parseDouble(lonF.getText());
                String qr   = qrF.getText();
                if (isNew) {
                    Database.locations.add(new Location(Database.nextLocId++, name, bld, cat, lat, lon, qr));
                } else {
                    loc.locationName = name; loc.building = bld; loc.category = cat;
                    loc.latitude = lat; loc.longitude = lon; loc.qrCodeData = qr;
                }
                refreshLocTable(model, "", "All");
                d.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d, "Invalid latitude/longitude values.");
            }
        });

        p.add(new JLabel(isNew ? "New Location" : "Edit Location")); p.add(Box.createVerticalStrut(12));
        String[] labels = {"Name","Building","Category","Latitude","Longitude","QR Code"};
        JComponent[] fields = {nameF, bldF, catB, latF, lonF, qrF};
        for (int i = 0; i < labels.length; i++) {
            JLabel l = new JLabel(labels[i]); l.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(l); p.add(Box.createVerticalStrut(4)); p.add(fields[i]); p.add(Box.createVerticalStrut(8));
        }
        p.add(save);
        d.setContentPane(new JScrollPane(p));
        d.setVisible(true);
    }
}
