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
    String path = "src/main/resources/1sh.png"; //дефолтная картинка
    String date;

    String year;
    String month;
    String day;

    public static void main(String[] args) throws TelegramApiException {
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

    private boolean mes(Update update) { //чтобы работали кнопки
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
        SQLite.makeTable();
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
                hhf.makeNote(chat_id, message_text, date);
                sendText(chat_id, "Текст успешно записан!");
                session.setState(BotState.NOTWAITING);
            } else if (session.getState().equals(BotState.WAITING2)) {
                date = message_text;
                sendText(chat_id, "Введите текст");
                session.setState(BotState.WAITING1);
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
                LocalDate dat = LocalDate.now();
                date = dat.toString();
                sendText(chat_id, "Введите текст");
                session.setState(BotState.WAITING1);
            } else if (call_data.equals("NO")) {
                sendText(chat_id, "Введите дату");
                session.setState(BotState.WAITING2);
            } else if (session.getState().equals(BotState.WAITING3)){
                year = call_data;
                List<String> months0;
                months0 = hhf.getMonths(chat_id, year);
                try {
                    execute(sendMonths(chat_id, months0));
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                session.setState(BotState.WAITING4);
            } else if (session.getState().equals(BotState.WAITING4)){
                month = call_data;
                List<String> days0;
                days0 = hhf.getDays(chat_id, year, month);
                try {
                    execute(sendDays(chat_id, days0));
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                session.setState(BotState.WAITING_AFTER_DAY);
            } else if (session.getState().equals(BotState.WAITING_AFTER_DAY)){
                day = call_data;
                String s = hhf.getMessage(chat_id, year, month, day);
                sendText(chat_id, s);
                session.setState(BotState.NOTWAITING);
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
            System.out.println("kkk");
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
        private void sendImageWhite () {
            path = "src/main/resources/2sh.png";
        }
        private void sendImageBlue () {
            path = "src/main/resources/3sh.png";
        }
        private void sendImageRad () {
            path = "src/main/resources/1sh.png";
        }
        private File addTextToImage (String text, File originalImage){
            try {
                BufferedImage image = ImageIO.read(originalImage);
                Graphics2D g2d = image.createGraphics();

                g2d.setFont(new Font("SansSerif", Font.BOLD, 40));


                g2d.setColor(Color.black); //Цвет текста
                g2d.drawString(text, 70, 40);

                g2d.dispose();

                File outputImage = new File("src/main/resources/output_" + originalImage.getName());
                ImageIO.write(image, "jpg", outputImage);

                return outputImage;
            } catch (IOException e) {
                throw new RuntimeException("Failed to process the image", e);
            }
        }
        private void sendPhotoText (Long who){
            SendMessage sm = new SendMessage();
            sm.setChatId(String.valueOf(who));
            SendMessage textMessage = finder();
            File originalImage = new File(path);
            File processedImage = addTextToImage(textMessage.getText(), originalImage);
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


