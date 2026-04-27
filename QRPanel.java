import javax.swing.*;
import java.awt.*;

public class QRPanel {
    public static JPanel build() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.BG);
        root.add(UIUtils.pageHeader("QR Code Interaction", "Scan or look up QR codes at campus points"), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(UIUtils.BG);
        body.setBorder(BorderFactory.createEmptyBorder(20,24,20,24));

        // Scanner simulator
        JPanel scanBox = UIUtils.card();
        scanBox.setLayout(new BoxLayout(scanBox, BoxLayout.Y_AXIS));
        scanBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.ACCENT, 2, true),
            BorderFactory.createEmptyBorder(20,20,20,20)
        ));
        scanBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        scanBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel scanIcon = new JLabel("▣  QR Code Scanner", SwingConstants.CENTER);
        scanIcon.setFont(new Font("SansSerif", Font.BOLD, 16)); scanIcon.setForeground(UIUtils.ACCENT);
        scanIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scanSub = new JLabel("Enter or select a QR code to simulate scanning", SwingConstants.CENTER);
        scanSub.setFont(UIUtils.fontSmall); scanSub.setForeground(UIUtils.TEXT2);
        scanSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[] qrOptions = Database.locations.stream().map(l -> l.qrCodeData + " — " + l.locationName).toArray(String[]::new);
        JComboBox<String> qrSelect = new JComboBox<>(qrOptions);
        qrSelect.setFont(UIUtils.fontNormal);
        qrSelect.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrSelect.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JButton scanBtn = UIUtils.primaryBtn("Simulate Scan");
        scanBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        scanBox.add(scanIcon); scanBox.add(Box.createVerticalStrut(8));
        scanBox.add(scanSub); scanBox.add(Box.createVerticalStrut(12));
        scanBox.add(qrSelect); scanBox.add(Box.createVerticalStrut(10));
        scanBox.add(scanBtn);
        body.add(scanBox); body.add(Box.createVerticalStrut(20));

        // Result panel
        JPanel resultPanel = UIUtils.card();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultPanel.setVisible(false);
        body.add(resultPanel);

        scanBtn.addActionListener(e -> {
            int idx = qrSelect.getSelectedIndex();
            if (idx < 0 || idx >= Database.locations.size()) return;
            Location loc = Database.locations.get(idx);
            resultPanel.removeAll();
            resultPanel.setVisible(true);

            JLabel rTitle = new JLabel("Scanned: " + loc.locationName);
            rTitle.setFont(UIUtils.fontBold); rTitle.setForeground(UIUtils.SUCCESS);
            rTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel info = new JLabel("<html><b>Building:</b> " + loc.building +
                "  |  <b>Category:</b> " + loc.category +
                "  |  <b>QR:</b> " + loc.qrCodeData + "</html>");
            info.setFont(UIUtils.fontNormal); info.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel svcTitle = new JLabel("Available services at this location:");
            svcTitle.setFont(UIUtils.fontBold); svcTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel svcRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            svcRow.setBackground(UIUtils.CARD);
            svcRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            boolean hasSvc = false;
            for (Service s : Database.services) {
                if (s.locationId == loc.locationId) {
                    svcRow.add(UIUtils.badge(s.serviceName, UIUtils.ACCENT)); hasSvc = true;
                }
            }
            if (!hasSvc) svcRow.add(new JLabel("No services linked to this location"));

            resultPanel.add(rTitle); resultPanel.add(Box.createVerticalStrut(8));
            resultPanel.add(info); resultPanel.add(Box.createVerticalStrut(12));
            resultPanel.add(svcTitle); resultPanel.add(Box.createVerticalStrut(6));
            resultPanel.add(svcRow);
            resultPanel.revalidate(); resultPanel.repaint();
        });

        // QR table
        body.add(Box.createVerticalStrut(20));
        body.add(UIUtils.sectionLabel("All QR Codes"));
        body.add(Box.createVerticalStrut(8));
        String[] cols = {"Location","Building","QR Code Data","Category"};
        Object[][] data = new Object[Database.locations.size()][4];
        for (int i = 0; i < Database.locations.size(); i++) {
            Location l = Database.locations.get(i);
            data[i] = new Object[]{l.locationName, l.building, l.qrCodeData, l.category};
        }
        JTable t = UIUtils.styledTable(data, cols);
        JScrollPane sp = new JScrollPane(t);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        sp.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER));
        body.add(sp);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        root.add(scroll, BorderLayout.CENTER);
        return root;
    }
}
