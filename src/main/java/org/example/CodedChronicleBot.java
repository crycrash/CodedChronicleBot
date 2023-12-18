package org.example;

import org.apache.commons.io.IOUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class CodedChronicleBot extends TelegramLongPollingBot {
    public static String message_text;
    private static CodedChronicleBot instance;

    public static synchronized CodedChronicleBot getInstance() {
        if (instance == null) {
            instance = new CodedChronicleBot();
        }
        return instance;
    }

    public CodedChronicleBot() {
        try {
            Connection hff = SQLite.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private long chat_id;
    static int f = 1;
    private final Map<Long, BotSession> userSessions = new HashMap<>();
    static Parametrs parametrs=new Parametrs();

    public static void main(String[] args) throws TelegramApiException {
        SQLite.makeTable();
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(new CodedChronicleBot());
    }

    static String fileOpener(String pa) {
        String text;
        try (FileInputStream inputStream = new FileInputStream(pa)) {
            text = IOUtils.toString(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return text;
    }

    @Override
    public String getBotUsername() {
        return "CodedChronicleBot";
    }

    @Override
    public String getBotToken() {
        return fileOpener("src/main/resources/token.txt");
    }

    public static String getPath() {
        return fileOpener("src/main/resources/path.txt");
    }

    private boolean mes(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            chat_id = update.getMessage().getChatId();
            message_text = update.getMessage().getText();
            return true;
        } else {
            return false;
        }
    }

    private boolean button(Update update) {
        if (update.hasCallbackQuery()) {
            chat_id = update.getCallbackQuery().getMessage().getChatId();
            return true;
        } else {
            return false;
        }
    }

    public void onUpdateReceived(Update update) {
        if (button(update)) {
            String buttonData = update.getCallbackQuery().getData();
            BotSession userSession = userSessions.computeIfAbsent(chat_id, k -> new BotSession());
            ButtonClick.processButtonClick(buttonData, userSession, chat_id);
        } else if (mes(update)) {
            SQLite.makeTable();
            BotSession userSession = userSessions.computeIfAbsent(chat_id, k -> new BotSession());
            MessageExecute.processMessage(update, userSession, chat_id);
        } else if (update.getMessage().hasPhoto()) {
            BotSession userSession = userSessions.computeIfAbsent(chat_id, k -> new BotSession());
            MainKeyboard.photo(userSession, chat_id, parametrs, update);
        }
    }

    void unsaveExecute(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    void photoExecute(long chat, File outputImage) {
        try {
            execute(SendPhoto.builder()
                    .chatId(String.valueOf(chat))
                    .photo(new InputFile(outputImage))
                    .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendText(Long who, String what) {
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString()) //
                .text(what).build();    //Message content
        unsaveExecute(sm);
    }
}