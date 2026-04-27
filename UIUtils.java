import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class UIUtils {
    // Colors
    public static final Color NAVY       = new Color(13,  27,  62);
    public static final Color NAVY_LIGHT = new Color(26,  47,  94);
    public static final Color ACCENT     = new Color(37,  99, 235);
    public static final Color ACCENT2    = new Color(59, 130, 246);
    public static final Color SUCCESS    = new Color(16, 185, 129);
    public static final Color WARNING    = new Color(245,158, 11);
    public static final Color DANGER     = new Color(239, 68, 68);
    public static final Color BG         = new Color(248,250,252);
    public static final Color CARD       = Color.WHITE;
    public static final Color BORDER     = new Color(226,232,240);
    public static final Color TEXT1      = new Color(15, 23, 42);
    public static final Color TEXT2      = new Color(100,116,139);

    // Fonts
    public static Font fontTitle  = new Font("SansSerif", Font.BOLD,  22);
    public static Font fontSub    = new Font("SansSerif", Font.PLAIN, 13);
    public static Font fontBold   = new Font("SansSerif", Font.BOLD,  14);
    public static Font fontNormal = new Font("SansSerif", Font.PLAIN, 13);
    public static Font fontSmall  = new Font("SansSerif", Font.PLAIN, 11);

    public static Color statusColor(String s) {
        switch (s) {
            case "Submitted":  return DANGER;
            case "In Progress":return WARNING;
            case "Resolved":   return SUCCESS;
            case "Closed":     return TEXT2;
            default:           return ACCENT;
        }
    }

    public static JLabel badge(String text, Color bg) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(fontSmall);
        l.setForeground(bg.darker().darker());
        l.setBackground(lighter(bg));
        l.setOpaque(true);
        l.setBorder(BorderFactory.createEmptyBorder(3,10,3,10));
        return l;
    }

    public static Color lighter(Color c) {
        int r = Math.min(255, c.getRed()   + (255 - c.getRed())   * 7 / 10);
        int g = Math.min(255, c.getGreen() + (255 - c.getGreen()) * 7 / 10);
        int b = Math.min(255, c.getBlue()  + (255 - c.getBlue())  * 7 / 10);
        return new Color(r, g, b);
    }

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(12,14,12,14)
        ));
        return p;
    }

    public static JButton primaryBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(fontBold);
        b.setForeground(Color.WHITE);
        b.setBackground(ACCENT);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8,18,8,18));
        return b;
    }

    public static JButton secondaryBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(fontNormal);
        b.setForeground(ACCENT);
        b.setBackground(lighter(ACCENT));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(7,14,7,14));
        return b;
    }

    public static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(TEXT2);
        l.setBorder(BorderFactory.createEmptyBorder(14,0,4,0));
        return l;
    }

    public static JTextField styledField(String placeholder) {
        JTextField f = new JTextField(placeholder);
        f.setFont(fontNormal);
        f.setForeground(TEXT2);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(6,10,6,10)
        ));
        f.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) { f.setText(""); f.setForeground(TEXT1); }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) { f.setText(placeholder); f.setForeground(TEXT2); }
            }
        });
        return f;
    }

    public static JPanel pageHeader(String title, String subtitle) {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(NAVY);
        h.setBorder(BorderFactory.createEmptyBorder(20,28,20,28));
        JLabel t = new JLabel(title);
        t.setFont(fontTitle); t.setForeground(Color.WHITE);
        JLabel s = new JLabel(subtitle);
        s.setFont(fontSmall); s.setForeground(new Color(148,163,184));
        h.add(t, BorderLayout.NORTH);
        h.add(s, BorderLayout.SOUTH);
        return h;
    }

    public static JTable styledTable(Object[][] data, String[] cols) {
        DefaultTableModel m = new DefaultTableModel(data, cols) {
            @Override
            public boolean isCellEditable(int r,int c){return false;}
        };
        return styledTable(m);
    }

    public static JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(fontNormal);
        t.setRowHeight(32);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0,0));
        t.setBackground(CARD);
        t.setSelectionBackground(lighter(ACCENT));
        t.setSelectionForeground(ACCENT.darker());
        t.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,12));
        t.getTableHeader().setBackground(new Color(241,245,249));
        t.getTableHeader().setForeground(TEXT2);
        t.getTableHeader().setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER));
        t.setFillsViewportHeight(true);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) c.setBackground(row%2==0 ? CARD : new Color(248,250,252));
                c.setForeground(isSelected ? ACCENT.darker() : TEXT1);
                ((JLabel)c).setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
                return c;
            }
        });
        return t;
    }

    public static void styleStatusColumn(JTable t, int col) {
        t.getColumnModel().getColumn(col).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String s = value != null ? value.toString() : "";
                Color c = statusColor(s);
                l.setForeground(isSelected ? ACCENT.darker() : c.darker());
                l.setBackground(isSelected ? lighter(ACCENT) : lighter(c));
                l.setOpaque(true);
                l.setFont(new Font("SansSerif",Font.BOLD,11));
                l.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
                return l;
            }
        });
    }

    public static JPanel buildFormPanel(String[] labels, JComponent[] fields) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        for (int i = 0; i < labels.length; i++) {
            JLabel l = new JLabel(labels[i]); l.setAlignmentX(Component.LEFT_ALIGNMENT);
            fields[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            fields[i].setMaximumSize(new Dimension(340, 36));
            p.add(l); p.add(Box.createVerticalStrut(4));
            p.add(fields[i]); p.add(Box.createVerticalStrut(8));
        }
        return p;
    }

    public static String today() {
        java.time.LocalDate d = java.time.LocalDate.now();
        return d.toString();
    }
}
