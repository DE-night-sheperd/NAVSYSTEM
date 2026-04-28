import javax.swing.*;
import java.awt.*;

public class SignupPanel {
    public interface SignupListener {
        void onSignupComplete();
        void onCancel();
    }

    public static JPanel build(JFrame mainFrame, SignupListener listener) {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UIUtils.NAVY);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        card.setPreferredSize(new Dimension(400, 600));

        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(UIUtils.ACCENT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Join the Smart Campus Online Service System", SwingConstants.CENTER);
        sub.setFont(UIUtils.fontSmall);
        sub.setForeground(UIUtils.TEXT2);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField name = UIUtils.styledField("Full Name");
        JTextField email = UIUtils.styledField("Email Address");
        JPasswordField pass = new JPasswordField();
        pass.setFont(UIUtils.fontNormal);
        pass.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.BORDER),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"student", "staff", "guest"});
        roleBox.setFont(UIUtils.fontNormal);
        roleBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton signup = UIUtils.primaryBtn("Sign Up");
        signup.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        
        JButton cancel = UIUtils.secondaryBtn("Back to Login");
        cancel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JLabel msg = new JLabel(" ", SwingConstants.CENTER);
        msg.setFont(UIUtils.fontSmall);
        msg.setForeground(UIUtils.DANGER);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        signup.addActionListener(e -> {
            String nm = name.getText().trim();
            String em = email.getText().trim();
            String ps = new String(pass.getPassword());
            String rl = (String) roleBox.getSelectedItem();
            
            if (nm.isEmpty() || em.isEmpty() || ps.isEmpty()) {
                msg.setText("All fields are required.");
                return;
            }
            
            // Check if user exists
            for (User u : Database.users) {
                if (u.email.equalsIgnoreCase(em)) {
                    msg.setText("User with this email already exists.");
                    return;
                }
            }
            
            User newUser = new User(Database.nextUserId++, nm, em, ps, rl);
            Database.addUser(newUser);
            JOptionPane.showMessageDialog(mainFrame, "Account created successfully! Please login.", "Success", JOptionPane.INFORMATION_MESSAGE);
            listener.onSignupComplete();
        });
        
        cancel.addActionListener(e -> listener.onCancel());

        card.add(title); card.add(Box.createVerticalStrut(4));
        card.add(sub); card.add(Box.createVerticalStrut(30));
        
        card.add(new JLabel("Full Name")); card.add(Box.createVerticalStrut(6));
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        name.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        card.add(name); card.add(Box.createVerticalStrut(12));
        
        card.add(new JLabel("Email")); card.add(Box.createVerticalStrut(6));
        email.setAlignmentX(Component.LEFT_ALIGNMENT);
        email.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        card.add(email); card.add(Box.createVerticalStrut(12));
        
        card.add(new JLabel("Password")); card.add(Box.createVerticalStrut(6));
        pass.setAlignmentX(Component.LEFT_ALIGNMENT);
        pass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        card.add(pass); card.add(Box.createVerticalStrut(12));
        
        card.add(new JLabel("Role")); card.add(Box.createVerticalStrut(6));
        roleBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(roleBox); card.add(Box.createVerticalStrut(24));
        
        signup.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(signup); card.add(Box.createVerticalStrut(8));
        
        cancel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(cancel); card.add(Box.createVerticalStrut(12));
        
        card.add(msg);

        root.add(card);
        return root;
    }
}