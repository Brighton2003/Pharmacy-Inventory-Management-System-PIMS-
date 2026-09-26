package pims;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** CRUD screen for the medicines (inventory) table. */
public class MedicinePanel extends JPanel {

    /** Small holder so a JComboBox can show a supplier name but return its id. */
    static class SupplierItem {
        final int id;
        final String name;
        SupplierItem(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Name", "Company", "Type", "Price (R)", "Stock",
                         "Reorder", "Expiry Date", "Supplier"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    private final JTextField txtId = new JTextField(6);
    private final JTextField txtName = new JTextField(16);
    private final JTextField txtCompany = new JTextField(16);
    private final JComboBox<String> cmbType = new JComboBox<>(
            new String[]{"Tablet", "Capsule", "Syrup", "Injection", "Cream", "Drops", "Ointment"});
    private final JTextField txtPrice = new JTextField(8);
    private final JTextField txtQty = new JTextField(8);
    private final JTextField txtReorder = new JTextField(8);
    private final JTextField txtExpiry = new JTextField(10);
    private final JComboBox<SupplierItem> cmbSupplier = new JComboBox<>();
    private final JTextField txtSearch = new JTextField(20);
    private final JLabel lblCount = UITheme.label("", UITheme.BODY, UITheme.MUTED);

    public MedicinePanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BG);
        UITheme.pad(this, 10, 10, 10, 10);

        add(buildForm(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);

        loadSuppliers();
        loadMedicines("");
    }

    private JPanel buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(UITheme.titled(" Medicine Details "));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 6, 5, 6);
        g.anchor = GridBagConstraints.WEST;

        txtId.setEditable(false);
        txtId.setBackground(new Color(236, 239, 241));

        addField(p, g, 0, 0, "Medicine ID", txtId);
        addField(p, g, 2, 0, "Name *", txtName);
        addField(p, g, 4, 0, "Company", txtCompany);
        addField(p, g, 6, 0, "Type", cmbType);

        addField(p, g, 0, 1, "Price (R) *", txtPrice);
        addField(p, g, 2, 1, "Quantity in Stock *", txtQty);
        addField(p, g, 4, 1, "Reorder Level *", txtReorder);
        addField(p, g, 6, 1, "Expiry (yyyy-MM-dd) *", txtExpiry);

        addField(p, g, 0, 2, "Supplier", cmbSupplier);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setOpaque(false);
        JButton add = UITheme.button("Add", UITheme.PRIMARY);
        JButton upd = UITheme.button("Update", UITheme.INFO);
        JButton del = UITheme.button("Delete", UITheme.DANGER);
        JButton clr = UITheme.button("Clear", UITheme.MUTED);
        add.addActionListener(e -> addMedicine());
        upd.addActionListener(e -> updateMedicine());
        del.addActionListener(e -> deleteMedicine());
        clr.addActionListener(e -> clearForm());
        btns.add(add);
        btns.add(upd);
        btns.add(del);
        btns.add(clr);

