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

import java.util.ArrayList;
import java.util.List;

public class CodedChronicleBot extends TelegramLongPollingBot {

    long chat_id;
    String message_text;
    String call_data;
    boolean startWait1 = false;
    boolean startWait2 = false;
    String path = "src/main/resources/1sh.png"; //дефолтная картинка


    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(new CodedChronicleBot());
    }
    private void fileMaker(){
        String fileName = "src/main/resources/file_" + chat_id + ".txt";
        try(FileWriter writer = new FileWriter(fileName, true))
        {
            writer.write(message_text + "%");
            writer.flush();
        }
        catch(IOException ex){

            System.out.println(ex.getMessage());
        }
    }

    private String fileOpener(String namePath){
        String text;
        try(FileInputStream inputStream = new FileInputStream(namePath)) {
            text = IOUtils.toString(inputStream);
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
        return text;
    }

    public SendMessage finder(){
        String str = message_text;
        SendMessage message = new SendMessage();
        try(FileInputStream inputStream = new FileInputStream("src/main/resources/file_" + chat_id + ".txt"))
        {
            String text = IOUtils.toString(inputStream);
            int number = text.indexOf(str);
            StringBuilder fin = new StringBuilder();
            while (number < text.length()){
                char c = text.charAt(number);
                if (c != '%'){
                    fin.append(c);
                }
                else{
                    break;
                }
                number++;
            }
            message.setChatId(String.valueOf(chat_id));
            message.setText(String.valueOf(fin));
        }
        catch(IOException ex){

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
        return fileOpener("src/main/resources/token.txt");
    }
    private boolean mes(Update update){ //чтобы работали инлайн кнопки
        if (update.hasMessage() && update.getMessage().hasText()) {
            chat_id = update.getMessage().getChatId();
            message_text = update.getMessage().getText();
            return true;
        }
        else{
            return false;
        }
    }
    @Override
    public void onUpdateReceived(Update update) {
        System.out.println("Привет Поля");
        if (mes(update)) {//если сообщение
            if (message_text.equals("/start") && !startWait1 && !startWait2) {
                sendKeyboardMessage();
            }if (message_text.equals("Создать заметку") && !startWait1 && !startWait2) {
                startWait1 = true;
                sendText(chat_id, """
                        Пример вода записи:
                        Мой день был хорошим, я провел его с замечательными людьми!
                            
                        Напишите день, за который хотите внести запись. Далее расскажите нам о своем дне❤️""");
            } else if (message_text.equals("Поменять оформление") && !startWait1 && !startWait2) {
                try {
                    execute(variableOfTheme(chat_id));
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if (message_text.equals("Посмотреть заметку") && !startWait1 && !startWait2) {
                startWait2 = true;
                sendText(chat_id, """
                        проверка изменений
                        Для поиска нужной Вам записи напишите в бот дату, которая вам нужна в таком формате:
                            
                        19.09.19""");
            } else if (startWait1) {
                fileMaker();
                sendText(chat_id, "Текст успешно записан!");
                startWait1 = false;
            } else if (startWait2) {
                sendPhotoText(chat_id);
                sendText(chat_id, "Ваша запись!");
                startWait2 = false;
            }
        }
        else if (update.hasCallbackQuery()) {//если инлайн кнопка
            call_data = update.getCallbackQuery().getData();
            chat_id = update.getCallbackQuery().getMessage().getChatId();
            if (call_data.equals("БЕЛОЕ")){//дада белое сердце и зеленая тема
                sendImageWhite();
                sendText(chat_id, "Выбрана зеленая тема!");
            }
            else if (call_data.equals("СИНЕЕ")){
                sendImageBlue();
                sendText(chat_id, "Выбрана синяя тема!");
            }
            else if (call_data.equals("КРАСНОЕ")){
                sendImageRad();
                sendText(chat_id, "Выбрана красная тема!");
            }
        }
    }

    public void sendText(Long who, String what){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString()) //
                .text(what).build();    //Message content
        try {
            execute(sm);                        //мы не любим это, можно пробовать отправлять еще раз
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }

    public SendMessage variableOfTheme(Long who)
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
    public void sendKeyboardMessage() {
        SendMessage sm = new SendMessage();
        sm.setChatId(String.valueOf(chat_id));
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();

        KeyboardRow row1 =new KeyboardRow();
        KeyboardRow row2 =new KeyboardRow();
        KeyboardRow row3 =new KeyboardRow();

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
    private void sendImageWhite() {
        path = "src/main/resources/2sh.png";
    }
    private void sendImageBlue() {
        path = "src/main/resources/3sh.png";
    }
    private void sendImageRad() {
        path = "src/main/resources/1sh.png";
    }
    private File addTextToImage(String text, File originalImage) {
        try {
            BufferedImage image = ImageIO.read(originalImage);
            Graphics2D g2d = image.createGraphics();

            g2d.setFont(new Font("SansSerif", Font.BOLD, 40));
            FontMetrics fontMetrics = g2d.getFontMetrics();

            g2d.setColor(Color.black); //Цвет текста
            g2d.drawString(text, 70,40);

            g2d.dispose();

            File outputImage = new File("src/main/resources/output_" + originalImage.getName());
            ImageIO.write(image, "jpg", outputImage);

            return outputImage;
        } catch (IOException e) {
            throw new RuntimeException("Failed to process the image", e);
        }
    }
    private void sendPhotoText(Long who) {
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