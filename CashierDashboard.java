package pims;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/** Cashier module: Point of Sale + read-only stock check. */
public class CashierDashboard extends JFrame {

    /** One line in the shopping cart. */
    static class CartItem {
        int medicineId;
        String name;
        double price;
        int qty;
        int available;

        CartItem(int id, String name, double price, int qty, int available) {
            this.medicineId = id;
            this.name = name;
            this.price = price;
            this.qty = qty;
            this.available = available;
        }

        double subtotal() {
            return price * qty;
        }
    }

    private final List<CartItem> cart = new ArrayList<>();

    private final DefaultTableModel searchModel = new DefaultTableModel(
            new String[]{"ID", "Medicine", "Type", "Price (R)", "Available", "Expiry"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable searchTable = new JTable(searchModel);

    private final DefaultTableModel cartModel = new DefaultTableModel(
            new String[]{"Medicine", "Unit Price (R)", "Qty", "Subtotal (R)"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable cartTable = new JTable(cartModel);

    private final JTextField txtSearch = new JTextField(22);
    private final JSpinner spQty = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
    private final JLabel lblTotal = new JLabel("R 0.00", SwingConstants.RIGHT);
    private final JLabel lblItems = UITheme.label("0 item(s) in cart", UITheme.BODY, UITheme.MUTED);

    public CashierDashboard() {
        setTitle("PIMS - Cashier / Point of Sale");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BG);

        JPanel head = UITheme.header("Point of Sale",
                "Cashier: " + Session.fullName + "   |   HealthFirst Pharmacy");
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 18));
        right.setOpaque(false);
        JButton logout = UITheme.button("Logout", new Color(255, 255, 255, 60));
        logout.addActionListener(e -> logout());
        right.add(logout);
        head.add(right, BorderLayout.EAST);
        add(head, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.BOLD);
        tabs.addTab("  Point of Sale  ", buildPos());
        tabs.addTab("  Stock Check  ", new StockCheckPanel());
        tabs.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));
        add(tabs, BorderLayout.CENTER);

