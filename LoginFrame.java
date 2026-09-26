package pims;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/** Authentication module: secure login + redirect to the correct dashboard. */
public class LoginFrame extends JFrame {

    private final JTextField txtUser = new JTextField(18);
    private final JPasswordField txtPass = new JPasswordField(18);
    private final JLabel lblMsg = new JLabel(" ", SwingConstants.CENTER);
    private JButton btnLogin;

    public LoginFrame() {
        setTitle("PIMS - HealthFirst Pharmacy | Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(860, 500);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UITheme.BG);
        setLayout(new BorderLayout());

        add(buildBranding(), BorderLayout.WEST);
        add(buildForm(), BorderLayout.CENTER);
        getRootPane().setDefaultButton(btnLogin);
    }

    /** Left-hand green branding panel. */
    private JPanel buildBranding() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, UITheme.PRIMARY, 0, getHeight(), UITheme.PRIMARY_DARK));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 38));
                g2.fillOval(-60, getHeight() - 150, 240, 240);
                g2.fillOval(getWidth() - 90, -70, 180, 180);
                // simple "plus" pharmacy symbol
                g2.setColor(new Color(255, 255, 255, 220));
                int cx = getWidth() / 2, cy = 150, arm = 46, th = 26;
                g2.fillRoundRect(cx - th / 2, cy - arm, th, arm * 2, 8, 8);
                g2.fillRoundRect(cx - arm, cy - th / 2, arm * 2, th, 8, 8);
                g2.dispose();
            }
        };
        p.setPreferredSize(new Dimension(360, 100));
        p.setLayout(new BorderLayout());

        JPanel txt = new JPanel();
        txt.setOpaque(false);
        txt.setLayout(new javax.swing.BoxLayout(txt, javax.swing.BoxLayout.Y_AXIS));
        txt.setBorder(BorderFactory.createEmptyBorder(230, 30, 30, 30));

        JLabel a = new JLabel("HealthFirst Pharmacy");
        a.setFont(new Font("Segoe UI", Font.BOLD, 26));
        a.setForeground(Color.WHITE);
        JLabel b = new JLabel("Inventory Management System");
        b.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        b.setForeground(new Color(215, 236, 233));
        JLabel c = new JLabel("<html><br>Stock &bull; Point of Sale &bull; Reports</html>");
        c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        c.setForeground(new Color(190, 225, 220));

        txt.add(a);
        txt.add(b);
        txt.add(c);
        p.add(txt, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildForm() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(UITheme.BG);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(223, 230, 233)),
                BorderFactory.createEmptyBorder(28, 34, 28, 34)));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;

        card.add(UITheme.label("User Login", UITheme.H1, UITheme.PRIMARY_DARK), g);
        g.gridy++;
        card.add(UITheme.label("Please sign in to continue", UITheme.BODY, UITheme.MUTED), g);

        g.gridwidth = 1;
        g.gridy++;
        g.insets = new Insets(18, 6, 4, 6);
        card.add(UITheme.label("Username", UITheme.BOLD, UITheme.TEXT), g);
        g.gridy++;
        g.insets = new Insets(0, 6, 8, 6);
        txtUser.setFont(UITheme.BODY);
        txtUser.setPreferredSize(new Dimension(260, 34));
        card.add(txtUser, g);

        g.gridy++;
        g.insets = new Insets(6, 6, 4, 6);
        card.add(UITheme.label("Password", UITheme.BOLD, UITheme.TEXT), g);
        g.gridy++;
        g.insets = new Insets(0, 6, 8, 6);
        txtPass.setFont(UITheme.BODY);
        txtPass.setPreferredSize(new Dimension(260, 34));
        card.add(txtPass, g);

        btnLogin = UITheme.button("LOGIN", UITheme.PRIMARY);
        btnLogin.setPreferredSize(new Dimension(260, 40));
        btnLogin.addActionListener(e -> doLogin());
        g.gridy++;
        g.insets = new Insets(14, 6, 6, 6);
        card.add(btnLogin, g);

        JButton btnExit = UITheme.button("Exit", UITheme.MUTED);
        btnExit.setPreferredSize(new Dimension(260, 32));
        btnExit.addActionListener(e -> System.exit(0));
        g.gridy++;
        g.insets = new Insets(0, 6, 6, 6);
        card.add(btnExit, g);

        lblMsg.setFont(UITheme.BODY);
        lblMsg.setForeground(UITheme.DANGER);
        lblMsg.setPreferredSize(new Dimension(260, 22));
        g.gridy++;
        card.add(lblMsg, g);

        JLabel hint = new JLabel("<html><center><font color='#78909C'>Demo &mdash; Admin: admin / admin123<br>"
                + "Cashier: cashier / cash123</font></center></html>");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.gridy++;
        card.add(hint, g);

        wrap.add(card);
        return wrap;
    }

    private void doLogin() {
        String u = txtUser.getText().trim();
        String p = new String(txtPass.getPassword());

        if (u.isEmpty() || p.isEmpty()) {
            lblMsg.setText("Please enter both username and password.");
            return;
        }

        String sql = "SELECT user_id, username, full_name, role, password FROM users WHERE username = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, u);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    lblMsg.setText("Invalid username or password.");
                    txtPass.setText("");
                    return;
                }
                String stored = rs.getString("password");
                // accepts a SHA-256 hash, and also plain text if the DB was seeded manually
                boolean ok = stored.equalsIgnoreCase(Session.hash(p)) || stored.equals(p);
                if (!ok) {
                    lblMsg.setText("Invalid username or password.");
                    txtPass.setText("");
                    return;
                }

                Session.set(rs.getInt("user_id"), rs.getString("username"),
                        rs.getString("full_name"), rs.getString("role"));

                dispose();
                if ("Admin".equalsIgnoreCase(Session.role)) {
                    new AdminDashboard().setVisible(true);
                } else {
                    new CashierDashboard().setVisible(true);
                }
            }
        } catch (Exception ex) {
            lblMsg.setText("Database error: " + ex.getMessage());
        }
    }
}
