package pims;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;

/** Shared colours, fonts and small helpers so every screen looks consistent. */
public class UITheme {

    public static final Color PRIMARY      = new Color(0, 121, 107);
    public static final Color PRIMARY_DARK = new Color(0, 90, 79);
    public static final Color ACCENT       = new Color(255, 143, 0);
    public static final Color DANGER       = new Color(198, 40, 40);
    public static final Color INFO         = new Color(21, 101, 192);
    public static final Color BG           = new Color(244, 247, 246);
    public static final Color CARD         = Color.WHITE;
    public static final Color TEXT         = new Color(38, 50, 56);
    public static final Color MUTED        = new Color(120, 144, 156);

    public static final Font H1    = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font H2    = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font BODY  = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BOLD  = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font MONO  = new Font("Consolas", Font.PLAIN, 13);

    /** A flat coloured button with rounded corners. */
    public static JButton button(String text, Color bg) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getBackground();
                if (getModel().isPressed()) {
                    c = c.darker();
                } else if (getModel().isRollover()) {
                    c = c.brighter();
                }
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(BOLD);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Gradient header bar used at the top of every window. */
    public static JPanel header(String title, String subtitle) {
        JPanel p = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, PRIMARY, getWidth(), 0, PRIMARY_DARK));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 60));
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(getWidth() - 70, -30, 90, 90);
                g2.dispose();
            }
        };
        p.setPreferredSize(new Dimension(100, 70));
        p.setLayout(new java.awt.BorderLayout());

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new javax.swing.BoxLayout(texts, javax.swing.BoxLayout.Y_AXIS));
        texts.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel t = new JLabel(title);
        t.setFont(H1);
        t.setForeground(Color.WHITE);
        JLabel s = new JLabel(subtitle);
        s.setFont(BODY);
        s.setForeground(new Color(220, 237, 234));

        texts.add(t);
        texts.add(s);
        p.add(texts, java.awt.BorderLayout.WEST);
        return p;
    }

    /** White "card" panel with a soft border. */
    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(223, 230, 233)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        return p;
    }

    public static Border titled(String title) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(223, 230, 233)),
                        title, 0, 0, BOLD, PRIMARY_DARK),
                BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }

    /** Consistent look for every JTable in the app. */
    public static void styleTable(JTable t) {
        t.setFont(BODY);
        t.setRowHeight(26);
        t.setGridColor(new Color(232, 236, 239));
        t.setSelectionBackground(new Color(178, 223, 219));
        t.setSelectionForeground(TEXT);
        t.setShowVerticalLines(false);
        t.getTableHeader().setFont(BOLD);
        t.getTableHeader().setBackground(PRIMARY);
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setPreferredSize(new Dimension(0, 30));
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    public static JLabel label(String text, Font f, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(f);
        l.setForeground(c);
        return l;
    }

    public static void pad(JComponent c, int t, int l, int b, int r) {
        c.setBorder(BorderFactory.createEmptyBorder(t, l, b, r));
    }
}
