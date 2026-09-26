package pims;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Lets a cashier look up a price and availability without making a sale.
 * This screen is read-only: cashiers cannot add or edit medicines.
 */
public class StockCheckPanel extends JPanel {

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Medicine", "Company", "Type", "Price (R)", "In Stock", "Expiry Date", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextField txtSearch = new JTextField(24);
    private final JLabel lblInfo = UITheme.label("", UITheme.BODY, UITheme.MUTED);

    public StockCheckPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BG);
        UITheme.pad(this, 10, 10, 10, 10);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.setBackground(Color.WHITE);
        top.setBorder(UITheme.titled(" Price & Availability Check (read-only) "));
        top.add(UITheme.label("Medicine name:", UITheme.BOLD, UITheme.TEXT));
        txtSearch.setFont(UITheme.BODY);
        txtSearch.setPreferredSize(new Dimension(250, 30));
        top.add(txtSearch);
        JButton b = UITheme.button("Check", UITheme.PRIMARY);
        b.addActionListener(e -> load(txtSearch.getText().trim()));
        txtSearch.addActionListener(e -> load(txtSearch.getText().trim()));
        top.add(b);
        JButton all = UITheme.button("Show All", UITheme.MUTED);
        all.addActionListener(e -> { txtSearch.setText(""); load(""); });
        top.add(all);
        add(top, BorderLayout.NORTH);

        JPanel centre = new JPanel(new BorderLayout());
        centre.setBackground(Color.WHITE);
        centre.setBorder(UITheme.titled(" Results "));
        UITheme.styleTable(table);
        centre.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.LEFT));
        foot.setOpaque(false);
        foot.add(lblInfo);
        centre.add(foot, BorderLayout.SOUTH);
        add(centre, BorderLayout.CENTER);

        load("");
    }

    private void load(String s) {
        model.setRowCount(0);
        String sql = "SELECT medicine_id, name, company, medicine_type, price, quantity_in_stock, "
                   + "expiry_date, reorder_level FROM medicines "
                   + "WHERE name LIKE ? OR company LIKE ? ORDER BY name";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + s + "%");
            ps.setString(2, "%" + s + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int stock = rs.getInt(6);
                    int reorder = rs.getInt(8);
                    java.sql.Date exp = rs.getDate(7);
                    String status;
                    if (exp.toLocalDate().isBefore(java.time.LocalDate.now())) {
                        status = "EXPIRED - do not sell";
                    } else if (stock == 0) {
                        status = "Out of stock";
                    } else if (stock <= reorder) {
                        status = "Low stock";
                    } else {
                        status = "Available";
                    }
                    model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), String.format("%.2f", rs.getDouble(5)), stock, exp, status});
                }
            }
            lblInfo.setText(model.getRowCount() + " medicine(s) found.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lookup failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
