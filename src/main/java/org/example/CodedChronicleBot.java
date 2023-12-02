package org.example;
import org.apache.commons.io.IOUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.awt.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;

public class CodedChronicleBot extends TelegramLongPollingBot {

    long chat_id;
    BotSession session = new BotSession();
    String message_text;
    String call_data;
    String path = "src/main/resources/pic1.jpeg"; //дефолтная картинка
    String date;

    String year;
    String month;
    String day;
    int f = 1;

    public static void main(String[] args) throws TelegramApiException {
        SQLite.makeTable();
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(new CodedChronicleBot());
    }

    private String fileOpener(String pa) {
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

    private boolean mes(Update update) { //чтобы работали teкнопки
        if (update.hasMessage() && update.getMessage().hasText()) {
            chat_id = update.getMessage().getChatId();
            message_text = update.getMessage().getText();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        SQLite hhf;
        try {
            hhf = new SQLite();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (mes(update)) {
            SQLite.makeTable();
            if (message_text.equals("/start") && session.getState().equals(BotState.START)) {
                sendKeyboardMessage();
                session.setState(BotState.NOTWAITING);
            }
            if (message_text.equals("Создать заметку") && session.getState().equals(BotState.NOTWAITING)) {
                try {
                    execute(yesOrNo(chat_id));
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if (message_text.equals("Поменять оформление") && session.getState().equals(BotState.NOTWAITING)) {
                try {
                    execute(variableOfTheme(chat_id));
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if (message_text.equals("Посмотреть заметку") && session.getState().equals(BotState.NOTWAITING)) {
                f = 0;
                List<String> years0;
                years0 = hhf.getYears(chat_id);
                if (!years0.isEmpty()){
                    try {
                        execute(sendYears(chat_id, years0));
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    session.setState(BotState.WAITING3);
                }else{
                    sendText(chat_id, "У вас еще нет записей!");
                    session.setState(BotState.NOTWAITING);
                }
            } else if (message_text.equals("Редактировать запись") && session.getState().equals(BotState.NOTWAITING)) {
                f = 1;
                List<String> years0;
                years0 = hhf.getYears(chat_id);
                try {
                    execute(sendYears(chat_id, years0));
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                session.setState(BotState.WAITING3);
            }else if (message_text.equals("Удалить записи") && session.getState().equals(BotState.NOTWAITING)) {
                try {
                    execute(firstAsk(chat_id));
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                session.setState(BotState.WAITING2);
            } else if (session.getState().equals(BotState.WAITING1)) {
                //sendText(chat_id,"запись за эту дату уже есть, выберете другую");
                hhf.makeNote(chat_id, message_text, year, month, day);
                try {
                    execute(photo(chat_id));
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                session.setState(BotState.NOTWAITING);
            } else if (session.getState().equals(BotState.WAITINGAFTERADD)) {
                String note = hhf.getMessage(chat_id, year, month, day);
                String newNote = note + "\r\n" + message_text;
                hhf.rewrite(chat_id, newNote, year, month, day);
                sendText(chat_id, "Текст успешно перезаписан!");
                session.setState(BotState.NOTWAITING);
            } else if (session.getState().equals(BotState.REWAITING)) {
                hhf.rewrite(chat_id, message_text, year, month, day);
                sendText(chat_id, "тест перезаписан");
                session.setState(BotState.NOTWAITING);
            }
        } else if (update.hasCallbackQuery()) {//если кнопка
            call_data = update.getCallbackQuery().getData();
            chat_id = update.getCallbackQuery().getMessage().getChatId();
            if (call_data.equals("БЕЛОЕ")) {//
                sendImageWhite();
                sendText(chat_id, "Выбрана зеленая тема!");
            } else if (call_data.equals("СИНЕЕ")) {
                sendImageBlue();
                sendText(chat_id, "Выбрана синяя тема!");
            } else if (call_data.equals("КРАСНОЕ")) {
                sendImageRad();
                sendText(chat_id, "Выбрана красная тема!");
            } else if (call_data.equals("YES") && session.getState().equals(BotState.NOTWAITING)) {
                //ЗАПИСЬ ЗА СЕГОДНЯ
                LocalDate dat = LocalDate.now();
                date = dat.toString();
                year = date.substring(0, 4);
                month = date.substring(5, 7);
                day = date.substring(8, 10);
                if (hhf.check(chat_id, year, month, day)) {
                    sendText(chat_id, "запись за эту дату уже есть, выберете другую");
                    session.setState(BotState.NOTWAITING);
                } else {
                    sendText(chat_id, "Введите текст");
                    session.setState(BotState.WAITING1);
                }
            } else if (call_data.equals("NO1")) {
                session.setState(BotState.NOTWAITING);
            } else if (call_data.equals("NO")) {
                try {
                    execute(sendConstantYears(chat_id));
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                session.setState(BotState.WAITING5);
            } else if (call_data.equals("NOPHOTO")) {
                sendText(chat_id, "Текст успешно записан");
                session.setState(BotState.NOTWAITING);
            } else if (call_data.equals("YESPHOTO")) {
                sendText(chat_id, "отправьте фото");
            } else if (session.getState().equals(BotState.WAITING3)) {
                year = call_data;
                List<String> months0;
                months0 = hhf.getMonths(chat_id, year);
                try {
                    execute(sendMonths(chat_id, months0));
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                session.setState(BotState.WAITING4);
            } else if (session.getState().equals(BotState.WAITING4)) {
                month = call_data;
                List<String> days0;
                days0 = hhf.getDays(chat_id, year, month);
                try {
                    execute(sendDays(chat_id, days0));
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                session.setState(BotState.WAITING_AFTER_DAY);
            } else if (session.getState().equals(BotState.WAITING_AFTER_DAY)) {
                day = call_data;
                if (f == 0) {
                    String s = hhf.getMessage(chat_id, year, month, day);
                    String date = day + "." + month + "." + year;
                    sendPhotoText(chat_id, s, date);
                }
                if (f == 1) {
                    try {
                        execute(editing(chat_id));
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
                session.setState(BotState.NOTWAITING);
            } else if (message_text.equals("rewrite")) {
                sendText(chat_id, "отправьте новый текст");
                session.setState(BotState.REWAITING);
            } else if (session.getState().equals(BotState.WAITING5)) {
                year = call_data;
                try {
                    execute(sendConstantMonths(chat_id));
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                session.setState(BotState.WAITING6);
            } else if (session.getState().equals(BotState.WAITING6)) {
                month = call_data;
                ArrayList<Integer> daysInMonth = hhf.makeNoteNo(chat_id, year, month);
                try {
                    execute(sendPreparedDays(chat_id, daysInMonth));
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                session.setState(BotState.WAITING7);
            } else if (session.getState().equals(BotState.WAITING7)) {
                day = call_data;
                sendText(chat_id, "Введите текст");
                session.setState(BotState.WAITING1);
            } else if (call_data.equals("rewrite")) {
                sendText(chat_id, "отправьте новый текст");
                session.setState(BotState.REWAITING);
            } else if (call_data.equals("add")) {
                sendText(chat_id, "отправьте текст");
                session.setState(BotState.WAITINGAFTERADD);
            } else if (session.getState().equals(BotState.WAITING2)) {
                if (call_data.equals("900")){
                    try {
                        execute(secondAsk(chat_id));
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    session.setState(BotState.WAITING8);
                }if (call_data.equals("100")){
                    sendText(chat_id, "Ну и хорошо!");
                    session.setState(BotState.NOTWAITING);
                }
            }else if (session.getState().equals(BotState.WAITING8)) {
                if (call_data.equals("800")){
                    hhf.deleteAll(chat_id);
                    sendText(chat_id, "Все ваши заметки удалены!");
                }if (call_data.equals("200")){
                    sendText(chat_id, "Ну и хорошо!");
                }
                session.setState(BotState.NOTWAITING);
            }

        } else if (update.getMessage().hasPhoto()) {
            savePhotoToFile(update, day, month, year);
            sendText(chat_id, "заметка записана");
            session.setState(BotState.NOTWAITING);
        }
    }

    public void sendText(Long who, String what) {
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString()) //
                .text(what).build();    //Message content
        try {
            execute(sm);                        //мы не любим это, можно пробовать отправлять еще раз
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }

    public SendMessage sendPreparedDays(Long who, ArrayList<Integer> daysInMonth) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(who));
        message.setText("Выберите день");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine2 = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine3 = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine4 = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine5 = new ArrayList<>();
        int count = 0;
        for (int s : daysInMonth) {
            if (s != 0) {
                if (count <= 6) {
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(String.valueOf(s));
                    button.setCallbackData(String.valueOf(s));
                    rowInLine1.add(button);
                } else if (count <= 12) {
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(String.valueOf(s));
                    button.setCallbackData(String.valueOf(s));
                    rowInLine2.add(button);
                } else if (count <= 18) {
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(String.valueOf(s));
                    button.setCallbackData(String.valueOf(s));
                    rowInLine3.add(button);
                } else if (count <= 24) {
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(String.valueOf(s));
                    button.setCallbackData(String.valueOf(s));
                    rowInLine4.add(button);
                } else {
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(String.valueOf(s));
                    button.setCallbackData(String.valueOf(s));
                    rowInLine5.add(button);
                }
                count++;
            }
        }

        rowsInLine.add(rowInLine1);
        rowsInLine.add(rowInLine2);
        rowsInLine.add(rowInLine3);
        rowsInLine.add(rowInLine4);
        rowsInLine.add(rowInLine5);
        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);

        return message;
    }


    public SendMessage variableOfTheme(Long who) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(who));
        message.setText("Выберите цветовую гамму");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> rowsInLine = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("\uD83E\uDD0D");
        button1.setCallbackData("БЕЛОЕ");

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("❤");
        button2.setCallbackData("КРАСНОЕ");

        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("\uD83D\uDC99");
        button3.setCallbackData("СИНЕЕ");

        rowsInLine.add(button1);
        rowsInLine.add(button2);
        rowsInLine.add(button3);

        markupInLine.setKeyboard(Collections.singletonList(rowsInLine));
        message.setReplyMarkup(markupInLine);

        return message;
    }

    public SendMessage firstAsk(Long who) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(who));
        message.setText("Вы уверены?");
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> rowsInLine = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Да");
        button1.setCallbackData("900");
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Нет");
        button2.setCallbackData("100");
        rowsInLine.add(button1);
        rowsInLine.add(button2);
        markupInLine.setKeyboard(Collections.singletonList(rowsInLine));
        message.setReplyMarkup(markupInLine);
        return message;
    }
    public SendMessage secondAsk(Long who) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(who));
        message.setText("Вы точно уверены?");
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> rowsInLine = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Да");
        button1.setCallbackData("800");
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Нет");
        button2.setCallbackData("200");
        rowsInLine.add(button1);
        rowsInLine.add(button2);
        markupInLine.setKeyboard(Collections.singletonList(rowsInLine));
        message.setReplyMarkup(markupInLine);
        return message;
    }

    public SendMessage yesOrNo(Long who) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(who));
        message.setText("Хотите за сегодня?");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> rowsInLine = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Да");
        button1.setCallbackData("YES");

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Нет");
        button2.setCallbackData("NO");

        rowsInLine.add(button1);
        rowsInLine.add(button2);

        markupInLine.setKeyboard(Collections.singletonList(rowsInLine));
        message.setReplyMarkup(markupInLine);

        return message;
    }

    public SendMessage photo(Long who) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(who));
        message.setText("Хотите добавить фото к заметке?");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> rowsInLine = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Да");
        button1.setCallbackData("YESPHOTO");

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Нет");
        button2.setCallbackData("NOPHOTO");

        rowsInLine.add(button1);
        rowsInLine.add(button2);

        markupInLine.setKeyboard(Collections.singletonList(rowsInLine));
        message.setReplyMarkup(markupInLine);

        return message;
    }

    public SendMessage editing(Long who) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(who));
        message.setText("Что хотите сделать с записью?");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> rowsInLine = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("перезаписать");
        button1.setCallbackData("rewrite");

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("добавить");
        button2.setCallbackData("add");

        rowsInLine.add(button1);
        rowsInLine.add(button2);

        markupInLine.setKeyboard(Collections.singletonList(rowsInLine));
        message.setReplyMarkup(markupInLine);

        return message;
    }

    public SendMessage sendYears(Long who, List<String> years) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(who));
        message.setText("Выберите год");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> rowsInLine = new ArrayList<>();
        for (String s : years) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(s);
            button.setCallbackData(s);
            rowsInLine.add(button);
        }
        markupInLine.setKeyboard(Collections.singletonList(rowsInLine));
        message.setReplyMarkup(markupInLine);
        return message;
    }

    public SendMessage sendMonths(Long who, List<String> months) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(who));
        message.setText("Выберите месяц");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> rowsInLine = new ArrayList<>();
        for (String s : months) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(s);
            button.setCallbackData(s);
            rowsInLine.add(button);
        }
        markupInLine.setKeyboard(Collections.singletonList(rowsInLine));
        message.setReplyMarkup(markupInLine);

        return message;
    }

    public SendMessage sendDays(Long who, List<String> days) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(who));
        message.setText("Выберите день");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> rowsInLine = new ArrayList<>();
        for (String s : days) {

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(s);
            button.setCallbackData(s);
            rowsInLine.add(button);
        }

        markupInLine.setKeyboard(Collections.singletonList(rowsInLine));
        message.setReplyMarkup(markupInLine);

        return message;
    }

    public void sendKeyboardMessage() {
        SendMessage sm = new SendMessage();
        sm.setChatId(String.valueOf(chat_id));
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();

        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        KeyboardRow row4 = new KeyboardRow();
        KeyboardRow row5 = new KeyboardRow();

        KeyboardButton button1 = new KeyboardButton("Создать заметку");
        KeyboardButton button2 = new KeyboardButton("Посмотреть заметку");
        KeyboardButton button3 = new KeyboardButton("Поменять оформление");
        KeyboardButton button4 = new KeyboardButton("Редактировать запись");
        KeyboardButton button5 = new KeyboardButton("Удалить записи");

        row1.add(button1);
        row2.add(button2);
        row3.add(button3);
        row4.add(button4);
        row5.add(button5);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(row1);
        keyboardRows.add(row2);
        keyboardRows.add(row3);
        keyboardRows.add(row4);
        keyboardRows.add(row5);

        keyboard.setKeyboard(keyboardRows);

        sm.setText("Добро пожаловать в свой личный дневник!");

        sm.setReplyMarkup(keyboard);

        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public SendMessage sendConstantYears(Long who) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(who));
        message.setText("Года");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> rowsInLine = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("2023");
        button1.setCallbackData("2023");

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("2022");
        button2.setCallbackData("2022");

        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("2021");
        button3.setCallbackData("2021");

        rowsInLine.add(button1);
        rowsInLine.add(button2);
        rowsInLine.add(button3);

        markupInLine.setKeyboard(Collections.singletonList(rowsInLine));
        message.setReplyMarkup(markupInLine);

        return message;
    }

    public SendMessage sendConstantMonths(Long who) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(who));
        message.setText("Месяца");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine1 = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(String.valueOf(i));
            button.setCallbackData(String.valueOf(i));
            rowInLine1.add(button);
        }
        List<InlineKeyboardButton> rowInLine2 = new ArrayList<>();
        for (int i = 7; i <= 12; i++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(String.valueOf(i));
            button.setCallbackData(String.valueOf(i));
            rowInLine2.add(button);
        }
        rowsInLine.add(rowInLine1);
        rowsInLine.add(rowInLine2);
        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);

        return message;
    }

    private void sendImageWhite() {
        path = "src/main/resources/pic2.jpeg";
    }

    private void sendImageBlue() {
        path = "src/main/resources/pic1.jpeg";
    }

    private void sendImageRad() {
        path = "src/main/resources/pic3.jpeg";
    }

    private File addTextToImage(String text, File originalImage) {
        try {
            BufferedImage image = ImageIO.read(originalImage);
            Graphics2D g2d = image.createGraphics();

            g2d.setFont(new Font("SansSerif", Font.BOLD, 30));
            g2d.setColor(Color.black); // Цвет текста

            int maxWidth = 40; // Максимальная длина строки
            float lineHeight = 31.999982f; // Высота текста

            List<String> lines = wrapText(text, maxWidth); // Разделение текста на строки

            int y = 149; // Начальная позиция по оси Y
            for (String line : lines) {
                g2d.drawString(line, 15, y); // Отображение строки
                y += lineHeight; // Увеличение позиции по оси Y для следующей строки
            }

            g2d.dispose();

            File outputImage = new File("src/main/resources/output_" + originalImage.getName());
            ImageIO.write(image, "jpg", outputImage);

            return outputImage;
        } catch (IOException e) {
            throw new RuntimeException("Failed to process the image", e);
        }
    }

    private File addTextToImage1(String text, File originalImage) {
        try {
            BufferedImage image = ImageIO.read(originalImage);
            Graphics2D g2d = image.createGraphics();

            g2d.setFont(new Font("SansSerif", Font.BOLD, 46));


            g2d.setColor(Color.black); //Цвет текста
            g2d.drawString(text, 525, 105);

            g2d.dispose();

            File outputImage = new File("src/main/resources/output_" + originalImage.getName());
            ImageIO.write(image, "jpg", outputImage);

            return outputImage;
        } catch (IOException e) {
            throw new RuntimeException("Failed to process the image", e);
        }
    }

    private List<String> wrapText(String text, int maxLength) {
        List<String> lines = new ArrayList<>();
        int length = text.length();

        for (int i = 0; i < length; i += maxLength) {
            int endIndex = Math.min(i + maxLength, length);
            lines.add(text.substring(i, endIndex));
        }

        return lines;
    }

    private File createPhotoText(Long who, String str, String date) {
        File originalImage = new File(path);
        File processedImage = addTextToImage(str, originalImage);
        File processedImage1 = addTextToImage1(date, processedImage);
        return processedImage1;
    }


    private void savePhotoToFile(Update update, String day, String month, String year) {
        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            // Получаем фото из сообщения пользователя
            PhotoSize photo = update.getMessage().getPhoto().stream()
                    .sorted((ps1, ps2) -> Integer.compare(ps2.getFileSize(), ps1.getFileSize()))
                    .findFirst()
                    .orElse(null);
            if (photo != null) {
                try {
                    // Получаем путь к фото
                    String filePath = getFilePath(photo.getFileId());

                    // URL для загрузки фото из Telegram
                    String t = fileOpener("src/main/resources/token.txt");
                    String fileURL = "https://api.telegram.org/file/bot" + t + "/" + filePath;

                    // Открываем поток для чтения фото
                    InputStream inputStream = new URL(fileURL).openStream();
                    // Создаем файл для сохранения фото в указанной папке
                    String savePath = "C:\\Users\\Юзер\\Desktop\\tg_photos";
                    String name = year + month + day; // Пример значения переменной name
                    //String filePath = "/path/to/file"; // Пример значения переменной filePath
                    String fileName = name + filePath.substring(filePath.lastIndexOf('.'));
                    File photoFile = new File(savePath, fileName);

                    // Создаем поток для записи фото в файл
                    FileOutputStream outputStream = new FileOutputStream(photoFile);

                    // Читаем фото из потока и записываем в файл
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    // Закрываем потоки
                    inputStream.close();
                    outputStream.close();

                    // Выводим сообщение об успешном сохранении
                    System.out.println("Фото успешно сохранено по пути: " + photoFile.getAbsolutePath());
                } catch (TelegramApiException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Метод для получения пути к файлу по его ID
    private String getFilePath(String fileId) throws TelegramApiException {
        GetFile getFileRequest = new GetFile(fileId);
        return execute(getFileRequest).getFilePath();
    }
    private void sendOverlappingImage(File file1, String path2) {
        try {
            // Загрузка изображений
            BufferedImage image1 = ImageIO.read(file1);
            BufferedImage image2 = ImageIO.read(new File(path2));

            // Создание нового изображения с размерами первого изображения
            BufferedImage overlappingImage = new BufferedImage(image1.getWidth(), image1.getHeight(), BufferedImage.TYPE_INT_ARGB);

            // Наложение второго изображения на первое
            int newWidth = 300;
            int newHeight = 300;

// Создание BufferedImage для измененного размера
            BufferedImage resizedImage2 = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

// Наложение второго изображения на измененный BufferedImage
            Graphics2D g2dResized = resizedImage2.createGraphics();
            g2dResized.drawImage(image2, 0, 0, newWidth, newHeight, null);
            g2dResized.dispose();

// Наложение измененного второго изображения на первое
            Graphics2D g2d = overlappingImage.createGraphics();
            g2d.drawImage(image1, 0, 0, null);
            g2d.drawImage(resizedImage2, 200, 200, null);
            g2d.dispose();

            // Сохранение полученного изображения в файл
            File outputImage = new File("output.png");
            ImageIO.write(overlappingImage, "png", outputImage);

            // Отправка изображения пользователю
            execute(SendPhoto.builder()
                    .chatId(String.valueOf(chat_id))
                    .photo(new InputFile(outputImage))
                    .build());

        } catch (IOException | TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void sendPhotoText(Long who, String str, String date) {
        File originalImage = new File(path);
        File processedImage = addTextToImage(str, originalImage);
        File processedImage1 = addTextToImage1(date, processedImage);
        InputFile inputFile = new InputFile(processedImage1);
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(who));
        sendPhoto.setPhoto(inputFile);
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }}
}


