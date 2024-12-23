import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.JPanel;
import javax.swing.JLabel;

public class GornerTableCellRenderer implements TableCellRenderer {

    private String needle = null; // Искомое значение

    // Метод для установки искомого значения
    public void setNeedle(String needle) {
        this.needle = needle;
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        JPanel panel = new JPanel();
        JLabel label = new JLabel(value.toString());
        panel.add(label);

        // Подсветка найденного значения
        if (needle != null && needle.equals(value.toString())) {
            panel.setBackground(Color.YELLOW); // Подсветка ячейки
        } else if (col == 2 && value instanceof Boolean && (Boolean) value) {
            panel.setBackground(Color.GREEN); // Подсветка ячейки с true
        } else {
            panel.setBackground(Color.WHITE); // Цвет по умолчанию
        }

        return panel;
    }
}