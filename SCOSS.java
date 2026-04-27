import javax.swing.*;

/**
 * Smart Campus Orientation and Support System (SCOSS)
 * Module: NMAD62110 / NPRG62120
 * Lecturer: Mrs. K.E. Mamabolo
 *
 * This is the modularized version of SCOSS.
 */
public class SCOSS {
    public static void main(String[] args) {
        // Set Look and Feel to System default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Initialize Database with seed data
        Database.seedData();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("SCOSS - Smart Campus Orientation and Support System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 750);
            frame.setLocationRelativeTo(null);

            // Start with Login
            showLogin(frame);

            frame.setVisible(true);
        });
    }

    private static void showLogin(JFrame frame) {
        frame.getContentPane().removeAll();
        frame.add(LoginPanel.build(frame, () -> {
            showDashboard(frame);
        }));
        frame.revalidate();
        frame.repaint();
    }

    private static void showDashboard(JFrame frame) {
        frame.getContentPane().removeAll();
        frame.add(new MainDashboard(frame));
        frame.revalidate();
        frame.repaint();
    }
}
