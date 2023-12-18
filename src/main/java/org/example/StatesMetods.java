package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatesMetods {
    public static void main(String[] args) {
    }

    private static final InlineKeyboards key = new InlineKeyboards();


    static void waiting1(BotSession userSession, long chat_id, String message_text, Parametrs parametrs) {
        if (message_text.length() > 1130) {
            new CodedChronicleBot().sendText(chat_id, "Ваше сообщение слишком длинное! Ограничение по символам 1130. Попробуйте ввести снова...");
        } else {
            SQLite.makeNote(chat_id, message_text, parametrs.year, parametrs.month, parametrs.day);
            List<String> names = Arrays.asList("Да", "Нет");
            List<String> codes = Arrays.asList("YESPHOTO", "NOPHOTO");
            new CodedChronicleBot().unsaveExecute(key.sendKeyboard(chat_id, names, codes, "Хотите добавить фото к заметке?"));
            userSession.setState(BotState.NOTWAITING);

        }
    }

    static void waitingafteradd(BotSession userSession, long chat_id, String message_text, Parametrs parametrs) {
        String note = SQLite.getMessage(chat_id, parametrs.year, parametrs.month, parametrs.day);
        String newNote = note + "\r\n" + message_text;
        SQLite.rewrite(chat_id, newNote, parametrs.year, parametrs.month, parametrs.day);
        new CodedChronicleBot().sendText(chat_id, "Текст успешно перезаписан!");
        userSession.setState(BotState.NOTWAITING);

    }

    static void rewaiting(BotSession userSession, long chat_id, String message_text, Parametrs parametrs) {
        SQLite.rewrite(chat_id, message_text, parametrs.year, parametrs.month, parametrs.day);
        new CodedChronicleBot().sendText(chat_id, "тест перезаписан");
        userSession.setState(BotState.NOTWAITING);
    }

    static String waitingYEAR(BotSession userSession, long chat_id, Parametrs parametrs, String buttonData) {
        parametrs.year = buttonData;
        List<String> months0;
        months0 = SQLite.getMonths(chat_id, parametrs.year);
        CodedChronicleBot.getInstance().unsaveExecute(key.sendKeyboard(chat_id, months0, months0, "Выберите месяц"));
        userSession.setState(BotState.WAITING4);
        return parametrs.year;
    }

    static void waitingMONTH(BotSession userSession, long chat_id, Parametrs parametrs, String buttonData) {
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

    static void waitingDAY(BotSession userSession, long chat_id, Parametrs parametrs, String buttonData,int f) {
        parametrs.day = buttonData;
        if (f == 0) {
            String s = SQLite.getMessage(chat_id, parametrs.year, parametrs.month, parametrs.day);
            String date = parametrs.day + "." + parametrs.month + "." + parametrs.year;
            String name = chat_id + "." + parametrs.year + parametrs.month + parametrs.day + ".jpg";
            if (SendPhotos.checkPhoto(CodedChronicleBot.getPath(), name) == 1) {
                System.out.println("не зашел");
                String photo = CodedChronicleBot.getPath() + "/" + name;
                System.out.println(photo);
                new SendPhotos().sendOverlappingImage(SendPhotos.createPhotoText(s, date), photo, chat_id);
            } else {
                System.out.println("зашел");
                new SendPhotos().sendPhotoText(chat_id, s, date);
            }
        }
        if (f == 1) {
            List<String> names = Arrays.asList("Перезапись", "Добавление");
            List<String> codes = Arrays.asList("rewrite", "add");
            new CodedChronicleBot().unsaveExecute(key.sendKeyboard(chat_id, names, codes, "Что хотите сделать с записью?"));
        }
        userSession.setState(BotState.NOTWAITING);
    }

    static String waitingToSendYear(BotSession userSession, long chat_id, Parametrs parametrs, String buttonData) {
        parametrs.year = buttonData;
        new CodedChronicleBot().unsaveExecute(key.sendConstantMonths(chat_id));
        userSession.setState(BotState.WAITING6);
        return parametrs.year;
    }

    static String waitingToSendMonth(BotSession userSession, long chat_id, Parametrs parametrs, String buttonData) {
        parametrs.month = buttonData;
        ArrayList<Integer> daysInMonth = SQLite.makeNoteNo(chat_id, parametrs.year, parametrs.month);
        assert daysInMonth != null;
        new CodedChronicleBot().unsaveExecute(key.sendPreparedDays(chat_id, daysInMonth, "Выберите день"));
        userSession.setState(BotState.WAITING7);
        return parametrs.month;
    }

    static void waitingToSendDay(BotSession userSession, long chat_id, Parametrs parametrs , String buttonData) {
        parametrs.day = buttonData;
        new CodedChronicleBot().sendText(chat_id, "Введите текст");
        userSession.setState(BotState.WAITING1);
    }

    static void delite1(BotSession userSession, long chat_id, String buttonData) {
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
    static void delite2(BotSession userSession, long chat_id, String buttonData) {
        if (buttonData.equals("800")) {
            SQLite.deleteAll(chat_id);
            SendPhotos.deletePhotos(chat_id, CodedChronicleBot.getPath());
            new CodedChronicleBot().sendText(chat_id, "Все ваши заметки удалены!");
        }
        if (buttonData.equals("200")) {
            new CodedChronicleBot().sendText(chat_id, "Ну и хорошо!");
        }
        userSession.setState(BotState.NOTWAITING);
    }
}
