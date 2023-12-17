import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ExpenseManager {
    private static final String EXPENSES_FILE_PATH = "expenses.xlsx";
    private static final String DAILY_EXPENSES_SHEET_NAME = "DailyExpenses";

    private Map<String, Double> expenses = new LinkedHashMap<>();
    private Map<String, Double> percentageExpenses = new LinkedHashMap<>();
    private Set<String> categories = new LinkedHashSet<>(Arrays.asList(
            "Еда", "Транспорт", "Жилье", "Здоровье", "Развлечения",
            "Одежда", "Образование", "Путешествия", "Игры", "Другое"
    ));

    private Workbook workbook;
    private Sheet sheet;
    private Sheet dailyExpensesSheet;

    public ExpenseManager() {
        initializeWorkbook();
        loadExpensesData();
        updatePercentageExpenses();
    }

    private void initializeWorkbook() {
        File file = new File(EXPENSES_FILE_PATH);

        if (file.exists() && file.length() > 0) {
            try (FileInputStream fis = new FileInputStream(file)) {
                workbook = new XSSFWorkbook(fis);
                sheet = workbook.getSheet("Expenses");
                if (sheet == null) {
                    sheet = workbook.createSheet("Expenses");
                    createExpensesHeaderRow(sheet); // Добавляем заголовки столбцов
                } else if (sheet.getRow(0) == null) {
                    createExpensesHeaderRow(sheet); // Добавляем заголовки столбцов, если их нет
                }
                dailyExpensesSheet = workbook.getSheet(DAILY_EXPENSES_SHEET_NAME);
                if (dailyExpensesSheet == null) {
                    dailyExpensesSheet = workbook.createSheet(DAILY_EXPENSES_SHEET_NAME);
                    createDailyExpensesHeaderRow(dailyExpensesSheet); // Добавляем заголовки столбцов
                } else if (dailyExpensesSheet.getRow(0) == null) {
                    createDailyExpensesHeaderRow(dailyExpensesSheet); // Добавляем заголовки столбцов, если их нет
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Expenses");
            createExpensesHeaderRow(sheet); // Создаем заголовки столбцов
            dailyExpensesSheet = workbook.createSheet(DAILY_EXPENSES_SHEET_NAME);
            createDailyExpensesHeaderRow(dailyExpensesSheet); // Создаем заголовки столбцов
            saveWorkbook();
        }
    }

    private void createHeaderRow(Sheet sheet) {
        // Создаем строку с заголовками столбцов
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Дата");
        headerRow.createCell(1).setCellValue("Сумма");
    }

    private void updatePercentageExpenses() {
        // Обновляем процентные значения расходов
        double totalAmount = getTotalAmount();
        for (Map.Entry<String, Double> entry : expenses.entrySet()) {
            String category = entry.getKey();
            double amount = entry.getValue();
            double percentage = (amount / totalAmount) * 100;
            percentageExpenses.put(category, percentage);
        }
    }

    public void addExpense(String category, double amount) {
        LocalDate date = LocalDate.now(); // Получение текущей даты
        expenses.putIfAbsent(category, 0.0);
        expenses.put(category, expenses.get(category) + amount);

        updateDailyExpenses(date, amount); // Обновление дневных расходов
        updatePercentageExpenses();
        updateExpensesExcel(); // Обновление Excel файла с новыми данными
        updateTotalExpensesSheet(); // Обновляем лист с общей суммой расходов
        saveWorkbook(); // Сохраняем изменения в книге
    }

    private void updateDailyExpenses(LocalDate date, double amount) {
        // Форматирование даты
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = date.format(formatter);

        // Поиск существующей записи для даты
        boolean dateExists = false;
        for (Row row : dailyExpensesSheet) {
            if (row.getRowNum() == 0) continue; // Пропуск строки заголовка
            Cell dateCell = row.getCell(0);
            if (dateCell != null && dateCell.getStringCellValue().equals(formattedDate)) {
                double currentAmount = row.getCell(1).getNumericCellValue();
                row.getCell(1).setCellValue(currentAmount + amount); // Обновляем сумму
                dateExists = true;
                break;
            }
        }

        if (!dateExists) {
            int lastRowNum = dailyExpensesSheet.getLastRowNum();
            Row newRow = dailyExpensesSheet.createRow(lastRowNum + 1);
            newRow.createCell(0).setCellValue(formattedDate);
            newRow.createCell(1).setCellValue(amount); // Добавляем новую запись
        }

        autoSizeColumns(dailyExpensesSheet); // Автоматическое изменение ширины столбцов
        saveWorkbook(); // Сохраняем книгу после изменений
    }

    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    public List<String> getCategoriesList() {
        return new ArrayList<>(categories);
    }

    public List<String> getDailyExpenses() {
        List<String> dailyExpenses = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        for (Row row : dailyExpensesSheet) {
            if (row.getRowNum() == 0) continue; // Пропуск строки заголовка
            Cell dateCell = row.getCell(0);
            Cell amountCell = row.getCell(1);

            if (dateCell != null && amountCell != null) {
                LocalDate date = LocalDate.parse(dateCell.getStringCellValue(), formatter);
                double amount = amountCell.getNumericCellValue();
                String formattedDate = date.format(formatter);
                String expenseInfo = formattedDate + ": " + amount;
                dailyExpenses.add(expenseInfo);
            }
        }

        return dailyExpenses;
    }

    public void printStatistics() {
        System.out.println("Статистика расходов:");
        for (Map.Entry<String, Double> entry : expenses.entrySet()) {
            String category = entry.getKey();
            Double amount = entry.getValue();
            Double percentage = percentageExpenses.get(category);
            System.out.printf("%s: Сумма - %.2f, Процент - %.2f%%%n", category, amount, percentage);
        }
    }

    public void printStatisticsInPercentage() {
        System.out.println("Статистика расходов в процентах:");
        for (Map.Entry<String, Double> entry : expenses.entrySet()) {
            String category = entry.getKey();
            Double amount = entry.getValue();
            double percentage = (amount / getTotalAmount()) * 100;
            System.out.printf("%s: %.2f%%%n", category, percentage);
        }
    }

    public void printCategoryStatistics(String category) {
        Double amount = expenses.get(category);
        if (amount != null) {
            Double percentage = percentageExpenses.get(category);
            System.out.printf("%s: Сумма - %.2f, Процент - %.2f%%%n", category, amount, percentage);
        } else {
            System.out.println("Данные по категории '" + category + "' отсутствуют.");
        }
    }

    public double getTotalAmount() {
        return expenses.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    private void updateExpensesExcel() {
        // Получаем лист Expenses или создаем его, если не существует
        sheet = workbook.getSheet("Expenses");
        if (sheet == null) {
            sheet = workbook.createSheet("Expenses");
            createHeaderRow(sheet); // Создаем заголовки столбцов только при создании нового листа
        }

        // Очищаем лист, начиная со второй строки, чтобы сохранить заголовки столбцов
        int lastRowNum = sheet.getLastRowNum();
        for (int i = lastRowNum; i > 0; i--) {
            Row row = sheet.getRow(i);
            if (row != null) {
                sheet.removeRow(row);
            }
        }

        // Заполняем лист данными
        int rowNum = 1;
        for (String category : categories) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(category);
            Double amount = expenses.getOrDefault(category, 0.0);
            row.createCell(1).setCellValue(amount);
            Double percentage = percentageExpenses.getOrDefault(category, 0.0);
            row.createCell(2).setCellValue(String.format("%.2f%%", percentage));
        }

        // Автоматическое изменение ширины столбцов
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);

        // Сохраняем изменения в книге
        saveWorkbook();
    }

    private void createExpensesHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Категория");
        headerRow.createCell(1).setCellValue("Сумма");
        headerRow.createCell(2).setCellValue("Процент");
    }

    private void createDailyExpensesHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Дата");
        headerRow.createCell(1).setCellValue("Сумма");
    }

    private void loadExpensesData() {
        FileInputStream file = null;
        Workbook workbook = null;
        try {
            file = new FileInputStream(EXPENSES_FILE_PATH);
            workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheet("Expenses");

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header
                String category = row.getCell(0).getStringCellValue();
                double amount = row.getCell(1) != null && row.getCell(1).getCellType() == CellType.NUMERIC ? row.getCell(1).getNumericCellValue() : 0.0;
                expenses.merge(category, amount, Double::sum);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (workbook != null) workbook.close();
                if (file != null) file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static final String TOTAL_EXPENSES_SHEET_NAME = "TotalExpenses";

    private void updateTotalExpensesSheet() {
        // Получаем или создаем лист с общей суммой расходов
        Sheet totalSheet = workbook.getSheet(TOTAL_EXPENSES_SHEET_NAME);
        if (totalSheet == null) {
            totalSheet = workbook.createSheet(TOTAL_EXPENSES_SHEET_NAME);
        }

        // Очищаем лист
        int lastRowNum = totalSheet.getLastRowNum();
        for (int i = lastRowNum; i >= 0; i--) {
            Row row = totalSheet.getRow(i);
            if (row != null) {
                totalSheet.removeRow(row);
            }
        }

        // Создаем строку с заголовком, если нужно
        if (totalSheet.getRow(0) == null) {
            Row headerRow = totalSheet.createRow(0);
            headerRow.createCell(0).setCellValue("Всего расходов за все время");
        }

        // Вносим общую сумму расходов
        Row valueRow = totalSheet.createRow(1);
        valueRow.createCell(0).setCellValue(getTotalAmount());

        // Сохраняем изменения в книге
        saveWorkbook();
    }

    public double getTotalAmountByCategory(String category) {
        return expenses.getOrDefault(category, 0.0);
    }

    public double getPercentageByCategory(String category) {
        double totalAmount = getTotalAmount();
        double categoryAmount = getTotalAmountByCategory(category);
        if (totalAmount == 0) {
            return 0.0;
        }
        return (categoryAmount / totalAmount) * 100;
    }

    private void saveWorkbook() {
        // Сохраняем книгу в файл
        try (FileOutputStream fos = new FileOutputStream(EXPENSES_FILE_PATH)) {
            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}