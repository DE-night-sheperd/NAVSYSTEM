import javax.swing.*;
import java.awt.*;

public class LoginPanel {
    public interface LoginListener {
        void onLoginSuccess();
        void onSignupClicked();
    }

    public static JPanel build(JFrame mainFrame, LoginListener listener) {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UIUtils.NAVY);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        card.setPreferredSize(new Dimension(400, 500));

        JLabel logo = new JLabel("SCOSS", SwingConstants.CENTER);
        logo.setFont(new Font("SansSerif", Font.BOLD, 32));
        logo.setForeground(UIUtils.ACCENT);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Smart Campus Online Service System", SwingConstants.CENTER);
        sub.setFont(UIUtils.fontSmall);
        sub.setForeground(UIUtils.TEXT2);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField email = UIUtils.styledField("Email Address");
        JPasswordField pass = new JPasswordField();
        pass.setFont(UIUtils.fontNormal);
        pass.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtils.BORDER),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JButton login = UIUtils.primaryBtn("Sign In");
        login.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JButton signup = UIUtils.secondaryBtn("Create Account");
        signup.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        signup.addActionListener(e -> listener.onSignupClicked());

        JLabel msg = new JLabel(" ", SwingConstants.CENTER);
        msg.setFont(UIUtils.fontSmall);
        msg.setForeground(UIUtils.DANGER);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        login.addActionListener(e -> {
            String em = email.getText();
            String ps = new String(pass.getPassword());
            User u = Database.authenticate(em, ps);
            if (u != null) {
                Database.currentUser = u;
                listener.onLoginSuccess();
            } else {
                msg.setText("Invalid email or password.");
            }
        });

        card.add(logo); card.add(Box.createVerticalStrut(4));
        card.add(sub); card.add(Box.createVerticalStrut(40));
        card.add(new JLabel("Email")); card.add(Box.createVerticalStrut(6));
        email.setAlignmentX(Component.LEFT_ALIGNMENT);
        email.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        card.add(email); card.add(Box.createVerticalStrut(16));
        card.add(new JLabel("Password")); card.add(Box.createVerticalStrut(6));
        pass.setAlignmentX(Component.LEFT_ALIGNMENT);
        pass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        card.add(pass); card.add(Box.createVerticalStrut(24));
        login.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(login); card.add(Box.createVerticalStrut(12));
        signup.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(signup); card.add(Box.createVerticalStrut(12));
        card.add(msg);

        root.add(card);
        return root;
    }
}