        g.gridx = 2;
        g.gridy = 2;
        g.gridwidth = 6;
        p.add(btns, g);
        return p;
    }

    private void addField(JPanel p, GridBagConstraints g, int x, int y, String label, Component field) {
        g.gridwidth = 1;
        g.gridx = x;
        g.gridy = y;
        p.add(UITheme.label(label, UITheme.BOLD, UITheme.TEXT), g);
        g.gridx = x + 1;
        if (field instanceof JTextField) {
            ((JTextField) field).setFont(UITheme.BODY);
        }
        field.setPreferredSize(new Dimension(150, 28));
        p.add(field, g);
    }

    private JPanel buildTable() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBackground(Color.WHITE);
        p.setBorder(UITheme.titled(" Inventory "));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        top.setOpaque(false);
        top.add(UITheme.label("Search:", UITheme.BOLD, UITheme.TEXT));
        txtSearch.setFont(UITheme.BODY);
        txtSearch.setPreferredSize(new Dimension(220, 28));
        top.add(txtSearch);
        JButton find = UITheme.button("Find", UITheme.PRIMARY);
        JButton refresh = UITheme.button("Refresh", UITheme.MUTED);
        find.addActionListener(e -> loadMedicines(txtSearch.getText().trim()));
        refresh.addActionListener(e -> { txtSearch.setText(""); loadMedicines(""); });
        txtSearch.addActionListener(e -> loadMedicines(txtSearch.getText().trim()));
        top.add(find);
        top.add(refresh);
        top.add(UITheme.label("   Red = low stock    Orange = expires within 30 days",
                UITheme.BODY, UITheme.MUTED));
        p.add(top, BorderLayout.NORTH);

        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(Object.class, new RowRenderer());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromRow();
            }
        });
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setOpaque(false);
        bottom.add(lblCount);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    /** Colours rows that are low on stock or close to expiry. */
    private class RowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                boolean foc, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            if (!sel) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 249));
                c.setForeground(UITheme.TEXT);
                try {
                    int stock = Integer.parseInt(model.getValueAt(row, 5).toString());
                    int reorder = Integer.parseInt(model.getValueAt(row, 6).toString());
                    LocalDate exp = LocalDate.parse(model.getValueAt(row, 7).toString());
                    if (stock <= reorder) {
                        c.setBackground(new Color(255, 226, 226));
                    } else if (!exp.isAfter(LocalDate.now().plusDays(30))) {
                        c.setBackground(new Color(255, 240, 214));
                    }
                } catch (Exception ignored) {
                }
            }
            return c;
        }
    }

    private void loadSuppliers() {
        cmbSupplier.removeAllItems();
        cmbSupplier.addItem(new SupplierItem(0, "-- None --"));
        String sql = "SELECT supplier_id, name FROM suppliers ORDER BY name";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                cmbSupplier.addItem(new SupplierItem(rs.getInt(1), rs.getString(2)));
            }
        } catch (Exception e) {
            error("Could not load suppliers: " + e.getMessage());
        }
    }

    public void loadMedicines(String search) {
        model.setRowCount(0);
        String sql = "SELECT m.medicine_id, m.name, m.company, m.medicine_type, m.price, "
                   + "m.quantity_in_stock, m.reorder_level, m.expiry_date, "
                   + "IFNULL(s.name,'-') AS supplier "
                   + "FROM medicines m LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id "
                   + "WHERE m.name LIKE ? OR m.company LIKE ? OR m.medicine_type LIKE ? "
                   + "ORDER BY m.name";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String like = "%" + search + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        String.format("%.2f", rs.getDouble(5)), rs.getInt(6), rs.getInt(7),
                        rs.getDate(8), rs.getString(9)});
                }
            }
            lblCount.setText(model.getRowCount() + " medicine(s) listed.");
        } catch (Exception e) {
            error("Could not load medicines: " + e.getMessage());
        }
    }

    private void fillFormFromRow() {
        int r = table.getSelectedRow();
        if (r < 0) {
            return;
        }
        txtId.setText(model.getValueAt(r, 0).toString());
        txtName.setText(model.getValueAt(r, 1).toString());
        txtCompany.setText(model.getValueAt(r, 2).toString());
        cmbType.setSelectedItem(model.getValueAt(r, 3).toString());
        txtPrice.setText(model.getValueAt(r, 4).toString());
        txtQty.setText(model.getValueAt(r, 5).toString());
        txtReorder.setText(model.getValueAt(r, 6).toString());
        txtExpiry.setText(model.getValueAt(r, 7).toString());
        String sup = model.getValueAt(r, 8).toString();
        for (int i = 0; i < cmbSupplier.getItemCount(); i++) {
            if (cmbSupplier.getItemAt(i).name.equals(sup)) {
                cmbSupplier.setSelectedIndex(i);
                break;
            }
        }
    }

    private void clearForm() {
        txtId.setText("");
        txtName.setText("");
        txtCompany.setText("");
        cmbType.setSelectedIndex(0);
        txtPrice.setText("");
        txtQty.setText("");
        txtReorder.setText("");
        txtExpiry.setText("");
        cmbSupplier.setSelectedIndex(0);
        table.clearSelection();
    }

    /** Validates the form; returns null and shows a message if something is wrong. */
    private Object[] readForm() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            error("Medicine name is required.");
            return null;
        }
        double price;
        int qty, reorder;
        Date expiry;
        try {
            price = Double.parseDouble(txtPrice.getText().trim().replace(",", "."));
            if (price < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            error("Price must be a positive number, e.g. 45.50");
            return null;
        }
        try {
            qty = Integer.parseInt(txtQty.getText().trim());
            reorder = Integer.parseInt(txtReorder.getText().trim());
            if (qty < 0 || reorder < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            error("Quantity and reorder level must be whole numbers (0 or more).");
            return null;
        }
        try {
            expiry = Date.valueOf(LocalDate.parse(txtExpiry.getText().trim()));
        } catch (Exception e) {
            error("Expiry date must be in the format yyyy-MM-dd, e.g. 2027-03-31");
            return null;
        }
        SupplierItem s = (SupplierItem) cmbSupplier.getSelectedItem();
        Integer supId = (s == null || s.id == 0) ? null : s.id;
        return new Object[]{name, txtCompany.getText().trim(),
            cmbType.getSelectedItem().toString(), price, qty, reorder, expiry, supId};
    }

    private void addMedicine() {
        Object[] f = readForm();
        if (f == null) {
            return;
        }
        String sql = "INSERT INTO medicines(name, company, medicine_type, price, "
                   + "quantity_in_stock, reorder_level, expiry_date, supplier_id) "
                   + "VALUES(?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, f);
            ps.executeUpdate();
            info("Medicine added successfully.");
            clearForm();
            loadMedicines("");
        } catch (Exception e) {
            error("Add failed: " + e.getMessage());
        }
    }

    private void updateMedicine() {
        if (txtId.getText().trim().isEmpty()) {
            error("Select a medicine in the table first.");
            return;
        }
        Object[] f = readForm();
        if (f == null) {
            return;
        }
        String sql = "UPDATE medicines SET name=?, company=?, medicine_type=?, price=?, "
                   + "quantity_in_stock=?, reorder_level=?, expiry_date=?, supplier_id=? "
                   + "WHERE medicine_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, f);
            ps.setInt(9, Integer.parseInt(txtId.getText().trim()));
            ps.executeUpdate();
            info("Medicine updated successfully.");
            clearForm();
            loadMedicines("");
        } catch (Exception e) {
            error("Update failed: " + e.getMessage());
        }
    }

    private void bind(PreparedStatement ps, Object[] f) throws Exception {
        ps.setString(1, (String) f[0]);
        ps.setString(2, (String) f[1]);
        ps.setString(3, (String) f[2]);
        ps.setDouble(4, (Double) f[3]);
        ps.setInt(5, (Integer) f[4]);
        ps.setInt(6, (Integer) f[5]);
        ps.setDate(7, (Date) f[6]);
        if (f[7] == null) {
            ps.setNull(8, java.sql.Types.INTEGER);
        } else {
            ps.setInt(8, (Integer) f[7]);
        }
    }

    private void deleteMedicine() {
        if (txtId.getText().trim().isEmpty()) {
            error("Select a medicine in the table first.");
            return;
        }
        int c = JOptionPane.showConfirmDialog(this,
                "Delete \"" + txtName.getText() + "\" from the inventory?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) {
            return;
        }
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM medicines WHERE medicine_id=?")) {
            ps.setInt(1, Integer.parseInt(txtId.getText().trim()));
            ps.executeUpdate();
            info("Medicine deleted.");
            clearForm();
            loadMedicines("");
        } catch (Exception e) {
            error("Delete failed. This medicine may already appear on a sale.\n\n" + e.getMessage());
        }
    }

    /** Used by other screens that need a fresh supplier list. */
    public void refreshAll() {
        loadSuppliers();
        loadMedicines("");
    }

    private void info(String m) {
        JOptionPane.showMessageDialog(this, m, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void error(String m) {
        JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /** Helper kept for completeness - list of all medicine names (used by POS auto-complete). */
    public static List<String> allNames() {
        List<String> names = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT name FROM medicines ORDER BY name")) {
            while (rs.next()) {
                names.add(rs.getString(1));
            }
        } catch (Exception ignored) {
        }
        return names;
    }
}
