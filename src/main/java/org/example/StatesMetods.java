package org.example;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatesMetods {
    public static void main(String[] args) {
    }

    private Connection hff;

    public StatesMetods() {
        try {
            this.hff = SQLite.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static InlineKeyboards key = new InlineKeyboards();


    static void waiting1(BotSession userSession, long chat_id, String message_text, String year, String month, String day) {
        if (message_text.length() > 1130) {
            new CodedChronicleBot().sendText(chat_id, "Ваше сообщение слишком длинное! Ограничение по символам 1130. Попробуйте ввести снова...");
        } else {
            SQLite.makeNote(chat_id, message_text, year, month, day);
            List<String> names = Arrays.asList("Да", "Нет");
            List<String> codes = Arrays.asList("YESPHOTO", "NOPHOTO");
            new CodedChronicleBot().unsaveExecute(key.sendKeyboard(chat_id, names, codes, "Хотите добавить фото к заметке?"));
            userSession.setState(BotState.NOTWAITING);

        }
    }

    static void waitingafteradd(BotSession userSession, long chat_id, String message_text, String year, String month, String day) {
        String note = SQLite.getMessage(chat_id, year, month, day);
        String newNote = note + "\r\n" + message_text;
        SQLite.rewrite(chat_id, newNote, year, month, day);
        new CodedChronicleBot().sendText(chat_id, "Текст успешно перезаписан!");
        userSession.setState(BotState.NOTWAITING);

    }

    static void rewaiting(BotSession userSession, long chat_id, String message_text, String year, String month, String day) {
        SQLite.rewrite(chat_id, message_text, year, month, day);
        new CodedChronicleBot().sendText(chat_id, "тест перезаписан");
        userSession.setState(BotState.NOTWAITING);
    }

    static String waitingYEAR(BotSession userSession, long chat_id, String message_text, Parametrs parametrs, String buttonData, int f) {
        parametrs.year = buttonData;
        System.out.println(parametrs.year);
        System.out.println(parametrs.month);
        System.out.println(parametrs.day);
        List<String> months0;
        months0 = SQLite.getMonths(chat_id, parametrs.year);
        CodedChronicleBot.getInstance().unsaveExecute(key.sendKeyboard(chat_id, months0, months0, "Выберите месяц"));
        userSession.setState(BotState.WAITING4);
        return parametrs.year;
    }

    static void waitingMONTH(BotSession userSession, long chat_id, String message_text, Parametrs parametrs, String buttonData, int f,String year) {
        parametrs.month = buttonData;
        System.out.println(parametrs.year);
        System.out.println(parametrs.month);
        System.out.println(parametrs.day);
        ArrayList<Integer> days0;
        days0 = SQLite.getDays(chat_id, parametrs.year, parametrs.month);
        for (Integer item : days0){
            System.out.println(item);
        }
        CodedChronicleBot.getInstance().unsaveExecute(key.sendPreparedDays(chat_id, days0, "Выберите день"));
        userSession.setState(BotState.WAITING_AFTER_DAY);
    }

    static void waitingDAY(BotSession userSession, long chat_id, String message_text, Parametrs parametrs, String buttonData,int f) {
        parametrs.day = buttonData;
        System.out.println(parametrs.year);
        System.out.println(parametrs.month);
        System.out.println(parametrs.day);
        if (f == 0) {

            String s = SQLite.getMessage(chat_id, parametrs.year, parametrs.month, parametrs.day);
            String date = parametrs.day + "." + parametrs.month + "." + parametrs.year;
            String name = chat_id + "." + parametrs.year + parametrs.month + parametrs.day + ".jpg";
            if (CodedChronicleBot.checkPhoto(CodedChronicleBot.getPath(), name) == 1) {
                System.out.println("не зашел");
                String photo = CodedChronicleBot.getPath() + "/" + name;
                new CodedChronicleBot().sendOverlappingImage(CodedChronicleBot.createPhotoText(chat_id, s, date), photo);
            } else {
                System.out.println("зашел");
                new CodedChronicleBot().sendPhotoText(chat_id, s, date);
            }
        }
        if (f == 1) {
            List<String> names = Arrays.asList("Перезапись", "Добавление");
            List<String> codes = Arrays.asList("rewrite", "add");
            new CodedChronicleBot().unsaveExecute(key.sendKeyboard(chat_id, names, codes, "Что хотите сделать с записью?"));
        }
        userSession.setState(BotState.NOTWAITING);
    }

    static void waitingToSendYear(BotSession userSession, long chat_id, String message_text, String year, String month, String day, String buttonData) {
        year = buttonData;
        new CodedChronicleBot().unsaveExecute(key.sendConstantMonths(chat_id));
        userSession.setState(BotState.WAITING6);
    }

    static void waitingToSendMonth(BotSession userSession, long chat_id, String message_text, String year, String month, String day, String buttonData) {
        month = buttonData;
        ArrayList<Integer> daysInMonth = SQLite.makeNoteNo(chat_id, year, month);
        new CodedChronicleBot().unsaveExecute(key.sendPreparedDays(chat_id, daysInMonth, "Выберите день"));
        userSession.setState(BotState.WAITING7);
    }

    static void waitingToSendDay(BotSession userSession, long chat_id, String message_text, String year, String month, String day, String buttonData) {
        day = buttonData;
        new CodedChronicleBot().sendText(chat_id, "Введите текст");
        userSession.setState(BotState.WAITING1);
    }

    static void delite1(BotSession userSession, long chat_id, String message_text, String year, String month, String day, String buttonData) {
        if (buttonData.equals("900")) {
            List<String> names = Arrays.asList("Да", "Нет");
            List<String> codes = Arrays.asList("800", "200");
            new CodedChronicleBot().unsaveExecute(key.sendKeyboard(chat_id, names, codes, "Вы точно уверены?"));
            userSession.setState(BotState.WAITING8);
        }
        if (buttonData.equals("100")) {
            new CodedChronicleBot().sendText(chat_id, "Ну и хорошо!");
            userSession.setState(BotState.NOTWAITING);
        }
    }
    static void delite2(BotSession userSession, long chat_id, String message_text, String year, String month, String day, String buttonData) {
        if (buttonData.equals("800")) {
            SQLite.deleteAll(chat_id);
            CodedChronicleBot.deletePhotos(chat_id, CodedChronicleBot.getPath());
            new CodedChronicleBot().sendText(chat_id, "Все ваши заметки удалены!");
        }
        if (buttonData.equals("200")) {
            new CodedChronicleBot().sendText(chat_id, "Ну и хорошо!");
        }
        userSession.setState(BotState.NOTWAITING);
    }
}
