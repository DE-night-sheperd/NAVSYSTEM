import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ProfilePanel {

    public static JPanel build() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("My Profile");
        title.setFont(UIUtils.fontTitle);
        title.setForeground(UIUtils.TEXT1);
        header.add(title, BorderLayout.WEST);

        // Animated Avatar/Icon
        JPanel avatarPanel = new JPanel() {
            private float scale = 1.0f;
            private boolean growing = true;
            private final Timer anim = new Timer(50, e -> {
                if (growing) {
                    scale += 0.02f;
                    if (scale >= 1.1f) growing = false;
                } else {
                    scale -= 0.02f;
                    if (scale <= 1.0f) growing = true;
                }
                repaint();
            });

            {
                anim.start();
                setOpaque(false);
                setPreferredSize(new Dimension(100, 100));
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = (int)(80 * scale);
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                g2.setColor(UIUtils.PRIMARY);
                g2.fillOval(x, y, size, size);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, (int)(32 * scale)));
                String initial = Database.currentUser != null ? Database.currentUser.name.substring(0, 1).toUpperCase() : "?";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial, x + (size - fm.stringWidth(initial)) / 2, y + (size + fm.getAscent()) / 2 - 4);
            }
        };

        header.add(avatarPanel, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(UIUtils.CARD);
        content.setBorder(BorderFactory.createEmptyBorder(30, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 0, 5, 20);

        JLabel nameLabel = new JLabel("Full Name");
        nameLabel.setFont(UIUtils.fontSmall);
        nameLabel.setForeground(UIUtils.TEXT2);
        content.add(nameLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 20);
        JTextField nameField = UIUtils.textField(Database.currentUser != null ? Database.currentUser.name : "");
        nameField.setPreferredSize(new Dimension(300, 40));
        content.add(nameField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 5, 20);
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(UIUtils.fontSmall);
        emailLabel.setForeground(UIUtils.TEXT2);
        content.add(emailLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 20);
        JTextField emailField = UIUtils.textField(Database.currentUser != null ? Database.currentUser.email : "");
        emailField.setPreferredSize(new Dimension(300, 40));
        emailField.setEditable(false); // Email usually isn't editable
        content.add(emailField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 5, 20);
        JLabel roleLabel = new JLabel("Role");
        roleLabel.setFont(UIUtils.fontSmall);
        roleLabel.setForeground(UIUtils.TEXT2);
        content.add(roleLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 20);
        JTextField roleField = UIUtils.textField(Database.currentUser != null ? Database.currentUser.role.substring(0, 1).toUpperCase() + Database.currentUser.role.substring(1) : "");
        roleField.setPreferredSize(new Dimension(300, 40));
        roleField.setEditable(false);
        content.add(roleField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 0, 20);
        JButton saveBtn = UIUtils.primaryBtn("Update Profile");
        
        // Add hover animation for button
        saveBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                saveBtn.setBackground(UIUtils.PRIMARY.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                saveBtn.setBackground(UIUtils.PRIMARY);
            }
        });

        saveBtn.addActionListener(e -> {
            if (Database.currentUser != null) {
                String newName = nameField.getText().trim();
                if (!newName.isEmpty()) {
                    Database.currentUser.name = newName;
                    Database.saveUsers();
                    JOptionPane.showMessageDialog(root, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    root.revalidate();
                    root.repaint();
                } else {
                    JOptionPane.showMessageDialog(root, "Name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        content.add(saveBtn, gbc);

        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 50));
        centerWrapper.setOpaque(false);
        centerWrapper.add(content);

        root.add(centerWrapper, BorderLayout.CENTER);
        return root;
    }
}