import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {

    // Константы с исходным размером окна приложения
    private static final int WIDTH = 700;
    private static final int HEIGHT = 500;

    // Массив коэффициентов многочлена
    private Double[] coefficients;

    // Объект диалогового окна для выбора файлов
    // Компонент не создаѐтся изначально, т.к. может и не понадобиться
// пользователю если тот не собирается сохранять данные в файл
    private JFileChooser fileChooser = null;

    // Элементы меню вынесены в поля данных класса, так как ими необходимо
// манипулировать из разных мест
    private JMenuItem saveToTextMenuItem;
    private JMenuItem saveToGraphicsMenuItem;
    private JMenuItem searchValueMenuItem;

    // Поля ввода для считывания значений переменных
    private JTextField textFieldFrom;
    private JTextField textFieldTo;
    private JTextField textFieldStep;

    private Box hBoxResult;

    // Визуализатор ячеек таблицы
    private GornerTableCellRenderer renderer = new GornerTableCellRenderer();

    // Модель данных с результатами вычислений
    private GornerTableModel data;

    public MainFrame(Double[] coefficients) {
        // Обязательный вызов конструктора предка
        super("Табулирование многочлена на отрезке по схеме Горнера");
        // Запомнить во внутреннем поле переданные коэффициенты
        this.coefficients = coefficients;
        // Установить размеры окна
        setSize(WIDTH, HEIGHT);
        Toolkit kit = Toolkit.getDefaultToolkit();
        // Отцентрировать окно приложения на экране
        setLocation((kit.getScreenSize().width - WIDTH) / 2,
                (kit.getScreenSize().height - HEIGHT) / 2);

        // Создать меню
        JMenuBar menuBar = new JMenuBar();
        // Установить меню в качестве главного меню приложения
        setJMenuBar(menuBar);
        // Добавить в меню пункт меню "Файл"
        JMenu fileMenu = new JMenu("Файл");
        // Добавить его в главное меню
        menuBar.add(fileMenu);
        // Создать пункт меню "Таблица"
        JMenu tableMenu = new JMenu("Таблица");
        // Добавить его в главное меню
        menuBar.add(tableMenu);
        JMenu infoMenu = new JMenu("Справка");
        menuBar.add(infoMenu);

        // Создание элемента меню
        JMenuItem aboutItem = new JMenuItem("О программе");
        infoMenu.add(aboutItem);

        // Добавляем действие для элемента меню
        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Показываем текст в диалоговом окне
                JOptionPane.showMessageDialog(MainFrame.this,
                        "Савич Станислав 9 группа",
                        "Информация",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });


        // Создать новое "действие" по сохранению в текстовый файл
        Action saveToTextAction = new AbstractAction("Сохранить в текстовый файл") {
            public void actionPerformed(ActionEvent event) {
                if (fileChooser == null) {
                    // Если экземпляр диалогового окна "Открыть файл" ещѐ не создан,
                    // то создать его
                    fileChooser = new JFileChooser();
                    // и инициализировать текущей директорией
                    fileChooser.setCurrentDirectory(new File("."));
                }
                // Показать диалоговое окно
                if (fileChooser.showSaveDialog(MainFrame.this) ==
                        JFileChooser.APPROVE_OPTION)
                    // Если результат его показа успешный,
// сохранить данные в текстовый файл
                    saveToTextFile(fileChooser.getSelectedFile());
            }
        };
        // Добавить соответствующий пункт подменю в меню "Файл"
        saveToTextMenuItem = fileMenu.add(saveToTextAction);
        // По умолчанию пункт меню является недоступным (данных ещѐ нет)
        saveToTextMenuItem.setEnabled(false);

        // Создать новое "действие" по сохранению в текстовый файл
        Action saveToGraphicsAction = new AbstractAction("Сохранить данные для построения графика") {
            public void actionPerformed(ActionEvent event) {
                if (fileChooser == null) {
                    // Если экземпляр диалогового окна
// "Открыть файл" ещѐ не создан,
                    // то создать его
                    fileChooser = new JFileChooser();
                    // и инициализировать текущей директорией
                    fileChooser.setCurrentDirectory(new File("."));
                }
                // Показать диалоговое окно
                if (fileChooser.showSaveDialog(MainFrame.this) ==
                        JFileChooser.APPROVE_OPTION) ;
                // Если результат его показа успешный,
// сохранить данные в двоичный файл
                saveToGraphicsFile(
                        fileChooser.getSelectedFile());
            }
        };
// Добавить соответствующий пункт подменю в меню "Файл"
        saveToGraphicsMenuItem = fileMenu.add(saveToGraphicsAction);
        // По умолчанию пункт меню является недоступным (данных ещѐ нет)
        saveToGraphicsMenuItem.setEnabled(false);

        // Создать новое действие по поиску значений многочлена
        Action searchValueAction = new AbstractAction("Найти значение многочлена") {
            public void actionPerformed(ActionEvent event) {
                String valueStr = JOptionPane.showInputDialog(MainFrame.this, "Введите значение для поиска", "Поиск значения", JOptionPane.QUESTION_MESSAGE);
                if (valueStr != null) { // Проверка, что пользователь не отменил ввод
                    try {
                        double searchValue = Double.parseDouble(valueStr);
                        boolean found = false;

                        // Поиск значения в таблице
                        for (int i = 0; i < data.getRowCount(); i++) {
                            double xValue = (Double) data.getValueAt(i, 0);
                            double polynomialValue = (Double) data.getValueAt(i, 1);

                            if (xValue == searchValue) {
                                JOptionPane.showMessageDialog(MainFrame.this, "Значение многочлена в точке " + xValue + " равно " + polynomialValue);
                                found = true;
                                break;
                            }
                        }

                        if (!found) {
                            JOptionPane.showMessageDialog(MainFrame.this, "Значение " + searchValue + " не найдено в таблице.", "Результат поиска", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(MainFrame.this, "Ошибка: введено неверное число.", "Ошибка ввода", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        };

        // Добавить действие в меню "Таблица"
        searchValueMenuItem = tableMenu.add(searchValueAction);
        // По умолчанию пункт меню является недоступным (данных ещѐ нет)

        searchValueMenuItem.setEnabled(false);
        // Создать область с полями ввода для границ отрезка и шага
        // Создать подпись для ввода левой границы отрезка
        JLabel labelForFrom = new JLabel("X изменяется на интервале от:");
        // Создать текстовое поле для ввода значения длиной в 10 символов
// со значением по умолчанию 0.0
        textFieldFrom = new JTextField("0.0", 10);
        // Установить максимальный размер равный предпочтительному, чтобы
// предотвратить увеличение размера поля ввода
        textFieldFrom.setMaximumSize(textFieldFrom.getPreferredSize());
        // Создать подпись для ввода левой границы отрезка
        JLabel labelForTo = new JLabel("до:");
        // Создать текстовое поле для ввода значения длиной в 10 символов
// со значением по умолчанию 1.0
        textFieldTo = new JTextField("1.0", 10);
        // Установить максимальный размер равный предпочтительному, чтобы
// предотвратить увеличение размера поля ввода
        textFieldTo.setMaximumSize(textFieldTo.getPreferredSize());
        // Создать подпись для ввода шага табулирования
        JLabel labelForStep = new JLabel("с шагом:");
        // Создать текстовое поле для ввода значения длиной в 10 символов
// со значением по умолчанию 1.0
        textFieldStep = new JTextField("0.1", 10);
        // Установить максимальный размер равный предпочтительному, чтобы
// предотвратить увеличение размера поля ввода
        textFieldStep.setMaximumSize(textFieldStep.getPreferredSize());
        // Создать контейнер 1 типа "коробка с горизонтальной укладкой"
        Box hboxRange = Box.createHorizontalBox();
        // Задать для контейнера тип рамки "объѐмная"
        hboxRange.setBorder(BorderFactory.createBevelBorder(1));
        // Добавить "клей" C1-H1
        hboxRange.add(Box.createHorizontalGlue());
        // Добавить подпись "От"
        hboxRange.add(labelForFrom);
        // Добавить "распорку" C1-H2
        hboxRange.add(Box.createHorizontalStrut(10));
        // Добавить поле ввода "От"
        hboxRange.add(textFieldFrom);
        // Добавить "распорку" C1-H3
        hboxRange.add(Box.createHorizontalStrut(20));
        // Добавить подпись "До"
        hboxRange.add(labelForTo);
        // Добавить "распорку" C1-H4
        hboxRange.add(Box.createHorizontalStrut(10));
        // Добавить поле ввода "До"
        hboxRange.add(textFieldTo);
        // Добавить "распорку" C1-H5
        hboxRange.add(Box.createHorizontalStrut(20));
        // Добавить подпись "с шагом"
        hboxRange.add(labelForStep);
        // Добавить "распорку" C1-H6
        hboxRange.add(Box.createHorizontalStrut(10));
        // Добавить поле для ввода шага табулирования
        hboxRange.add(textFieldStep);
        // Добавить "клей" C1-H7
        hboxRange.add(Box.createHorizontalGlue());
        // Установить предпочтительный размер области равным удвоенному
// минимальному, чтобы при  компоновке область совсем не сдавили
        hboxRange.setPreferredSize(new Dimension(
                (int) hboxRange.getMaximumSize().getWidth(),
                (int) hboxRange.getMinimumSize().getHeight() * 2
        ));

        // Установить область в верхнюю (северную) часть компоновки
        getContentPane().add(hboxRange, BorderLayout.NORTH);

        // Создать кнопку "Вычислить"
        JButton buttonCalc = new JButton("Вычислить");
// Задать действие на нажатие "Вычислить" и привязать к кнопке
        buttonCalc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    // Считать значения начала и конца отрезка, шага
                    Double from = Double.parseDouble(textFieldFrom.getText());
                    Double to = Double.parseDouble(textFieldTo.getText());
                    Double step = Double.parseDouble(textFieldStep.getText());

                    // Проверка: если все поля равны 0
                    if (from == 0 && to == 0 && step == 0) {
                        JOptionPane.showMessageDialog(MainFrame.this,
                                "Ошибка: все поля не могут быть равны 0.",
                                "Неверные значения",
                                JOptionPane.WARNING_MESSAGE);
                        return; // Выход из метода, если ошибка
                    }

                    // Подключение новой модели
                    data = new GornerTableModel(from, to, step, MainFrame.this.coefficients);
                    JTable table = new JTable(data);
                    // Подключение визуализатора ячеек
                    table.setDefaultRenderer(Boolean.class, new GornerTableCellRenderer());

                    // Установка размеров строки таблицы
                    table.setRowHeight(30);

                    // Очистка предыдущих результатов
                    hBoxResult.removeAll();

                    // Добавление таблицы с прокруткой
                    hBoxResult.add(new JScrollPane(table));

                    // Обновление интерфейса
                    getContentPane().validate();

                    // Активировать элементы меню
                    saveToTextMenuItem.setEnabled(true);
                    saveToGraphicsMenuItem.setEnabled(true);
                    searchValueMenuItem.setEnabled(true);
                } catch (NumberFormatException ex) {
                    // Обработка ошибок ввода
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Ошибка в формате записи числа с плавающей точкой",
                            "Ошибочный формат числа",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });


        // Создать кнопку "Очистить поля"
        JButton buttonReset = new JButton("Очистить поля");
// Задать действие на нажатие "Очистить поля" и привязать к кнопке
        buttonReset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                // Установка значений полей в 0
                textFieldFrom.setText("0");
                textFieldTo.setText("0");
                textFieldStep.setText("0");
            }
        });

        // Действие по нажатию на кнопку "Вычислить"
        buttonCalc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    // Считать значения начала и конца отрезка, шага
                    Double from = Double.parseDouble(textFieldFrom.getText());
                    Double to = Double.parseDouble(textFieldTo.getText());
                    Double step = Double.parseDouble(textFieldStep.getText());

                    // Подключение новой модели
                    data = new GornerTableModel(from, to, step, MainFrame.this.coefficients);
                    JTable table = new JTable(data);

                    // Подключение визуализатора ячеек
                    table.setDefaultRenderer(Boolean.class, new GornerTableCellRenderer());

                    // Установка размеров строки таблицы
                    table.setRowHeight(30);

                    // Очистка предыдущих результатов
                    hBoxResult.removeAll();

                    // Добавление таблицы с прокруткой
                    hBoxResult.add(new JScrollPane(table));

                    // Обновление интерфейса
                    getContentPane().validate();

                    // Активировать элементы меню
                    saveToTextMenuItem.setEnabled(true);
                    saveToGraphicsMenuItem.setEnabled(true);
                    searchValueMenuItem.setEnabled(true);
                } catch (NumberFormatException ex) {
                    // Обработка ошибок ввода
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "Ошибка в формате записи числа с плавающей точкой",
                            "Ошибочный формат числа",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // Поместить созданные кнопки в контейнер
        Box hboxButtons = Box.createHorizontalBox();
        hboxButtons.setBorder(BorderFactory.createBevelBorder(1));
        hboxButtons.add(Box.createHorizontalGlue());
        hboxButtons.add(buttonCalc);
        hboxButtons.add(Box.createHorizontalStrut(30));
        hboxButtons.add(buttonReset);
        hboxButtons.add(Box.createHorizontalGlue());
        // Установить предпочтительный размер области равным удвоенному минимальному, чтобы при
        // компоновке окна область совсем не сдавили
        hboxRange.setPreferredSize(new Dimension(
                (int) hboxRange.getMaximumSize().getWidth(),
                (int) hboxRange.getMinimumSize().getHeight() * 2
        ));
        // Разместить контейнер с кнопками в нижней (южной) области граничной компоновки
        getContentPane().add(hboxButtons, BorderLayout.SOUTH);
        // Область для вывода результата пока что пустая
        hBoxResult = Box.createHorizontalBox();
        hBoxResult.add(new JPanel());
        // Установить контейнер hBoxResult в главной (центральной) области граничной компоновки
        getContentPane().add(hBoxResult, BorderLayout.CENTER);
    }

    protected void saveToGraphicsFile(File selectedFile) {
        try {
            // Создать новый байтовый поток вывода, направленный в указанный файл
            DataOutputStream out = new DataOutputStream(new
                    FileOutputStream(selectedFile));
            // Записать в поток вывода попарно значение X в точке, значение многочлена в точке
            for (int i = 0; i < data.getRowCount(); i++) {
                out.writeDouble((Double) data.getValueAt(i, 0));
                out.writeDouble((Double) data.getValueAt(i, 1));
            }
            // Закрыть поток вывода
            out.close();
        } catch (Exception e) {
            // Исключительную ситуацию "ФайлНеНайден" в данном случае можно не обрабатывать,
            // так как мы файл создаѐм, а не открываем для чтения
        }
    }

    protected void saveToTextFile(File selectedFile) {
        try {
            // Создать новый символьный поток вывода, направленный в указанный файл
            PrintStream out = new PrintStream(selectedFile);
            // Записать в поток вывода заголовочные сведения
            out.println("Результаты табулирования многочлена по схеме Горнера");
            out.print("Многочлен: ");
            for (int i = 0; i < coefficients.length; i++) {
                out.print(coefficients[i] + "*X^" +
                        (coefficients.length - i - 1));
                if (i != coefficients.length - 1)
                    out.print(" + ");
            }
            out.println("");
            out.println("Интервал от " + data.getFrom() + " до " +
                    data.getTo() + " с шагом " + data.getStep());

            out.println("====================================================");
            // Записать в поток вывода значения в точках
            for (int i = 0; i < data.getRowCount(); i++) {
                out.println("Значение в точке " + data.getValueAt(i, 0)
                        + " равно " + data.getValueAt(i, 1));
            }
            // Закрыть поток
            out.close();
        } catch (FileNotFoundException e) {
            // Исключительную ситуацию "ФайлНеНайден" можно не
// обрабатывать, так как мы файл создаѐм, а не открываем
        }
    }

    public static void main(String[] args) {
        Double[] coefficients;

        if (args.length == 0) {
            // Если аргументы не заданы, использовать коэффициенты по умолчанию
            System.out.println("Коэффициенты не заданы, используются значения по умолчанию: 1x^2 - 3x + 2");
            coefficients = new Double[]{1.0, -3.0, 2.0}; // Многочлен: 1x^2 - 3x + 2
        } else {
            // Инициализируем массив коэффициентов на основе входных данных
            coefficients = new Double[args.length];
            try {
                for (int i = 0; i < args.length; i++) {
                    coefficients[i] = Double.parseDouble(args[i]);
                }
            } catch (NumberFormatException ex) {
                System.out.println("Ошибка: один из коэффициентов не является числом. Завершение программы.");
                System.exit(-1);
                return;
            }
        }

        // Создаём главное окно
        MainFrame frame = new MainFrame(coefficients);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}