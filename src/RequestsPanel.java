import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RequestsPanel {
    public static JPanel build() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.BG);
        root.add(UIUtils.pageHeader("Service Requests", "Submit and track your campus requests"), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIUtils.fontBold);
        tabs.setBackground(UIUtils.BG);

        tabs.addTab("My Requests",  buildMyRequestsTab());
        tabs.addTab("New Request",  buildNewRequestTab(tabs));

        root.add(tabs, BorderLayout.CENTER);
        return root;
    }

    private static JPanel buildMyRequestsTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIUtils.BG);
        p.setBorder(BorderFactory.createEmptyBorder(16,20,16,20));

        String[] cols = {"Req #","Description","Location","Service","Status","Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r,int c){return false;}
        };

        List<Request> mine = new ArrayList<>();
        for (Request r : Database.requests) if (r.userId == Database.currentUser.userId) mine.add(r);

        for (Request r : mine) {
            Location loc = Database.findLocation(r.locationId);
            Service  svc = Database.findService(r.serviceId);
            model.addRow(new Object[]{
                "REQ-"+String.format("%04d",r.requestId),
                r.description.length()>45?r.description.substring(0,42)+"...":r.description,
                loc!=null?loc.locationName:"N/A",
                svc!=null?svc.serviceName:"N/A",
                r.status, r.requestDate
            });
        }

        JTable table = UIUtils.styledTable(model);
        UIUtils.styleStatusColumn(table, 4);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER));
        p.add(sp, BorderLayout.CENTER);

        // Updates panel below
        JPanel updPanel = UIUtils.card();
        updPanel.setLayout(new BoxLayout(updPanel, BoxLayout.Y_AXIS));
        updPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(12,0,0,0),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER),
                BorderFactory.createEmptyBorder(12,14,12,14)
            )
        ));
        JLabel updTitle = new JLabel("Select a request to view updates");
        updTitle.setFont(UIUtils.fontBold); updTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        updPanel.add(updTitle);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int row = table.getSelectedRow();
                if (row >= mine.size()) return;
                Request req = mine.get(row);
                updPanel.removeAll();
                JLabel lbl = new JLabel("Updates for " + "REQ-" + String.format("%04d",req.requestId) + ": " + req.description.substring(0,Math.min(40,req.description.length())));
                lbl.setFont(UIUtils.fontBold); lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                updPanel.add(lbl); updPanel.add(Box.createVerticalStrut(8));

                // Status timeline
                String[] steps = {"Submitted","In Progress","Resolved","Closed"};
                int curr = 0;
                for (int i = 0; i < steps.length; i++) if (steps[i].equals(req.status)) curr = i;
                JPanel timeline = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                timeline.setBackground(UIUtils.CARD);
                for (int i = 0; i < steps.length; i++) {
                    JLabel step = new JLabel(" " + steps[i] + " ");
                    step.setFont(i == curr ? UIUtils.fontBold : UIUtils.fontSmall);
                    step.setForeground(i <= curr ? UIUtils.ACCENT : UIUtils.TEXT2);
                    step.setOpaque(true);
                    step.setBackground(i == curr ? UIUtils.lighter(UIUtils.ACCENT) : UIUtils.CARD);
                    step.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(i <= curr ? UIUtils.ACCENT : UIUtils.BORDER),
                        BorderFactory.createEmptyBorder(4,8,4,8)
                    ));
                    timeline.add(step);
                    if (i < steps.length-1) {
                        JLabel arrow = new JLabel(" → ");
                        arrow.setFont(UIUtils.fontSmall); arrow.setForeground(UIUtils.TEXT2);
                        timeline.add(arrow);
                    }
                }
                updPanel.add(timeline); updPanel.add(Box.createVerticalStrut(10));

                boolean hasUpd = false;
                for (RequestUpdate u : Database.requestUpdates) {
                    if (u.requestId == req.requestId) {
                        hasUpd = true;
                        User staff = Database.findUser(u.staffId);
                        JPanel card2 = new JPanel(new BorderLayout());
                        card2.setBackground(UIUtils.lighter(new Color(59,130,246)));
                        card2.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(UIUtils.BORDER),
                            BorderFactory.createEmptyBorder(8,10,8,10)
                        ));
                        JLabel by = new JLabel("Staff: " + (staff!=null?staff.fullName:"Unknown") + " — " + u.updateDate);
                        by.setFont(UIUtils.fontSmall); by.setForeground(UIUtils.TEXT2);
                        JLabel comment = new JLabel("<html>" + u.comment + "</html>");
                        comment.setFont(UIUtils.fontNormal);
                        card2.add(by, BorderLayout.NORTH);
                        card2.add(comment, BorderLayout.CENTER);
                        card2.setAlignmentX(Component.LEFT_ALIGNMENT);
                        updPanel.add(card2); updPanel.add(Box.createVerticalStrut(6));
                    }
                }
                if (!hasUpd) {
                    JLabel noUpd = new JLabel("No updates yet.");
                    noUpd.setFont(UIUtils.fontSmall); noUpd.setForeground(UIUtils.TEXT2);
                    noUpd.setAlignmentX(Component.LEFT_ALIGNMENT);
                    updPanel.add(noUpd);
                }
                updPanel.revalidate(); updPanel.repaint();
            }
        });

        p.add(updPanel, BorderLayout.SOUTH);
        return p;
    }

    private static JPanel buildNewRequestTab(JTabbedPane tabs) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UIUtils.BG);
        p.setBorder(BorderFactory.createEmptyBorder(20,24,20,24));

        JPanel form = UIUtils.card();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.setMaximumSize(new Dimension(600, 9999));

        JLabel title = new JLabel("Submit a New Request");
        title.setFont(UIUtils.fontBold); title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> svcBox = new JComboBox<>(Database.services.stream().map(s->s.serviceName+" — "+s.department).toArray(String[]::new));
        svcBox.setFont(UIUtils.fontNormal); svcBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        svcBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JComboBox<String> locBox = new JComboBox<>(Database.locations.stream().map(l->l.locationName+" ("+l.building+")").toArray(String[]::new));
        locBox.setFont(UIUtils.fontNormal); locBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        locBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JTextArea desc = new JTextArea(4, 30);
        desc.setFont(UIUtils.fontNormal);
        desc.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.BORDER),
            BorderFactory.createEmptyBorder(6,10,6,10)
        ));
        JScrollPane descScroll = new JScrollPane(desc);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JLabel msg = new JLabel(" "); msg.setFont(UIUtils.fontSmall); msg.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton submit = UIUtils.primaryBtn("Submit Request");
        submit.setAlignmentX(Component.LEFT_ALIGNMENT);
        submit.setMaximumSize(new Dimension(200, 38));
        submit.addActionListener(e -> {
            if (desc.getText().trim().isEmpty()) { msg.setForeground(UIUtils.DANGER); msg.setText("Please enter a description."); return; }
            int svcIdx = svcBox.getSelectedIndex();
            int locIdx = locBox.getSelectedIndex();
            Request r = new Request(Database.nextReqId++, Database.currentUser.userId,
                Database.services.get(svcIdx).serviceId,
                Database.locations.get(locIdx).locationId,
                desc.getText().trim(), "Submitted", UIUtils.today());
            Database.requests.add(r);
            msg.setForeground(UIUtils.SUCCESS);
            msg.setText("Request submitted successfully! REQ-"+String.format("%04d",r.requestId));
            desc.setText("");
        });

        form.add(title); form.add(Box.createVerticalStrut(16));
        String[] lbs = {"Service Type","Location","Description of Issue"};
        JComponent[] flds = {svcBox, locBox, descScroll};
        for (int i = 0; i < lbs.length; i++) {
            JLabel l = new JLabel(lbs[i]); l.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(l); form.add(Box.createVerticalStrut(4));
            form.add(flds[i]); form.add(Box.createVerticalStrut(10));
        }
        form.add(msg); form.add(Box.createVerticalStrut(6)); form.add(submit);

        p.add(form);
        return p;
    }
}
