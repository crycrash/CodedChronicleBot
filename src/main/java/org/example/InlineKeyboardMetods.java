package org.example;


import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class InlineKeyboardMetods {

    public static void main(String[] args) {
    }

    private Connection hff;

    public InlineKeyboardMetods() {
        try {
            this.hff = SQLite.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static InlineKeyboards key = new InlineKeyboards();

    static void greenHeart(BotSession userSession, long chat_id) {
        new SendPhotos().sendImageWhite();
        new CodedChronicleBot().sendText(chat_id, "Выбрана зеленая тема!");
    }

    static void redHeart(BotSession userSession, long chat_id) {
        new SendPhotos().sendImageRad();
        new CodedChronicleBot().sendText(chat_id, "Выбрана красная тема!");
    }

    static void blueHeart(BotSession userSession, long chat_id) {
        new SendPhotos().sendImageBlue();
        new CodedChronicleBot().sendText(chat_id, "Выбрана синяя тема!");
    }

    static Parametrs noteForToday(BotSession userSession, long chat_id, Parametrs par) {
        LocalDate dat = LocalDate.now();
        String date = dat.toString();
        String year1 = date.substring(0, 4);
        String month1 = date.substring(5, 7);
        String day1 = date.substring(8, 10);
        par.year = year1;
        par.month = month1;
        par.day = day1;
        if (day1.charAt(0) == '0') {
            day1 = day1.substring(1, 2);
            par.day = day1;
        }
        if (month1.charAt(0) == '0') {
            month1 = month1.substring(1, 2);
            par.month = month1;
        }
        if (SQLite.check(chat_id, year1, month1, day1)) {
            new CodedChronicleBot().sendText(chat_id, "запись за эту дату уже есть, выберете другую");
            userSession.setState(BotState.NOTWAITING);
        } else {
            new CodedChronicleBot().sendText(chat_id, "Введите текст");
            userSession.setState(BotState.WAITING1);
        }
        return par;
    }

    static void notTodayNote(BotSession userSession, long chat_id) {
        List<String> names = Arrays.asList("2023", "2022", "2021");
        new CodedChronicleBot().unsaveExecute(key.sendKeyboard(chat_id, names, names, "Года"));
        userSession.setState(BotState.WAITING5);
    }

    static void justNo(BotSession userSession, long chat_id) {
        userSession.setState(BotState.NOTWAITING);
    }

    static void noPhoto(BotSession userSession, long chat_id) {
        new CodedChronicleBot().sendText(chat_id, "Текст успешно записан");
        userSession.setState(BotState.NOTWAITING);
    }

    static void yesPhoto(BotSession userSession, long chat_id) {
        new CodedChronicleBot().sendText(chat_id, "отправьте фото");
    }

    static void rewrite(BotSession userSession, long chat_id) {
        new CodedChronicleBot().sendText(chat_id, "отправьте новый текст");
        userSession.setState(BotState.REWAITING);
    }

    static void add(BotSession userSession, long chat_id) {
        new CodedChronicleBot().sendText(chat_id, "отправьте текст");
        userSession.setState(BotState.WAITINGAFTERADD);
    }
}
