package pims;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/** Administrator module: medicines, suppliers, users and reports. */
public class AdminDashboard extends JFrame {

    private final JTabbedPane tabs = new JTabbedPane();

    public AdminDashboard() {
        setTitle("PIMS - Administrator Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BG);

        JPanel head = UITheme.header("Administrator Dashboard",
                "Signed in as " + Session.fullName + "  (" + Session.role + ")");

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 18));
        right.setOpaque(false);
        JButton logout = UITheme.button("Logout", new Color(255, 255, 255, 60));
        logout.addActionListener(e -> logout());
        right.add(logout);
        head.add(right, BorderLayout.EAST);
        add(head, BorderLayout.NORTH);

        tabs.setFont(UITheme.BOLD);
        tabs.addTab("  Manage Medicines  ", new MedicinePanel());
        tabs.addTab("  Manage Suppliers  ", new SupplierPanel());
        tabs.addTab("  Manage Users  ", new UserPanel());
        tabs.addTab("  Reports  ", new ReportsPanel());
        tabs.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));
        add(tabs, BorderLayout.CENTER);

        JPanel status = new JPanel(new FlowLayout(FlowLayout.LEFT));
        status.setBackground(new Color(236, 239, 241));
        status.add(UITheme.label("HealthFirst Pharmacy  |  PIMS v1.0  |  User: "
                + Session.username, UITheme.BODY, UITheme.MUTED));
        add(status, BorderLayout.SOUTH);
    }

    private void logout() {
        int c = JOptionPane.showConfirmDialog(this, "Log out of PIMS?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            Session.clear();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
