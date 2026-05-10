import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for Staff members to manage and process incoming service requests.
 * Provides filtering by status and a dialog to update request progress.
 */
public class StaffPanel {
    public static JPanel build(JFrame mainFrame) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.BG);
        root.add(UIUtils.pageHeader("Request Management", "Process and update campus service requests"), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(16, 16));
        body.setBackground(UIUtils.BG);
        body.setBorder(BorderFactory.createEmptyBorder(16,20,16,20));

        // Filters: Allow staff to narrow down requests by their current status
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterRow.setBackground(UIUtils.BG);
        String[] stats = {"All Statuses","Submitted","In Progress","Resolved","Closed"};
        JComboBox<String> statFilter = new JComboBox<>(stats);
        statFilter.setPreferredSize(new Dimension(160, 34));
        filterRow.add(new JLabel("Status Filter:")); filterRow.add(statFilter);
        body.add(filterRow, BorderLayout.NORTH);

        // Table Setup
        String[] cols = {"ID","User","Service","Location","Status","Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r,int c){return false;}
        };
        refreshStaffTable(model, "All Statuses"); // Initial load
        JTable table = UIUtils.styledTable(model);
        UIUtils.styleStatusColumn(table, 4);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER));
        body.add(sp, BorderLayout.CENTER);

        // Action panel: Shows details of selected request and the update button
        JPanel actionPanel = UIUtils.card();
        actionPanel.setLayout(new BorderLayout(15, 0));
        actionPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(12,0,0,0),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER),
                BorderFactory.createEmptyBorder(12,14,12,14)
            )
        ));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 4));
        left.setBackground(UIUtils.CARD);
        JLabel selLbl = new JLabel("Select a request to process");
        selLbl.setFont(UIUtils.fontBold);
        JLabel detLbl = new JLabel(" ");
        detLbl.setFont(UIUtils.fontSmall); detLbl.setForeground(UIUtils.TEXT2);
        left.add(selLbl); left.add(detLbl);
        actionPanel.add(left, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        right.setBackground(UIUtils.CARD);
        JButton updBtn = UIUtils.primaryBtn("Add Update / Change Status");
        updBtn.setEnabled(false); // Disabled until a request is selected
        right.add(updBtn);
        actionPanel.add(right, BorderLayout.EAST);
        body.add(actionPanel, BorderLayout.SOUTH);

        // Listener for status filter changes
        statFilter.addActionListener(e -> {
            refreshStaffTable(model, (String)statFilter.getSelectedItem());
            updBtn.setEnabled(false);
            selLbl.setText("Select a request to process");
            detLbl.setText(" ");
        });

        // Listener for table selection to enable/disable processing
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int row = table.getSelectedRow();
                String reqIdStr = (String) model.getValueAt(row, 0);
                int id = Integer.parseInt(reqIdStr.replace("REQ-", ""));
                Request req = Database.findRequest(id);
                if (req != null) {
                    // Mark as seen if not already
                    if (req.firstSeenDate == null) {
                        req.firstSeenDate = UIUtils.today();
                        Database.saveRequests();
                    }
                    selLbl.setText("Processing: REQ-" + String.format("%04d", req.requestId));
                    detLbl.setText("Description: " + (req.description.length()>60?req.description.substring(0,57)+"...":req.description));
                    updBtn.setEnabled(true);
                }
            }
        });

        // Open the update dialog for the selected request
        updBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            int id = Integer.parseInt(((String) model.getValueAt(row, 0)).replace("REQ-", ""));
            showUpdateDialog(mainFrame, Database.findRequest(id), model, (String)statFilter.getSelectedItem());
        });

        root.add(body, BorderLayout.CENTER);
        return root;
    }

    /**
     * Refreshes the staff table based on the selected status filter.
     */
    private static void refreshStaffTable(DefaultTableModel m, String filter) {
        m.setRowCount(0);
        for (Request r : Database.requests) {
            if ("All Statuses".equals(filter) || r.status.equals(filter)) {
                User u = Database.findUser(r.userId);
                Service s = Database.findService(r.serviceId);
                Location l = Database.findLocation(r.locationId);
                m.addRow(new Object[]{
                    "REQ-"+String.format("%04d",r.requestId),
                    (u!=null?u.name:"ID:"+r.userId),
                    (s!=null?s.serviceName:"N/A"),
                    (l!=null?l.locationName:"N/A"),
                    r.status, r.requestDate
                });
            }
        }
    }

    /**
     * Shows a modal dialog for staff to change status and add progress comments.
     */
    private static void showUpdateDialog(JFrame mainFrame, Request req, DefaultTableModel model, String filter) {
        JDialog d = new JDialog(mainFrame, "Update Request", true);
        d.setSize(450, 420);
        d.setLocationRelativeTo(mainFrame);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(20,24,20,24));
        p.setBackground(UIUtils.CARD());

        JLabel t = new JLabel("Update REQ-" + String.format("%04d", req.requestId));
        t.setFont(UIUtils.fontBold); t.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] stats = {"Submitted","In Progress","Resolved","Closed"};
        JComboBox<String> statBox = new JComboBox<>(stats);
        statBox.setSelectedItem(req.status);
        statBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        statBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Quick reply templates
        JLabel templatesLabel = new JLabel("Quick Reply Templates:");
        templatesLabel.setFont(UIUtils.fontSmall);
        templatesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel templatesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        templatesPanel.setBackground(UIUtils.CARD());
        templatesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        String[] templates = {
            "We've received your request and are reviewing it.",
            "Your issue is currently being worked on.",
            "We've resolved your request! Please check.",
            "Your request has been closed. Thank you!"
        };
        
        JTextArea comm = new JTextArea(4, 20);
        comm.setFont(UIUtils.fontNormal);
        comm.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER()));
        
        for (String template : templates) {
            JButton btn = new JButton("📋 " + (template.length() > 25 ? template.substring(0, 22) + "..." : template));
            btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(true);
            btn.setForeground(UIUtils.TEXT1());
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> comm.setText(template));
            templatesPanel.add(btn);
        }
        
        JScrollPane cs = new JScrollPane(comm);
        cs.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton save = UIUtils.primaryBtn("Submit Update");
        save.setAlignmentX(Component.LEFT_ALIGNMENT);
        save.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        save.addActionListener(e -> {
            String newStat = (String) statBox.getSelectedItem();
            String comment = comm.getText().trim();
            if (comment.isEmpty()) { 
                JOptionPane.showMessageDialog(d,"Please add a comment."); 
                return; 
            }
            
            // Update request status and add a new update record
            req.status = newStat;
            Database.requestUpdates.add(new RequestUpdate(Database.nextUpdId++, req.requestId, Database.currentUser.userId, comment, UIUtils.today()));
            
            // Persist changes to files
            Database.saveRequests();
            Database.saveUpdates();
            
            ToastManager.showSuccess("Request update submitted successfully!");
            
            refreshStaffTable(model, filter); // Refresh view
            d.dispose();
        });

        // Dialog assembly
        p.add(t); p.add(Box.createVerticalStrut(15));
        p.add(new JLabel("Change Status:")); p.add(Box.createVerticalStrut(4));
        p.add(statBox); p.add(Box.createVerticalStrut(12));
        p.add(templatesLabel); p.add(Box.createVerticalStrut(4));
        p.add(templatesPanel); p.add(Box.createVerticalStrut(8));
        p.add(new JLabel("Internal Comment / Progress Update:")); p.add(Box.createVerticalStrut(4));
        p.add(cs); p.add(Box.createVerticalStrut(15));
        p.add(save);

        d.setContentPane(p); d.setVisible(true);
    }
}
