import javax.swing.*;
import java.awt.*;

public class QRPanel {
    static class QRGraphic extends JPanel {
        private String currentData = "SCAN-ME";
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int size = Math.min(getWidth(), getHeight()) - 40;
            if (size < 100) size = 100;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Background
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // QR Border
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(x - 5, y - 5, size + 10, size + 10);

            int cells = 21; // Standard QR-like grid
            int cellSize = size / cells;
            int startX = x + (size - (cells * cellSize)) / 2;
            int startY = y + (size - (cells * cellSize)) / 2;
            
            long seed = currentData.hashCode();
            java.util.Random rnd = new java.util.Random(seed);
            
            for (int row = 0; row < cells; row++) {
                for (int col = 0; col < cells; col++) {
                    // Position Detection Patterns (the 3 large squares)
                    if (isPositionPattern(row, col, cells)) {
                        g2.setColor(Color.BLACK);
                        if (isOuterPattern(row, col, cells)) {
                            g2.fillRect(startX + col*cellSize, startY + row*cellSize, cellSize, cellSize);
                        } else if (isInnerPattern(row, col, cells)) {
                            g2.fillRect(startX + col*cellSize, startY + row*cellSize, cellSize, cellSize);
                        }
                        continue;
                    }
                    
                    // Random-ish modules based on data
                    if (rnd.nextBoolean()) {
                        g2.setColor(Color.BLACK);
                        g2.fillRect(startX + col*cellSize, startY + row*cellSize, cellSize, cellSize);
                    }
                }
            }
        }

        private boolean isPositionPattern(int r, int c, int cells) {
            return (r < 7 && c < 7) || (r < 7 && c > cells-8) || (r > cells-8 && c < 7);
        }

        private boolean isOuterPattern(int r, int c, int cells) {
            // Check for the 7x7 outer square
            if (r < 7 && c < 7) return (r == 0 || r == 6 || c == 0 || c == 6);
            if (r < 7 && c > cells-8) return (r == 0 || r == 6 || c == cells-7 || c == cells-1);
            if (r > cells-8 && c < 7) return (r == cells-7 || r == cells-1 || c == 0 || c == 6);
            return false;
        }

        private boolean isInnerPattern(int r, int c, int cells) {
            // Check for the 3x3 inner square
            if (r >= 2 && r <= 4 && c >= 2 && c <= 4) return true;
            if (r >= 2 && r <= 4 && c >= cells-5 && c <= cells-3) return true;
            if (r >= cells-5 && r <= cells-3 && c >= 2 && c <= 4) return true;
            return false;
        }

        public void setData(String data) { this.currentData = data; repaint(); }
    }

    public static JPanel build() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.BG);
        root.add(UIUtils.pageHeader("QR Interactions", "Scan and Generate Scannable Codes"), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(UIUtils.BG);
        body.setBorder(BorderFactory.createEmptyBorder(20,24,20,24));

        JPanel qrContainer = new JPanel(new GridLayout(1, 2, 20, 0));
        qrContainer.setBackground(UIUtils.BG);
        qrContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        JPanel scanBox = UIUtils.card();
        scanBox.setLayout(new BoxLayout(scanBox, BoxLayout.Y_AXIS));
        
        JLabel scanIcon = new JLabel("▣  Scanner", SwingConstants.CENTER);
        scanIcon.setFont(new Font("SansSerif", Font.BOLD, 18)); scanIcon.setForeground(UIUtils.ACCENT);
        scanIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scanSub = new JLabel("Select a point to scan", SwingConstants.CENTER);
        scanSub.setFont(UIUtils.fontSmall); scanSub.setForeground(UIUtils.TEXT2);
        scanSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[] qrOptions = Database.locations.stream().map(l -> l.qrCodeData).toArray(String[]::new);
        JComboBox<String> qrSelect = new JComboBox<>(qrOptions);
        qrSelect.setFont(UIUtils.fontNormal);
        qrSelect.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrSelect.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JButton scanBtn = UIUtils.primaryBtn("Process Scan");
        scanBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        scanBox.add(scanIcon); scanBox.add(Box.createVerticalStrut(10));
        scanBox.add(scanSub); scanBox.add(Box.createVerticalStrut(15));
        scanBox.add(qrSelect); scanBox.add(Box.createVerticalStrut(15));
        scanBox.add(scanBtn);

        JLabel qrImageLabel = new JLabel("", SwingConstants.CENTER);
        JPanel genBox = UIUtils.card();
        genBox.setLayout(new BorderLayout());
        genBox.add(new JLabel("Code Preview", SwingConstants.CENTER), BorderLayout.NORTH);
        genBox.add(qrImageLabel, BorderLayout.CENTER);
        
        qrContainer.add(scanBox);
        qrContainer.add(genBox);
        body.add(qrContainer); body.add(Box.createVerticalStrut(20));

        JPanel resultPanel = UIUtils.card();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultPanel.setVisible(false);
        body.add(resultPanel);

        scanBtn.addActionListener(e -> {
            int idx = qrSelect.getSelectedIndex();
            if (idx < 0 || idx >= Database.locations.size()) return;
            Location loc = Database.locations.get(idx);
            ImageIcon qrIcon = new ImageIcon("src/resources/qrcodes/" + loc.qrCodeData + ".png");
            Image scaled = qrIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            qrImageLabel.setIcon(new ImageIcon(scaled));
            
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
