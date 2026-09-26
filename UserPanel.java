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
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** Create, delete and manage Cashier / Admin accounts. */
public class UserPanel extends JPanel {

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Username", "Full Name", "Role"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    private final JTextField txtId = new JTextField(6);
    private final JTextField txtUser = new JTextField(14);
    private final JTextField txtFull = new JTextField(18);
    private final JPasswordField txtPass = new JPasswordField(14);
    private final JComboBox<String> cmbRole = new JComboBox<>(new String[]{"Cashier", "Admin"});

    public UserPanel() {
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
        p.setBorder(UITheme.titled(" User Account "));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 6, 5, 6);
        g.anchor = GridBagConstraints.WEST;

        txtId.setEditable(false);
        txtId.setBackground(new Color(236, 239, 241));

        addField(p, g, 0, 0, "User ID", txtId);
        addField(p, g, 2, 0, "Username *", txtUser);
        addField(p, g, 4, 0, "Full Name *", txtFull);
        addField(p, g, 0, 1, "Password *", txtPass);
        addField(p, g, 2, 1, "Role", cmbRole);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setOpaque(false);
        JButton add = UITheme.button("Create User", UITheme.PRIMARY);
        JButton upd = UITheme.button("Update Details", UITheme.INFO);
        JButton pwd = UITheme.button("Reset Password", UITheme.ACCENT);
        JButton del = UITheme.button("Delete", UITheme.DANGER);
        JButton clr = UITheme.button("Clear", UITheme.MUTED);
        add.addActionListener(e -> create());
        upd.addActionListener(e -> update());
        pwd.addActionListener(e -> resetPassword());
        del.addActionListener(e -> delete());
        clr.addActionListener(e -> clear());
        btns.add(add);
        btns.add(upd);
        btns.add(pwd);
        btns.add(del);
        btns.add(clr);

        g.gridx = 0;
        g.gridy = 2;
        g.gridwidth = 6;
        p.add(btns, g);

        g.gridy = 3;
        p.add(UITheme.label("Passwords are stored as SHA-256 hashes, never as plain text.",
                UITheme.BODY, UITheme.MUTED), g);
        return p;
    }

    private void addField(JPanel p, GridBagConstraints g, int x, int y, String label, Component f) {
        g.gridwidth = 1;
        g.gridx = x;
        g.gridy = y;
        p.add(UITheme.label(label, UITheme.BOLD, UITheme.TEXT), g);
        g.gridx = x + 1;
        f.setPreferredSize(new Dimension(170, 28));
        p.add(f, g);
    }

    private JPanel buildTable() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(UITheme.titled(" System Users "));
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
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT user_id, username, full_name, role FROM users ORDER BY role, username")) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)});
            }
        } catch (Exception e) {
            err("Could not load users: " + e.getMessage());
        }
    }

    private void fill() {
        int r = table.getSelectedRow();
        if (r < 0) {
            return;
        }
        txtId.setText(model.getValueAt(r, 0).toString());
        txtUser.setText(model.getValueAt(r, 1).toString());
        txtFull.setText(model.getValueAt(r, 2).toString());
        cmbRole.setSelectedItem(model.getValueAt(r, 3).toString());
        txtPass.setText("");
    }

    private void clear() {
        txtId.setText("");
        txtUser.setText("");
        txtFull.setText("");
        txtPass.setText("");
        cmbRole.setSelectedIndex(0);
        table.clearSelection();
    }

    private void create() {
        String u = txtUser.getText().trim();
        String f = txtFull.getText().trim();
        String p = new String(txtPass.getPassword());
        if (u.isEmpty() || f.isEmpty() || p.isEmpty()) {
            err("Username, full name and password are all required.");
            return;
        }
        if (p.length() < 5) {
            err("Password must be at least 5 characters long.");
            return;
        }
        String sql = "INSERT INTO users(username, password, role, full_name) VALUES(?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u);
            ps.setString(2, Session.hash(p));
            ps.setString(3, cmbRole.getSelectedItem().toString());
            ps.setString(4, f);
            ps.executeUpdate();
            ok("User account created.");
            clear();
            load();
        } catch (Exception e) {
            err("Could not create user. The username may already exist.\n\n" + e.getMessage());
        }
    }

    private void update() {
        if (txtId.getText().trim().isEmpty()) {
            err("Select a user in the table first.");
            return;
        }
        String sql = "UPDATE users SET username=?, full_name=?, role=? WHERE user_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtUser.getText().trim());
            ps.setString(2, txtFull.getText().trim());
            ps.setString(3, cmbRole.getSelectedItem().toString());
            ps.setInt(4, Integer.parseInt(txtId.getText().trim()));
            ps.executeUpdate();
            ok("User details updated.");
            clear();
            load();
        } catch (Exception e) {
            err("Update failed: " + e.getMessage());
        }
    }

    private void resetPassword() {
        if (txtId.getText().trim().isEmpty()) {
            err("Select a user in the table first.");
            return;
        }
        String p = new String(txtPass.getPassword());
        if (p.length() < 5) {
            err("Type the new password (at least 5 characters) in the Password box first.");
            return;
        }
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE users SET password=? WHERE user_id=?")) {
            ps.setString(1, Session.hash(p));
            ps.setInt(2, Integer.parseInt(txtId.getText().trim()));
            ps.executeUpdate();
            ok("Password reset for " + txtUser.getText().trim() + ".");
            txtPass.setText("");
        } catch (Exception e) {
            err("Reset failed: " + e.getMessage());
        }
    }

    private void delete() {
        if (txtId.getText().trim().isEmpty()) {
            err("Select a user in the table first.");
            return;
        }
        int id = Integer.parseInt(txtId.getText().trim());
        if (id == Session.userId) {
            err("You cannot delete the account you are currently logged in with.");
            return;
        }
        int c = JOptionPane.showConfirmDialog(this, "Delete user \"" + txtUser.getText() + "\"?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) {
            return;
        }
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM users WHERE user_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            ok("User deleted.");
            clear();
            load();
        } catch (Exception e) {
            err("Delete failed. This user may have processed sales already.\n\n" + e.getMessage());
        }
    }

    private void ok(String m) {
        JOptionPane.showMessageDialog(this, m, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void err(String m) {
        JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
