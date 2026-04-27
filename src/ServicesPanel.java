import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ServicesPanel {
    public static JPanel build(JFrame mainFrame) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.BG);
        root.add(UIUtils.pageHeader("Manage Services", "Define and monitor campus service units"), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(16, 16));
        body.setBackground(UIUtils.BG);
        body.setBorder(BorderFactory.createEmptyBorder(16,20,16,20));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(UIUtils.BG);
        JButton addBtn = UIUtils.primaryBtn("+ Add Service");
        toolbar.add(addBtn);
        body.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID","Service Name","Department","Location","Contact"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r,int c){return false;}
        };
        refreshSvcTable(model);
        JTable table = UIUtils.styledTable(model);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER));
        body.add(sp, BorderLayout.CENTER);

        // Action row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        btnRow.setBackground(UIUtils.BG);
        JButton editBtn = UIUtils.secondaryBtn("Edit Selected");
        JButton delBtn  = new JButton("Delete");
        delBtn.setFont(UIUtils.fontNormal); delBtn.setForeground(UIUtils.DANGER);
        delBtn.setBackground(UIUtils.lighter(UIUtils.DANGER)); delBtn.setBorderPainted(false);
        delBtn.setFocusPainted(false); delBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        delBtn.setBorder(BorderFactory.createEmptyBorder(7,14,7,14));

        btnRow.add(editBtn); btnRow.add(delBtn);
        body.add(btnRow, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> showSvcDialog(mainFrame, null, model));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(root,"Select a service."); return; }
            int id = (int) model.getValueAt(row, 0);
            showSvcDialog(mainFrame, Database.findService(id), model);
        });
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(root,"Select a service."); return; }
            int id = (int) model.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(root,"Delete?","Confirm",JOptionPane.YES_NO_OPTION) == 0) {
                Database.services.removeIf(s -> s.serviceId == id);
                refreshSvcTable(model);
            }
        });

        root.add(body, BorderLayout.CENTER);
        return root;
    }

    private static void refreshSvcTable(DefaultTableModel m) {
        m.setRowCount(0);
        for (Service s : Database.services) {
            Location loc = Database.findLocation(s.locationId);
            m.addRow(new Object[]{s.serviceId, s.serviceName, s.department, (loc!=null?loc.locationName:"N/A"), s.contactInfo});
        }
    }

    private static void showSvcDialog(JFrame mainFrame, Service svc, DefaultTableModel model) {
        boolean isNew = (svc == null);
        JDialog d = new JDialog(mainFrame, isNew ? "Add Service" : "Edit Service", true);
        d.setSize(400, 360);
        d.setLocationRelativeTo(mainFrame);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(20,24,20,24));
        p.setBackground(UIUtils.CARD);

        JTextField nameF = UIUtils.styledField("Service name");
        JTextField descF = UIUtils.styledField("Description");
        JTextField deptF = UIUtils.styledField("Department");
        JComboBox<String> locBox = new JComboBox<>(Database.locations.stream().map(l->l.locationName).toArray(String[]::new));
        JTextField contactF = UIUtils.styledField("Contact info");

        if (!isNew) {
            nameF.setText(svc.serviceName); nameF.setForeground(UIUtils.TEXT1);
            descF.setText(svc.description); descF.setForeground(UIUtils.TEXT1);
            deptF.setText(svc.department); deptF.setForeground(UIUtils.TEXT1);
            Location l = Database.findLocation(svc.locationId);
            if (l!=null) locBox.setSelectedItem(l.locationName);
            contactF.setText(svc.contactInfo); contactF.setForeground(UIUtils.TEXT1);
        }

        JButton save = UIUtils.primaryBtn("Save Service");
        save.setAlignmentX(Component.LEFT_ALIGNMENT);
        save.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        save.addActionListener(e -> {
            String name = nameF.getText(); 
            String desc = descF.getText();
            String dept = deptF.getText();
            int locId = Database.locations.get(locBox.getSelectedIndex()).locationId;
            String cont = contactF.getText();
            if (isNew) Database.services.add(new Service(Database.nextSvcId++, name, desc, dept, locId, cont));
            else { 
                svc.serviceName = name; 
                svc.description = desc;
                svc.department = dept; 
                svc.locationId = locId; 
                svc.contactInfo = cont; 
            }
            refreshSvcTable(model);
            d.dispose();
        });

        String[] lbs = {"Name","Description","Department","Location","Contact"};
        JComponent[] flds = {nameF, descF, deptF, locBox, contactF};
        for (int i = 0; i < lbs.length; i++) {
            p.add(new JLabel(lbs[i])); p.add(Box.createVerticalStrut(4));
            flds[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            flds[i].setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            p.add(flds[i]); p.add(Box.createVerticalStrut(8));
        }
        p.add(Box.createVerticalStrut(10)); p.add(save);

        d.setContentPane(p); d.setVisible(true);
    }
}
