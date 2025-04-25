# Документация проекта

## Общее описание 
`NameGeneratorApp` - это Java-приложение с графическим интерфейсом (Swing), которое генерирует случайные комбинации имен и псевдонимов. Приложение позволяет:
- Генерировать случайные фразы (имя + псевдоним)
- Добавлять новые имена и псевдонимы
- Просматривать и управлять списками слов
- Сохранять историю последних сгенерированных фраз

## Структура проекта
NameGeneratorApp/ </br>
├── src/ </br>
│   └── NameGeneratorApp.java  # Главный класс приложения </br>
├── lib/ </br>
│   └── json-simple-1.1.1.jar  # Библиотека для работы с JSON </br>
└── data.json                  # Файл с данными (создается автоматически) </br>

## Классы и методы
### Основные поля класса
```
private List<String> names = new ArrayList<>();       // Список имен
private List<String> nicknames = new ArrayList<>();   // Список псевдонимов
private LinkedList<String> lastPhrases = new LinkedList<>(); // История фраз
private final String DATA_FILE = "data.json";         // Файл для хранения данных
private Random random = new Random();                 // Генератор случайных чисел
private JFrame frame;                                // Главное окно приложения
private JLabel resultLabel;                          // Метка для отображения результата
```
