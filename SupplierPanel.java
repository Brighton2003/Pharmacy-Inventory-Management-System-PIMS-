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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** CRUD screen for suppliers. */
public class SupplierPanel extends JPanel {

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Name", "Contact Person", "Phone", "Email", "Address"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    private final JTextField txtId = new JTextField(6);
    private final JTextField txtName = new JTextField(18);
    private final JTextField txtContact = new JTextField(18);
    private final JTextField txtPhone = new JTextField(14);
    private final JTextField txtEmail = new JTextField(18);
    private final JTextArea txtAddress = new JTextArea(3, 20);

    public SupplierPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BG);
        UITheme.pad(this, 10, 10, 10, 10);
        add(buildForm(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        load();
    }

    private JPanel buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(UITheme.titled(" Supplier Details "));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 6, 5, 6);
        g.anchor = GridBagConstraints.WEST;

        txtId.setEditable(false);
        txtId.setBackground(new Color(236, 239, 241));

        addField(p, g, 0, 0, "Supplier ID", txtId);
        addField(p, g, 2, 0, "Name *", txtName);
        addField(p, g, 4, 0, "Contact Person", txtContact);
        addField(p, g, 0, 1, "Phone", txtPhone);
        addField(p, g, 2, 1, "Email", txtEmail);

        g.gridx = 4;
        g.gridy = 1;
        p.add(UITheme.label("Address", UITheme.BOLD, UITheme.TEXT), g);
        g.gridx = 5;
        txtAddress.setFont(UITheme.BODY);
        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(txtAddress);
        sp.setPreferredSize(new Dimension(220, 58));
        p.add(sp, g);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setOpaque(false);
        JButton add = UITheme.button("Add", UITheme.PRIMARY);
        JButton upd = UITheme.button("Update", UITheme.INFO);
        JButton del = UITheme.button("Delete", UITheme.DANGER);
        JButton clr = UITheme.button("Clear", UITheme.MUTED);
        add.addActionListener(e -> add());
        upd.addActionListener(e -> update());
        del.addActionListener(e -> delete());
        clr.addActionListener(e -> clear());
        btns.add(add);
        btns.add(upd);
        btns.add(del);
        btns.add(clr);

        g.gridx = 0;
        g.gridy = 2;
        g.gridwidth = 6;
        p.add(btns, g);
        return p;
    }

    private void addField(JPanel p, GridBagConstraints g, int x, int y, String label, Component f) {
        g.gridwidth = 1;
        g.gridx = x;
        g.gridy = y;
        p.add(UITheme.label(label, UITheme.BOLD, UITheme.TEXT), g);
        g.gridx = x + 1;
        if (f instanceof JTextField) {
            ((JTextField) f).setFont(UITheme.BODY);
        }
        f.setPreferredSize(new Dimension(170, 28));
        p.add(f, g);
    }

    private JPanel buildTable() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(UITheme.titled(" Suppliers "));
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fill();
            }
        });
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void load() {
        model.setRowCount(0);
        String sql = "SELECT supplier_id, name, contact_person, phone, email, address "
                   + "FROM suppliers ORDER BY name";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3),
                    rs.getString(4), rs.getString(5), rs.getString(6)});
            }
        } catch (Exception e) {
            err("Could not load suppliers: " + e.getMessage());
        }
    }

    private void fill() {
        int r = table.getSelectedRow();
        if (r < 0) {
            return;
        }
        txtId.setText(str(model.getValueAt(r, 0)));
        txtName.setText(str(model.getValueAt(r, 1)));
        txtContact.setText(str(model.getValueAt(r, 2)));
        txtPhone.setText(str(model.getValueAt(r, 3)));
        txtEmail.setText(str(model.getValueAt(r, 4)));
        txtAddress.setText(str(model.getValueAt(r, 5)));
    }

    private String str(Object o) {
        return o == null ? "" : o.toString();
    }

    private void clear() {
        txtId.setText("");
        txtName.setText("");
        txtContact.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtAddress.setText("");
        table.clearSelection();
    }

    private void add() {
        if (txtName.getText().trim().isEmpty()) {
            err("Supplier name is required.");
            return;
        }
        String sql = "INSERT INTO suppliers(name, contact_person, phone, email, address) VALUES(?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps);
            ps.executeUpdate();
            ok("Supplier added.");
            clear();
            load();
        } catch (Exception e) {
            err("Add failed: " + e.getMessage());
        }
    }

    private void update() {
        if (txtId.getText().trim().isEmpty()) {
            err("Select a supplier in the table first.");
            return;
        }
        String sql = "UPDATE suppliers SET name=?, contact_person=?, phone=?, email=?, address=? "
                   + "WHERE supplier_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps);
            ps.setInt(6, Integer.parseInt(txtId.getText().trim()));
            ps.executeUpdate();
            ok("Supplier updated.");
            clear();
            load();
        } catch (Exception e) {
            err("Update failed: " + e.getMessage());
        }
    }

    private void bind(PreparedStatement ps) throws Exception {
        ps.setString(1, txtName.getText().trim());
        ps.setString(2, txtContact.getText().trim());
        ps.setString(3, txtPhone.getText().trim());
        ps.setString(4, txtEmail.getText().trim());
        ps.setString(5, txtAddress.getText().trim());
    }

    private void delete() {
        if (txtId.getText().trim().isEmpty()) {
            err("Select a supplier in the table first.");
            return;
        }
        int c = JOptionPane.showConfirmDialog(this, "Delete supplier \"" + txtName.getText() + "\"?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) {
            return;
        }
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM suppliers WHERE supplier_id=?")) {
            ps.setInt(1, Integer.parseInt(txtId.getText().trim()));
            ps.executeUpdate();
            ok("Supplier deleted.");
            clear();
            load();
        } catch (Exception e) {
            err("Delete failed. Medicines may still be linked to this supplier.\n\n" + e.getMessage());
        }
    }

    private void ok(String m) {
        JOptionPane.showMessageDialog(this, m, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void err(String m) {
        JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
