package org.example;


public class Main {
    public static void main(String[] args) {
        // Запускаем графическое приложение через класс-обертку, обходя ограничения модулей
        org.example.ui.MainApp.main(args);
    }

}
