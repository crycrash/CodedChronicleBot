package org.example;
import org.apache.commons.io.IOUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
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

    public static void main(String[] args) throws TelegramApiException {
        SQLite.makeTable();
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(new CodedChronicleBot());
    }

    private String fileOpener() {
        String text;
        try (FileInputStream inputStream = new FileInputStream("src/main/resources/token.txt")) {
            text = IOUtils.toString(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return text;
    }

    public SendMessage finder() {
        String str = message_text;
        SendMessage message = new SendMessage();
        try (FileInputStream inputStream = new FileInputStream("src/main/resources/file_" + chat_id + ".txt")) {
            String text = IOUtils.toString(inputStream);
            int number = text.indexOf(str);
            StringBuilder fin = new StringBuilder();
            while (number < text.length()) {
                char c = text.charAt(number);
                if (c != '%') {
                    fin.append(c);
                } else {
                    break;
                }
                number++;
            }
            message.setChatId(String.valueOf(chat_id));
            message.setText(String.valueOf(fin));
        } catch (IOException ex) {

            System.out.println(ex.getMessage());
        }
        return message;
    }

    @Override
    public String getBotUsername() {
        return "CodedChronicleBot";
    }

    @Override
    public String getBotToken() {
        return fileOpener();
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
                List<String> years0;
                years0 = hhf.getYears(chat_id);
                try {
                    execute(sendYears(chat_id, years0));
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                session.setState(BotState.WAITING3);
            } else if (session.getState().equals(BotState.WAITING1)) {
                hhf.makeNote(chat_id, message_text, year, month, day);
                sendText(chat_id, "Текст успешно записан!");
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
                String year = date.substring(0, 4);
                String month = date.substring(5, 7);
                String day = date.substring(8, 10);
                if (hhf.check(chat_id, year, month, day)) {
                    try {
                        execute(reWriteOrNot(chat_id));
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    session.setState(BotState.REWAITING);
                } else {
                    sendText(chat_id, "Введите текст");
                    session.setState(BotState.WAITING1);
                }
            } else if (call_data.equals("YES1")) {
                //String note=hhf.getMessage(chat_id, year, month, day);
                sendText(chat_id,"отравьте новый текст");
                //sendText(chat_id,note);
                session.setState(BotState.REWAITING);
            }else if (call_data.equals("NO1")){
                    session.setState(BotState.NOTWAITING);
            }else if (call_data.equals("NO")) {
                try {
                    execute(sendConstantYears(chat_id));
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                session.setState(BotState.WAITING5);

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
            }  else if (session.getState().equals(BotState.WAITING4)) {
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
                String s = hhf.getMessage(chat_id, year, month, day);
                sendPhotoText(chat_id, s);
                session.setState(BotState.NOTWAITING);
            } else if (session.getState().equals(BotState.WAITING5)) {
                year = call_data;
                try {
                    execute(sendConstantMonths(chat_id));
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                session.setState(BotState.WAITING6);
            }
             else if (session.getState().equals(BotState.WAITING6)) {
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
            }
        }}


        public void sendText (Long who, String what){
            SendMessage sm = SendMessage.builder()
                    .chatId(who.toString()) //
                    .text(what).build();    //Message content
            try {
                execute(sm);                        //мы не любим это, можно пробовать отправлять еще раз
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);      //Any error will be printed here
            }
        }
    public SendMessage sendPreparedDays (Long who, ArrayList<Integer> daysInMonth)
    {
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
            if(s != 0){
                if (count <= 6){
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(String.valueOf(s));
                    button.setCallbackData(String.valueOf(s));
                    rowInLine1.add(button);
                }
                else if (count <= 12){
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(String.valueOf(s));
                    button.setCallbackData(String.valueOf(s));
                    rowInLine2.add(button);
                }
                else if (count <= 18){
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(String.valueOf(s));
                    button.setCallbackData(String.valueOf(s));
                    rowInLine3.add(button);
                }
                else if (count <= 24){
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(String.valueOf(s));
                    button.setCallbackData(String.valueOf(s));
                    rowInLine4.add(button);
                }else{
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(String.valueOf(s));
                    button.setCallbackData(String.valueOf(s));
                    rowInLine5.add(button);
                }
                count++;}
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

        public SendMessage variableOfTheme (Long who)
        {
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
        public SendMessage yesOrNo (Long who)
        {
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
    public SendMessage reWriteOrNot (Long who)
    {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(who));
        message.setText("Запись за эту дату уже есть" +
                "Хотите перезаписать заметку?");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> rowsInLine = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Да");
        button1.setCallbackData("YES1");

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Нет");
        button2.setCallbackData("NO1");

        rowsInLine.add(button1);
        rowsInLine.add(button2);

        markupInLine.setKeyboard(Collections.singletonList(rowsInLine));
        message.setReplyMarkup(markupInLine);

        return message;
    }

    public SendMessage sendYears (Long who, List<String> years)
    {
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
    public SendMessage sendMonths (Long who, List<String> months)
    {
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
    public SendMessage sendDays (Long who, List<String> days)
    {
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
        public void sendKeyboardMessage () {
            SendMessage sm = new SendMessage();
            sm.setChatId(String.valueOf(chat_id));
            ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();

            KeyboardRow row1 = new KeyboardRow();
            KeyboardRow row2 = new KeyboardRow();
            KeyboardRow row3 = new KeyboardRow();

            KeyboardButton button1 = new KeyboardButton("Создать заметку");
            KeyboardButton button2 = new KeyboardButton("Посмотреть заметку");
            KeyboardButton button3 = new KeyboardButton("Поменять оформление");

            row1.add(button1);
            row2.add(button2);
            row3.add(button3);

            List<KeyboardRow> keyboardRows = new ArrayList<>();
            keyboardRows.add(row1);
            keyboardRows.add(row2);
            keyboardRows.add(row3);

            keyboard.setKeyboard(keyboardRows);

            sm.setText("Добро пожаловать в свой личный дневник!");

            sm.setReplyMarkup(keyboard);

            try {
                execute(sm);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    public SendMessage sendConstantYears (Long who)
    {
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
    public SendMessage sendConstantMonths (Long who)
    {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(who));
        message.setText("Месяца");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine1 = new ArrayList<>();
        for(int i = 1;i<=6;i++){
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(String.valueOf(i));
            button.setCallbackData(String.valueOf(i));
            rowInLine1.add(button);
        }
        List<InlineKeyboardButton> rowInLine2 = new ArrayList<>();
        for(int i = 7;i<=12;i++){
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
        private void sendImageWhite () {
            path = "src/main/resources/pic2.jpeg";
        }
        private void sendImageBlue () {
            path = "src/main/resources/pic1.jpeg";
        }
        private void sendImageRad () {
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

    private List<String> wrapText(String text, int maxLength) {
        List<String> lines = new ArrayList<>();
        int length = text.length();

        for (int i = 0; i < length; i += maxLength) {
            int endIndex = Math.min(i + maxLength, length);
            lines.add(text.substring(i, endIndex));
        }

        return lines;
    }
        private void sendPhotoText (Long who,String str){
            //SendMessage sm = new SendMessage();
            //sm.setChatId(String.valueOf(who));
            //SendMessage textMessage = finder();
            File originalImage = new File(path);
            File processedImage = addTextToImage(str, originalImage);
            InputFile inputFile = new InputFile(processedImage);
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(String.valueOf(who));
            sendPhoto.setPhoto(inputFile);
            try {
                execute(sendPhoto);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }


