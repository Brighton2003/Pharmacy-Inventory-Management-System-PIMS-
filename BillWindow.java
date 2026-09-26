package pims;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** Prints / saves the customer bill for a completed sale. */
public class BillWindow extends JDialog {

    private final JTextArea area = new JTextArea();

    public BillWindow(JFrame parent, int saleId, List<CashierDashboard.CartItem> items, double total) {
        super(parent, "Bill / Invoice  #" + saleId, true);
        setSize(520, 640);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        area.setFont(UITheme.MONO);
        area.setEditable(false);
        area.setMargin(new java.awt.Insets(14, 14, 14, 14));
        area.setText(buildBill(saleId, items, total));
        area.setCaretPosition(0);
        add(new JScrollPane(area), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btns.setBackground(Color.WHITE);
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(223, 230, 233)));
        JButton print = UITheme.button("Print", UITheme.PRIMARY);
        JButton save = UITheme.button("Save as .txt", UITheme.INFO);
        JButton close = UITheme.button("Close", UITheme.MUTED);
        print.addActionListener(e -> doPrint());
        save.addActionListener(e -> doSave(saleId));
        close.addActionListener(e -> dispose());
        btns.add(print);
        btns.add(save);
        btns.add(close);
        add(btns, BorderLayout.SOUTH);
    }

    private String buildBill(int saleId, List<CashierDashboard.CartItem> items, double total) {
        StringBuilder b = new StringBuilder();
        String line = "--------------------------------------------\n";
        b.append("       HEALTHFIRST PHARMACY\n");
        b.append("      123 Main Road, Johannesburg\n");
        b.append("        Tel: 011 555 0199\n");
        b.append(line);
        b.append("Invoice No : ").append(saleId).append("\n");
        b.append("Date       : ")
         .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        b.append("Served by  : ").append(Session.fullName).append("\n");
        b.append(line);
        b.append(String.format("%-20s %5s %7s %9s%n", "ITEM", "QTY", "PRICE", "AMOUNT"));
        b.append(line);

        int units = 0;
        for (CashierDashboard.CartItem ci : items) {
            String n = ci.name.length() > 20 ? ci.name.substring(0, 20) : ci.name;
            b.append(String.format("%-20s %5d %7.2f %9.2f%n", n, ci.qty, ci.price, ci.subtotal()));
            units += ci.qty;
        }

        double vat = total - (total / 1.15);   // 15% VAT included
        b.append(line);
        b.append(String.format("%-33s %9d%n", "Total units", units));
        b.append(String.format("%-33s %9.2f%n", "Subtotal (excl. VAT)", total - vat));
        b.append(String.format("%-33s %9.2f%n", "VAT @ 15%", vat));
        b.append(line);
        b.append(String.format("%-33s R%8.2f%n", "TOTAL DUE", total));
        b.append(line);
        b.append("\n     Thank you for your purchase!\n");
        b.append("   Goods are non-refundable once sold.\n");
        b.append("        Get well soon :)\n");
        return b.toString();
    }

    private void doPrint() {
        try {
            boolean done = area.print();
            if (!done) {
                JOptionPane.showMessageDialog(this, "Printing was cancelled.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Printing failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doSave(int saleId) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("bill_" + saleId + ".txt"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(fc.getSelectedFile()))) {
            pw.print(area.getText());
            JOptionPane.showMessageDialog(this, "Bill saved to:\n"
                    + fc.getSelectedFile().getAbsolutePath());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Save failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
