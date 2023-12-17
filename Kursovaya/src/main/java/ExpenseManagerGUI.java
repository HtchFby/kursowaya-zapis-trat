import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ExpenseManagerGUI {
    private JFrame frame;
    private JComboBox<String> categoryComboBox;
    private JTextField amountField;
    private ExpenseManager expenseManager;
    private JTextArea resultTextArea;

    public ExpenseManagerGUI() {
        expenseManager = new ExpenseManager();
        initializeUI();
    }

    private boolean isValidDouble(String text) {
        try {
            double amount = Double.parseDouble(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void initializeUI() {
        frame = new JFrame("Expense Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600); // Увеличил размер окна

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel categoryLabel = new JLabel("Категория:");
        List<String> categories = expenseManager.getCategoriesList();
        categoryComboBox = new JComboBox<>(categories.toArray(new String[0]));
        categoryComboBox.setPreferredSize(new Dimension(200, 30)); // Увеличил размер комбо-бокса

        JLabel amountLabel = new JLabel("Сумма:");
        amountField = new JTextField();
        amountField.setPreferredSize(new Dimension(200, 30));

        JButton addButton = new JButton("Добавить расход");
        addButton.setPreferredSize(new Dimension(200, 40)); // Увеличил размер кнопки
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedCategory = (String) categoryComboBox.getSelectedItem();
                String amountText = amountField.getText();

                if (isValidDouble(amountText)) {
                    double amount = Double.parseDouble(amountText);
                    expenseManager.addExpense(selectedCategory, amount);
                    amountField.setText("");
                    JOptionPane.showMessageDialog(frame, "Расход успешно добавлен.");
                } else {
                    JOptionPane.showMessageDialog(frame, "Введите корректное значение для суммы.");
                }
            }
        });

        JButton viewStatisticsButton = new JButton("Просмотреть статистику");
        viewStatisticsButton.setPreferredSize(new Dimension(200, 40));
        viewStatisticsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStatistics();
            }
        });

        JButton viewPercentageStatisticsButton = new JButton("Просмотреть статистику в процентах");
        viewPercentageStatisticsButton.setPreferredSize(new Dimension(200, 40));
        viewPercentageStatisticsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPercentageStatistics();
            }
        });

        JButton viewCategoryStatisticsButton = new JButton("Просмотреть статистику по категории");
        viewCategoryStatisticsButton.setPreferredSize(new Dimension(200, 40));
        viewCategoryStatisticsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedCategory = (String) categoryComboBox.getSelectedItem();
                showCategoryStatistics(selectedCategory);
            }
        });

        JButton viewTotalExpensesButton = new JButton("Всего расходов за все время");
        viewTotalExpensesButton.setPreferredSize(new Dimension(200, 40));
        viewTotalExpensesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTotalExpenses();
            }
        });

        JButton viewDailyExpensesButton = new JButton("Просмотр расходов по дням");
        viewDailyExpensesButton.setPreferredSize(new Dimension(200, 40));
        viewDailyExpensesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDailyExpenses();
            }
        });

        resultTextArea = new JTextArea(20, 50);
        resultTextArea.setEditable(false);
        resultTextArea.setFont(new Font("Arial", Font.PLAIN, 14)); // Изменил шрифт и размер текстового поля

// Устанавливаем предпочтительный размер для JScrollPane
        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));


        // Добавляем компоненты на панель с учетом сетки
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(categoryLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(categoryComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(amountLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(amountField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(addButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(viewStatisticsButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(viewPercentageStatisticsButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(viewCategoryStatisticsButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        panel.add(viewTotalExpensesButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        panel.add(viewDailyExpensesButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;
        gbc.weighty = 0.5;
        panel.add(scrollPane, gbc);

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    private void showTotalExpenses() {
        double totalExpenses = expenseManager.getTotalAmount();
        JOptionPane.showMessageDialog(frame, "Всего расходов за все время: " + totalExpenses);
    }

    private void showDailyExpenses() {
        JFrame dailyExpensesFrame = new JFrame("Расходы по дням");
        dailyExpensesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dailyExpensesFrame.setSize(600, 400);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextArea dailyExpensesTextArea = new JTextArea();
        dailyExpensesTextArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(dailyExpensesTextArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));


        List<String> dailyExpenses = expenseManager.getDailyExpenses();
        for (String dailyExpense : dailyExpenses) {
            dailyExpensesTextArea.append(dailyExpense + "\n");
        }

        panel.add(scrollPane, gbc);

        dailyExpensesFrame.getContentPane().add(panel);
        dailyExpensesFrame.setVisible(true);
    }

    private void showCategoryStatistics(String category) {
        String statistics = "Статистика по категории '" + category + "':\n";
        double totalAmount = expenseManager.getTotalAmountByCategory(category);
        double percentage = expenseManager.getPercentageByCategory(category);

        statistics += "Общая сумма: " + totalAmount + "\n";
        statistics += "Процент от общей суммы: " + percentage + "%";

        resultTextArea.setText(statistics);
    }

    private void showStatistics() {
        resultTextArea.setText("Статистика расходов:\n");
        for (String category : expenseManager.getCategoriesList()) {
            double totalAmount = expenseManager.getTotalAmountByCategory(category);
            double percentage = expenseManager.getPercentageByCategory(category);
            resultTextArea.append(String.format("%s: Сумма - %.2f, Процент - %.2f%%\n", category, totalAmount, percentage));
        }
    }

    private void showPercentageStatistics() {
        resultTextArea.setText("Статистика расходов в процентах:\n");
        for (String category : expenseManager.getCategoriesList()) {
            double percentage = expenseManager.getPercentageByCategory(category);
            resultTextArea.append(String.format("%s: Процент - %.2f%%\n", category, percentage));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ExpenseManagerGUI();
            }
        });
    }
}