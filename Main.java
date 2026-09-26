package pims;

import java.util.Locale;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Pharmacy Inventory Management System (PIMS) - HealthFirst Pharmacy
 * Entry point. Set this class as the project's Main Class in NetBeans.
 */
public class Main {

    public static void main(String[] args) {
        // Force number formatting (String.format, DecimalFormat, etc.) to always use
        // a period as the decimal separator, regardless of the machine's regional
        // settings. Without this, on locales that use a comma (e.g. many South
        // African / European Windows installs), prices display as "39,95" and
        // Double.parseDouble() later crashes trying to read that string back.
        Locale.setDefault(Locale.Category.FORMAT, Locale.US);

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            UIManager.put("control", UITheme.BG);
            UIManager.put("nimbusBase", UITheme.PRIMARY_DARK);
            UIManager.put("nimbusFocus", UITheme.PRIMARY);
            UIManager.put("Table.alternateRowColor", new java.awt.Color(247, 250, 249));
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            if (DBConnection.testConnection()) {
                new LoginFrame().setVisible(true);
            } else {
                System.exit(1);
            }
        });
    }
}
