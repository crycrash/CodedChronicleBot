package org.example;

import org.telegram.telegrambots.meta.api.objects.Update;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.example.CodedChronicleBot.savePhotoToFile;

public class MainKeyboard {
    public static void main(String[] args) {
    }
    private static InlineKeyboards key = new InlineKeyboards();

    private Connection hff;

    public MainKeyboard() {
        try {
            this.hff = SQLite.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static void handleStartCommand(BotSession userSession, long chatId) {
        if (userSession.getState().equals(BotState.START)) {
            ReplyKeyboard sen = new ReplyKeyboard();
            new CodedChronicleBot().unsaveExecute(sen.sendKeyboard(chatId));
            userSession.setState(BotState.NOTWAITING);
        }
    }

    static void handleCreateNoteCommand(BotSession userSession, long chatId) {
        if (userSession.getState().equals(BotState.NOTWAITING)) {
            List<String> names = Arrays.asList("Да", "Нет");
            List<String> codes = Arrays.asList("YES", "NO");
            new CodedChronicleBot().unsaveExecute(key.sendKeyboard(chatId, names, codes, "Хотите за сегодня?"));
            System.out.println("11111111");

        }
    }

    static void handleChangeDesignCommand(BotSession userSession, long chatId) {
        if (userSession.getState().equals(BotState.NOTWAITING)) {
            List<String> names = Arrays.asList("\uD83E\uDD0D", "❤", "\uD83D\uDC99");
            List<String> codes = Arrays.asList("БЕЛОЕ", "КРАСНОЕ", "СИНЕЕ");
            new CodedChronicleBot().unsaveExecute(key.sendKeyboard(chatId, names, codes, "Выберите цветовую гамму"));
        }
    }

    static void handleViewNoteCommand(BotSession userSession, long chat_id) {
        System.out.println("qwert");
        CodedChronicleBot.f = 0;
        List<String> years0;
        years0 = SQLite.getYears(chat_id);
        if (!years0.isEmpty()) {
            System.out.println("1");
            for (String item:years0){
                System.out.println(item);
            }
            CodedChronicleBot.getInstance().unsaveExecute(key.sendKeyboard(chat_id, years0, years0, "Выберите год"));
            userSession.setState(BotState.WAITING3);
        } else {
            new CodedChronicleBot().sendText(chat_id, "У вас еще нет записей!");
            userSession.setState(BotState.NOTWAITING);
        }


    }

    static void handleEditNoteCommand(BotSession userSession, long chatId) {
        if (userSession.getState().equals(BotState.NOTWAITING)) {
            CodedChronicleBot.f = 1;
            List<String> years = SQLite.getYears(chatId);
            new CodedChronicleBot().unsaveExecute(key.sendKeyboard(chatId, years, years, "Выберите год"));
            userSession.setState(BotState.WAITING3);
        }
    }

    static void handleDeleteNotesCommand(BotSession userSession, long chatId) {
        if (userSession.getState().equals(BotState.NOTWAITING)) {
            List<String> names = Arrays.asList("Да", "Нет");
            List<String> codes = Arrays.
                    asList("900", "100");
            new CodedChronicleBot().unsaveExecute(key.sendKeyboard(chatId, names, codes, "Вы уверены?"));
            userSession.setState(BotState.WAITING2);
        }
    }
    static void photo(BotSession userSession, long chat_id,Parametrs parametrs, Update update) {
        System.out.println("внутри фото");
        if (parametrs.day.charAt(0) == '0') {
            parametrs.day = parametrs.day.substring(1, 2);
        }
        if (parametrs.month.charAt(0) == '0') {
            parametrs.month = parametrs.month.substring(1, 2);
        }
        savePhotoToFile(update, chat_id, parametrs.day, parametrs.month, parametrs.year);
        new CodedChronicleBot().sendText(chat_id, "заметка записана");
        userSession.setState(BotState.NOTWAITING);
    }
}
