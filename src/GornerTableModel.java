import javax.swing.table.AbstractTableModel;
import java.text.DecimalFormat;
import java.math.BigDecimal;
import java.math.RoundingMode;

@SuppressWarnings("serial")
public class GornerTableModel extends AbstractTableModel {

    private Double[] coefficients; // Коэффициенты многочлена
    private Double from; // Начало отрезка
    private Double to;   // Конец отрезка
    private Double step; // Шаг табулирования

    public GornerTableModel(Double from, Double to, Double step, Double[] coefficients) {
        this.from = from;
        this.to = to;
        this.step = step;
        this.coefficients = coefficients;
    }

    public Double getFrom() {
        return from;
    }

    public Double getTo() {
        return to;
    }

    public Double getStep() {
        return step;
    }

    @Override
    public int getColumnCount() {
        // Теперь у нас три столбца
        return 3;
    }

    // Вычислить количество точек между началом и концом отрезка, исходя из шага
    @Override
    public int getRowCount() {
        return (int) Math.ceil((to - from) / step) + 1;
    }

    @Override
    public Object getValueAt(int row, int col) {
        double x = from + step * row;
        if (col == 0) {
            // Значение X
            return x;
        } else if (col == 1) {
            // Значение многочлена в точке
            double polynomialValue = calculatePolynomial(x);

            BigDecimal bd = new BigDecimal(polynomialValue);
            bd = bd.setScale(2, RoundingMode.HALF_UP);

            return bd.doubleValue();
        } else if (col == 2) {
            double polynomialValue = calculatePolynomial(x);


            BigDecimal bd = new BigDecimal(polynomialValue);
            bd = bd.setScale(2, RoundingMode.HALF_UP);

            //дробная часть
            BigDecimal fractionalPart = bd.remainder(BigDecimal.ONE);
            int fractionalAsInt = fractionalPart.movePointRight(2).intValue(); // Масштабируем на 100


            return fractionalAsInt % 2 != 0;
        }
        return null;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case 0:
                return "Значение X"; // Первый столбец
            case 1:
                return "Значение многочлена"; // Второй столбец
            case 2:
                return "Дробная часть нечётная"; // Третий столбец
            default:
                return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case 0:
            case 1:
                return Double.class; // Первые два столбца содержат Double
            case 2:
                return Boolean.class; // Третий столбец содержит Boolean
            default:
                return Object.class;
        }
    }

    // Вычисление значения многочлена по схеме Горнера
    private double calculatePolynomial(double x) {
        double result = 0.0;
        for (double coefficient : coefficients) {
            result = result * x + coefficient;
        }
        return result;
    }
}