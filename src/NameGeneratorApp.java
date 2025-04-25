import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class NameGeneratorApp {
    private java.util.List<String> names = new ArrayList<>();
    private java.util.List<String> nicknames = new ArrayList<>();
    private Set<String> generatedPhrases = new LinkedHashSet<>(); // Для хранения уникальных фраз с сохранением порядка
    private final String DATA_FILE = "data.json";
    private final String PHRASES_FILE = "phrases.json";
    private Random random = new Random();

    private JFrame frame;
    private JLabel resultLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new NameGeneratorApp().initialize();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Ошибка: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void initialize() throws Exception {
        loadData();
        loadPhrases();

        frame = new JFrame("Генератор имен и псевдонимов");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 550);
        frame.setLayout(new BorderLayout());

        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 0, 0));
        resultLabel = new JLabel("Нажмите кнопку для генерации", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 18));
        resultPanel.add(resultLabel, BorderLayout.CENTER);
        frame.add(resultPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton generateButton = createStyledButton("Сгенерировать фразу");
        generateButton.addActionListener(e -> generatePhrase());

        JButton addWordsButton = createStyledButton("Добавить новые слова");
        addWordsButton.addActionListener(e -> {
            try {
                openAddWordsDialog();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Ошибка: " + ex.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton viewWordsButton = createStyledButton("Просмотреть все слова");
        viewWordsButton.addActionListener(e -> {
            try {
                openViewWordsDialog();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Ошибка: " + ex.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton viewPhrasesButton = createStyledButton("Просмотреть все фразы");
        viewPhrasesButton.addActionListener(e -> showAllPhrases());

        JButton clearPhrasesButton = createStyledButton("Очистить список фраз");
        clearPhrasesButton.addActionListener(e -> clearPhrases());

        JButton exitButton = createStyledButton("Выход");
        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(generateButton);
        buttonPanel.add(Box.createVerticalStrut(15));
        buttonPanel.add(addWordsButton);
        buttonPanel.add(Box.createVerticalStrut(15));
        buttonPanel.add(viewWordsButton);
        buttonPanel.add(Box.createVerticalStrut(15));
        buttonPanel.add(viewPhrasesButton);
        buttonPanel.add(Box.createVerticalStrut(15));
        buttonPanel.add(clearPhrasesButton);
        buttonPanel.add(Box.createVerticalStrut(15));
        buttonPanel.add(exitButton);

        frame.add(buttonPanel, BorderLayout.CENTER);

        JLabel madeByLabel = new JLabel("made by webkarlik", SwingConstants.CENTER);
        madeByLabel.setForeground(Color.BLUE.darker());
        madeByLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        madeByLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://github.com/webkarlik"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Не удалось открыть ссылку",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                madeByLabel.setText("<html><u>made by webkarlik</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                madeByLabel.setText("made by webkarlik");
            }
        });

        JPanel footerPanel = new JPanel();
        footerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        footerPanel.add(madeByLabel);
        frame.add(footerPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void loadData() throws Exception {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try {
                String content = new String(Files.readAllBytes(file.toPath()));
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(content);

                if (json.containsKey("names")) {
                    JSONArray namesArray = (JSONArray) json.get("names");
                    this.names = new ArrayList<>();
                    for (Object item : namesArray) {
                        this.names.add(item.toString());
                    }
                }

                if (json.containsKey("nicknames")) {
                    JSONArray nicknamesArray = (JSONArray) json.get("nicknames");
                    this.nicknames = new ArrayList<>();
                    for (Object item : nicknamesArray) {
                        this.nicknames.add(item.toString());
                    }
                }
            } catch (IOException | ParseException e) {
                System.err.println("Ошибка загрузки файла данных: " + e.getMessage());
                initializeDefaultData();
                saveData();
            }
        } else {
            initializeDefaultData();
            saveData();
        }
    }

    private void loadPhrases() throws Exception {
        File file = new File(PHRASES_FILE);
        if (file.exists()) {
            try {
                String content = new String(Files.readAllBytes(file.toPath()));
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(content);

                if (json.containsKey("generatedPhrases")) {
                    JSONArray phrasesArray = (JSONArray) json.get("generatedPhrases");
                    this.generatedPhrases = new LinkedHashSet<>();
                    for (Object item : phrasesArray) {
                        this.generatedPhrases.add(item.toString());
                    }
                }
            } catch (IOException | ParseException e) {
                System.err.println("Ошибка загрузки файла фраз: " + e.getMessage());
                this.generatedPhrases = new LinkedHashSet<>();
                savePhrases();
            }
        } else {
            this.generatedPhrases = new LinkedHashSet<>();
            savePhrases();
        }
    }

    private void initializeDefaultData() {
        this.names = new ArrayList<>(Arrays.asList("Александр", "Мария", "Дмитрий"));
        this.nicknames = new ArrayList<>(Arrays.asList("Грозный", "Великий", "Мудрый"));
    }

    private void saveData() throws Exception {
        JSONObject json = new JSONObject();
    
        JSONArray namesArray = new JSONArray();
        namesArray.addAll(this.names);
        json.put("names", namesArray);
    
        JSONArray nicknamesArray = new JSONArray();
        nicknamesArray.addAll(this.nicknames);
        json.put("nicknames", nicknamesArray);
    
        try (FileWriter writer = new FileWriter(DATA_FILE)) {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            
            // Сохраняем имена в столбик
            sb.append("  \"names\": [\n");
            for (int i = 0; i < namesArray.size(); i++) {
                sb.append("    \"").append(namesArray.get(i)).append("\"");
                if (i < namesArray.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("  ],\n");
            
            // Сохраняем псевдонимы в столбик
            sb.append("  \"nicknames\": [\n");
            for (int i = 0; i < nicknamesArray.size(); i++) {
                sb.append("    \"").append(nicknamesArray.get(i)).append("\"");
                if (i < nicknamesArray.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("  ]\n");
            
            sb.append("}");
            
            writer.write(sb.toString());
        }
    }

    private void savePhrases() throws Exception {
        JSONObject json = new JSONObject();
        JSONArray phrasesArray = new JSONArray();
        phrasesArray.addAll(this.generatedPhrases);
        json.put("generatedPhrases", phrasesArray);
    
        try (FileWriter writer = new FileWriter(PHRASES_FILE)) {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            
            sb.append("  \"generatedPhrases\": [\n");
            int count = 0;
            for (String phrase : this.generatedPhrases) {
                sb.append("    \"").append(phrase).append("\"");
                if (count++ < this.generatedPhrases.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("  ]\n");
            
            sb.append("}");
            
            writer.write(sb.toString());
        }
    }

    private void generatePhrase() {
        if (this.names.isEmpty() || this.nicknames.isEmpty()) {
            resultLabel.setText("Недостаточно слов для генерации");
            return;
        }

        String name = this.names.get(random.nextInt(this.names.size()));
        String nickname = this.nicknames.get(random.nextInt(this.nicknames.size()));
        String phrase = name + " " + nickname;

        resultLabel.setText(phrase);

        if (!this.generatedPhrases.contains(phrase)) {
            this.generatedPhrases.add(phrase);
            try {
                savePhrases();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Ошибка сохранения фраз",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearPhrases() {
        int confirm = JOptionPane.showConfirmDialog(frame,
                "Вы уверены, что хотите очистить список всех фраз?",
                "Подтверждение очистки", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            this.generatedPhrases.clear();
            try {
                savePhrases();
                JOptionPane.showMessageDialog(frame, "Список всех фраз очищен",
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Ошибка сохранения",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAllPhrases() {
        if (this.generatedPhrases.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Нет сохраненных фраз",
                    "Информация", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(frame, "Все сгенерированные фразы", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 400);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String phrase : this.generatedPhrases) {
            listModel.addElement(phrase);
        }

        JList<String> phrasesList = new JList<>(listModel);
        phrasesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(phrasesList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialog.add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Закрыть");
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    // Остальные методы остаются без изменений
    private void openAddWordsDialog() {
        JDialog dialog = new JDialog(frame, "Добавить слова", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(350, 200);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 10));

        JLabel categoryLabel = new JLabel("Категория:");
        JComboBox<String> categoryCombo = new JComboBox<>(new String[] { "Имя", "Псевдоним" });

        JLabel wordLabel = new JLabel("Слово:");
        JTextField wordField = new JTextField();

        inputPanel.add(categoryLabel);
        inputPanel.add(categoryCombo);
        inputPanel.add(wordLabel);
        inputPanel.add(wordField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        JButton addButton = new JButton("Добавить");
        addButton.addActionListener(e -> {
            String word = wordField.getText().trim();
            if (word.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Введите слово!",
                        "Ошибка", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int categoryIndex = categoryCombo.getSelectedIndex();
                java.util.List<String> targetList = (categoryIndex == 0) ? this.names : this.nicknames;

                if (targetList.contains(word)) {
                    JOptionPane.showMessageDialog(dialog, "Такое слово уже существует!",
                            "Ошибка", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                targetList.add(word);
                Collections.sort(targetList);
                saveData();

                wordField.setText("");
                JOptionPane.showMessageDialog(dialog, "Слово успешно добавлено!",
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Ошибка сохранения: " + ex.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        mainPanel.add(inputPanel, BorderLayout.CENTER);
        buttonPanel.add(addButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void openViewWordsDialog() {
        JDialog dialog = new JDialog(frame, "Просмотр слов", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 400);

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel namesPanel = createWordsPanel(this.names);
        tabbedPane.addTab("Имена (" + this.names.size() + ")", namesPanel);

        JPanel nicknamesPanel = createWordsPanel(this.nicknames);
        tabbedPane.addTab("Псевдонимы (" + this.nicknames.size() + ")", nicknamesPanel);

        dialog.add(tabbedPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Закрыть");
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel closePanel = new JPanel();
        closePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        closePanel.add(closeButton);
        dialog.add(closePanel, BorderLayout.SOUTH);

        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private JPanel createWordsPanel(java.util.List<String> words) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String word : words) {
            listModel.addElement(word);
        }

        JList<String> wordsList = new JList<>(listModel);
        wordsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(wordsList);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        JButton deleteButton = new JButton("Удалить выбранное");
        deleteButton.addActionListener(e -> {
            int selectedIndex = wordsList.getSelectedIndex();
            if (selectedIndex != -1) {
                int confirm = JOptionPane.showConfirmDialog(panel,
                        "Вы уверены, что хотите удалить это слово?",
                        "Подтверждение удаления", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    listModel.remove(selectedIndex);
                    words.remove(selectedIndex);
                    try {
                        saveData();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Ошибка сохранения",
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Выберите слово для удаления",
                        "Ошибка", JOptionPane.WARNING_MESSAGE);
            }
        });

        buttonPanel.add(deleteButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(250, 40));
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setFocusPainted(false);
        return button;
    }
}