        searchMedicines("");
    }

    private JPanel buildPos() {
        JPanel left = new JPanel(new BorderLayout(6, 6));
        left.setBackground(Color.WHITE);
        left.setBorder(UITheme.titled(" 1. Find a Medicine "));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        top.setOpaque(false);
        txtSearch.setFont(UITheme.BODY);
        txtSearch.setPreferredSize(new Dimension(230, 30));
        top.add(UITheme.label("Search:", UITheme.BOLD, UITheme.TEXT));
        top.add(txtSearch);
        JButton find = UITheme.button("Search", UITheme.PRIMARY);
        find.addActionListener(e -> searchMedicines(txtSearch.getText().trim()));
        txtSearch.addActionListener(e -> searchMedicines(txtSearch.getText().trim()));
        top.add(find);
        top.add(UITheme.label("   Qty:", UITheme.BOLD, UITheme.TEXT));
        spQty.setPreferredSize(new Dimension(70, 30));
        top.add(spQty);
        JButton addBtn = UITheme.button("Add to Cart", UITheme.ACCENT);
        addBtn.addActionListener(e -> addToCart());
        top.add(addBtn);
        left.add(top, BorderLayout.NORTH);

        UITheme.styleTable(searchTable);
        searchTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        left.add(new JScrollPane(searchTable), BorderLayout.CENTER);

        JPanel rightP = new JPanel(new BorderLayout(6, 6));
        rightP.setBackground(Color.WHITE);
        rightP.setBorder(UITheme.titled(" 2. Cart "));

        UITheme.styleTable(cartTable);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rightP.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(6, 6));
        south.setOpaque(false);

        JPanel totalBox = new JPanel(new BorderLayout());
        totalBox.setBackground(new Color(232, 245, 233));
        totalBox.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        totalBox.add(UITheme.label("TOTAL", UITheme.H2, UITheme.PRIMARY_DARK), BorderLayout.WEST);
        lblTotal.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 26));
        lblTotal.setForeground(UITheme.PRIMARY_DARK);
        totalBox.add(lblTotal, BorderLayout.EAST);
        south.add(totalBox, BorderLayout.NORTH);

        JPanel btns = new JPanel(new GridLayout(1, 3, 8, 8));
        btns.setOpaque(false);
        btns.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        JButton remove = UITheme.button("Remove Item", UITheme.INFO);
        JButton clear = UITheme.button("Clear Cart", UITheme.DANGER);
        JButton checkout = UITheme.button("CHECKOUT", UITheme.PRIMARY);
        remove.addActionListener(e -> removeItem());
        clear.addActionListener(e -> clearCart());
        checkout.addActionListener(e -> checkout());
        btns.add(remove);
        btns.add(clear);
        btns.add(checkout);
        south.add(btns, BorderLayout.CENTER);

        JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT));
        info.setOpaque(false);
        info.add(lblItems);
        south.add(info, BorderLayout.SOUTH);

        rightP.add(south, BorderLayout.SOUTH);

        // Fixed 55/45 split using two BorderLayout cells instead of a JSplitPane,
        // so the cart panel can never be collapsed or hidden by a dragged divider.
        JPanel split = new JPanel(new java.awt.GridBagLayout());
        split.setOpaque(false);
        java.awt.GridBagConstraints gc = new java.awt.GridBagConstraints();
        gc.fill = java.awt.GridBagConstraints.BOTH;
        gc.gridy = 0;
        gc.weighty = 1;

        gc.gridx = 0;
        gc.weightx = 0.55;
        gc.insets = new java.awt.Insets(0, 0, 0, 5);
        split.add(left, gc);

        gc.gridx = 1;
        gc.weightx = 0.45;
        gc.insets = new java.awt.Insets(0, 5, 0, 0);
        split.add(rightP, gc);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UITheme.BG);
        wrap.add(split, BorderLayout.CENTER);
        return wrap;
    }

    private void searchMedicines(String s) {
        searchModel.setRowCount(0);
        String sql = "SELECT medicine_id, name, medicine_type, price, quantity_in_stock, expiry_date "
                   + "FROM medicines WHERE (name LIKE ? OR company LIKE ?) "
                   + "AND quantity_in_stock > 0 AND expiry_date >= CURDATE() "
                   + "ORDER BY name";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + s + "%");
            ps.setString(2, "%" + s + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    searchModel.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3),
                        String.format("%.2f", rs.getDouble(4)), rs.getInt(5), rs.getDate(6)});
                }
            }
        } catch (Exception e) {
            err("Search failed: " + e.getMessage());
        }
    }

    private void addToCart() {
        try {
            int r = searchTable.getSelectedRow();
            if (r < 0) {
                err("Select a medicine from the list on the left first.");
                return;
            }
            int id = Integer.parseInt(searchModel.getValueAt(r, 0).toString());
            String name = searchModel.getValueAt(r, 1).toString();
            double price = parsePrice(searchModel.getValueAt(r, 3).toString());
            int available = Integer.parseInt(searchModel.getValueAt(r, 4).toString());
            int qty = (Integer) spQty.getValue();

            CartItem existing = null;
            for (CartItem ci : cart) {
                if (ci.medicineId == id) {
                    existing = ci;
                    break;
                }
            }
            int wanted = qty + (existing == null ? 0 : existing.qty);
            if (wanted > available) {
                err("Not enough stock.\n\nAvailable: " + available + "\nRequested: " + wanted);
                return;
            }
            if (existing != null) {
                existing.qty = wanted;
            } else {
                cart.add(new CartItem(id, name, price, qty, available));
            }
            spQty.setValue(1);
            refreshCart();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Add to Cart failed:\n\n" + ex,
                    "Debug - Add to Cart Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Parses a price string safely, whether it uses "." or "," as the decimal point. */
    private double parsePrice(String s) {
        return Double.parseDouble(s.trim().replace(",", "."));
    }

    private void removeItem() {
        int r = cartTable.getSelectedRow();
        if (r < 0) {
            err("Select a line in the cart to remove.");
            return;
        }
        cart.remove(r);
        refreshCart();
    }

    private void clearCart() {
        if (cart.isEmpty()) {
            return;
        }
        int c = JOptionPane.showConfirmDialog(this, "Clear all items from the cart?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            cart.clear();
            refreshCart();
        }
    }

    private double total() {
        double t = 0;
        for (CartItem ci : cart) {
            t += ci.subtotal();
        }
        return t;
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        for (CartItem ci : cart) {
            cartModel.addRow(new Object[]{ci.name, String.format("%.2f", ci.price), ci.qty,
                String.format("%.2f", ci.subtotal())});
        }
        lblTotal.setText(String.format("R %.2f", total()));
        lblItems.setText(cart.size() + " item(s) in cart");
    }

    /** Saves the sale, its line items and reduces stock - all inside one transaction. */
    private void checkout() {
        if (cart.isEmpty()) {
            err("The cart is empty.");
            return;
        }
        double grand = total();
        int c = JOptionPane.showConfirmDialog(this,
                String.format("Complete this sale?%n%nItems: %d%nTotal: R %.2f", cart.size(), grand),
                "Confirm Checkout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (c != JOptionPane.YES_OPTION) {
            return;
        }

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // 1. re-check stock inside the transaction
            try (PreparedStatement chk = con.prepareStatement(
                    "SELECT quantity_in_stock FROM medicines WHERE medicine_id=? FOR UPDATE")) {
                for (CartItem ci : cart) {
                    chk.setInt(1, ci.medicineId);
                    try (ResultSet rs = chk.executeQuery()) {
                        if (!rs.next() || rs.getInt(1) < ci.qty) {
                            con.rollback();
                            err("Stock changed while you were busy.\n\"" + ci.name
                                    + "\" no longer has enough units. Please re-check the cart.");
                            return;
                        }
                    }
                }
            }

            // 2. sale header
            int saleId;
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO sales(total_amount, user_id) VALUES(?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setDouble(1, grand);
                ps.setInt(2, Session.userId);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    saleId = keys.getInt(1);
                }
            }

            // 3. line items + stock reduction
            try (PreparedStatement item = con.prepareStatement(
                        "INSERT INTO sale_items(sale_id, medicine_id, quantity_sold, price_at_sale) "
                      + "VALUES(?,?,?,?)");
                 PreparedStatement stock = con.prepareStatement(
                        "UPDATE medicines SET quantity_in_stock = quantity_in_stock - ? "
                      + "WHERE medicine_id = ?")) {

                for (CartItem ci : cart) {
                    item.setInt(1, saleId);
                    item.setInt(2, ci.medicineId);
                    item.setInt(3, ci.qty);
                    item.setDouble(4, ci.price);
                    item.addBatch();

                    stock.setInt(1, ci.qty);
                    stock.setInt(2, ci.medicineId);
                    stock.addBatch();
                }
                item.executeBatch();
                stock.executeBatch();
            }

            con.commit();

            new BillWindow(this, saleId, new ArrayList<>(cart), grand).setVisible(true);
            cart.clear();
            refreshCart();
            searchMedicines(txtSearch.getText().trim());

        } catch (Exception e) {
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (Exception ignored) {
            }
            err("Checkout failed, the sale was cancelled.\n\n" + e.getMessage());
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (Exception ignored) {
            }
        }
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

    private void err(String m) {
        JOptionPane.showMessageDialog(this, m, "Notice", JOptionPane.WARNING_MESSAGE);
    }
}
