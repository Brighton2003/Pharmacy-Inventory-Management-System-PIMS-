package pims;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.LocalDate;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Four analytical reports for the owner/manager:
 *   1. Sales Report        - every transaction in a date range
 *   2. Item-Wise Report    - quantity and revenue per medicine
 *   3. Low Stock Report    - stock at or below the reorder level
 *   4. Expiry Report       - medicines expiring within the next month
 */
public class ReportsPanel extends JPanel {

    private final DefaultTableModel model = new DefaultTableModel();
    private final JTable table = new JTable(model);
    private final JLabel lblTitle = UITheme.label("Sales Report", UITheme.H2, UITheme.PRIMARY_DARK);
    private final JLabel lblSummary = UITheme.label(" ", UITheme.BOLD, UITheme.TEXT);

    private final JTextField txtFrom = new JTextField(10);
    private final JTextField txtTo = new JTextField(10);

    public ReportsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BG);
        UITheme.pad(this, 10, 10, 10, 10);

        add(buildControls(), BorderLayout.NORTH);

        JPanel centre = new JPanel(new BorderLayout(6, 6));
        centre.setBackground(Color.WHITE);
        centre.setBorder(UITheme.titled(" Report Output "));

        JPanel head = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        head.setOpaque(false);
        head.add(lblTitle);
        centre.add(head, BorderLayout.NORTH);

        UITheme.styleTable(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        centre.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        foot.setOpaque(false);
        foot.add(lblSummary);
        JButton export = UITheme.button("Export to CSV", UITheme.MUTED);
        export.addActionListener(e -> exportCsv());
        foot.add(export);
        centre.add(foot, BorderLayout.SOUTH);

        add(centre, BorderLayout.CENTER);

        txtFrom.setText(LocalDate.now().minusMonths(1).toString());
        txtTo.setText(LocalDate.now().toString());
        salesReport();
    }

    private JPanel buildControls() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(UITheme.titled(" Choose a Report "));

        p.add(UITheme.label("From (yyyy-MM-dd):", UITheme.BOLD, UITheme.TEXT));
        txtFrom.setFont(UITheme.BODY);
        txtFrom.setPreferredSize(new Dimension(110, 28));
        p.add(txtFrom);
        p.add(UITheme.label("To:", UITheme.BOLD, UITheme.TEXT));
        txtTo.setFont(UITheme.BODY);
        txtTo.setPreferredSize(new Dimension(110, 28));
        p.add(txtTo);

        JButton b1 = UITheme.button("Sales Report", UITheme.PRIMARY);
        JButton b2 = UITheme.button("Item-Wise Report", UITheme.INFO);
        JButton b3 = UITheme.button("Low Stock Report", UITheme.ACCENT);
        JButton b4 = UITheme.button("Expiry Report", UITheme.DANGER);
        b1.addActionListener(e -> salesReport());
        b2.addActionListener(e -> itemWiseReport());
        b3.addActionListener(e -> lowStockReport());
        b4.addActionListener(e -> expiryReport());
        p.add(b1);
        p.add(b2);
        p.add(b3);
        p.add(b4);
        return p;
    }

    /** Runs a query and dumps the whole result set into the table. */
    private void run(String sql, Object[] params, String title, String summaryPrefix, int moneyCol) {
        lblTitle.setText(title);
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();
                String[] headers = new String[cols];
                for (int i = 1; i <= cols; i++) {
                    headers[i - 1] = md.getColumnLabel(i);
                }
                model.setDataVector(new Object[0][0], headers);

                double total = 0;
                int rows = 0;
                while (rs.next()) {
                    Object[] row = new Object[cols];
                    for (int i = 1; i <= cols; i++) {
                        row[i - 1] = rs.getObject(i);
                    }
                    if (moneyCol > 0) {
                        double v = rs.getDouble(moneyCol);
                        total += v;
                        row[moneyCol - 1] = String.format("%.2f", v);
                    }
                    model.addRow(row);
                    rows++;
                }
                UITheme.styleTable(table);
                lblSummary.setText(rows + " record(s)."
                        + (moneyCol > 0 ? "   " + summaryPrefix + " R " + String.format("%.2f", total) : ""));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Report failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void salesReport() {
        String sql = "SELECT s.sale_id AS 'Sale ID', s.sale_date AS 'Date & Time', "
                   + "u.full_name AS 'Processed By', "
                   + "(SELECT COUNT(*) FROM sale_items si WHERE si.sale_id = s.sale_id) AS 'Items', "
                   + "s.total_amount AS 'Total (R)' "
                   + "FROM sales s JOIN users u ON s.user_id = u.user_id "
                   + "WHERE DATE(s.sale_date) BETWEEN ? AND ? "
                   + "ORDER BY s.sale_date DESC";
        run(sql, new Object[]{txtFrom.getText().trim(), txtTo.getText().trim()},
                "Sales Report  (" + txtFrom.getText().trim() + "  to  " + txtTo.getText().trim() + ")",
                "Grand Total:", 5);
    }

    private void itemWiseReport() {
        String sql = "SELECT m.name AS 'Medicine', m.medicine_type AS 'Type', "
                   + "SUM(si.quantity_sold) AS 'Units Sold', "
                   + "SUM(si.quantity_sold * si.price_at_sale) AS 'Revenue (R)' "
                   + "FROM sale_items si "
                   + "JOIN medicines m ON si.medicine_id = m.medicine_id "
                   + "JOIN sales s ON si.sale_id = s.sale_id "
                   + "WHERE DATE(s.sale_date) BETWEEN ? AND ? "
                   + "GROUP BY m.medicine_id, m.name, m.medicine_type "
                   + "ORDER BY SUM(si.quantity_sold * si.price_at_sale) DESC";
        run(sql, new Object[]{txtFrom.getText().trim(), txtTo.getText().trim()},
                "Item-Wise Sales Report  (" + txtFrom.getText().trim() + "  to  " + txtTo.getText().trim() + ")",
                "Total Revenue:", 4);
    }

    private void lowStockReport() {
        String sql = "SELECT m.medicine_id AS 'ID', m.name AS 'Medicine', "
                   + "m.quantity_in_stock AS 'In Stock', m.reorder_level AS 'Reorder Level', "
                   + "IFNULL(s.name,'-') AS 'Supplier', IFNULL(s.phone,'-') AS 'Supplier Phone' "
                   + "FROM medicines m LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id "
                   + "WHERE m.quantity_in_stock <= m.reorder_level "
                   + "ORDER BY m.quantity_in_stock ASC";
        run(sql, null, "Low Stock Report  (stock at or below reorder level)", "", 0);
    }

    private void expiryReport() {
        String sql = "SELECT m.medicine_id AS 'ID', m.name AS 'Medicine', "
                   + "m.expiry_date AS 'Expiry Date', "
                   + "DATEDIFF(m.expiry_date, CURDATE()) AS 'Days Left', "
                   + "m.quantity_in_stock AS 'Stock', IFNULL(s.name,'-') AS 'Supplier' "
                   + "FROM medicines m LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id "
                   + "WHERE m.expiry_date <= DATE_ADD(CURDATE(), INTERVAL 1 MONTH) "
                   + "ORDER BY m.expiry_date ASC";
        run(sql, null, "Expiry Report  (expiring within the next month)", "", 0);
    }

    private void exportCsv() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "There is nothing to export.");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("pims_report.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(fc.getSelectedFile()))) {
            for (int c = 0; c < model.getColumnCount(); c++) {
                pw.print((c > 0 ? "," : "") + model.getColumnName(c));
            }
            pw.println();
            for (int r = 0; r < model.getRowCount(); r++) {
                for (int c = 0; c < model.getColumnCount(); c++) {
                    Object v = model.getValueAt(r, c);
                    pw.print((c > 0 ? "," : "") + "\"" + (v == null ? "" : v.toString()) + "\"");
                }
                pw.println();
            }
            JOptionPane.showMessageDialog(this, "Report saved to:\n" + fc.getSelectedFile().getAbsolutePath());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
