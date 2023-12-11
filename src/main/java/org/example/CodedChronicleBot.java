package org.example;
import org.apache.commons.io.IOUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
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
    String path = "src/main/resources/newpic2.jpeg"; //дефолтная картинка
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
    public String getPath() {
        return fileOpener("src/main/resources/path.txt");
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
        InlineKeyboards key;
        key = new InlineKeyboards();
        if (mes(update)) {
            SQLite.makeTable();
            if (message_text.equals("/start") && session.getState().equals(BotState.START)) {
                ReplyKeyboard sen = new ReplyKeyboard();
                unsaveExecute(sen.sendKeyboard(chat_id));
                session.setState(BotState.NOTWAITING);
            }
            if (message_text.equals("Создать заметку") && session.getState().equals(BotState.NOTWAITING)) {
                List<String> names = Arrays.asList("Да", "Нет");
                List<String> codes = Arrays.asList("YES", "NO");
                unsaveExecute(key.sendKeyboard(chat_id, names, codes, "Хотите за сегодня?"));
            } else if (message_text.equals("Поменять оформление") && session.getState().equals(BotState.NOTWAITING)) {
                List<String> names = Arrays.asList("\uD83E\uDD0D", "❤", "\uD83D\uDC99");
                List<String> codes = Arrays.asList("БЕЛОЕ", "КРАСНОЕ", "СИНЕЕ");
                unsaveExecute(key.sendKeyboard(chat_id, names, codes, "Выберите цветовую гамму"));
            } else if (message_text.equals("Посмотреть заметку") && session.getState().equals(BotState.NOTWAITING)) {
                f = 0;
                List<String> years0;
                years0 = hhf.getYears(chat_id);
                if (!years0.isEmpty()){
                    unsaveExecute(key.sendKeyboard(chat_id, years0, years0, "Выберите год"));
                    session.setState(BotState.WAITING3);
                }else{
                    sendText(chat_id, "У вас еще нет записей!");
                    session.setState(BotState.NOTWAITING);
                }
            } else if (message_text.equals("Редактировать запись") && session.getState().equals(BotState.NOTWAITING)) {
                f = 1;
                List<String> years0;
                years0 = hhf.getYears(chat_id);
                unsaveExecute(key.sendKeyboard(chat_id, years0, years0, "Выберите год"));
                session.setState(BotState.WAITING3);
            }else if (message_text.equals("Удалить записи") && session.getState().equals(BotState.NOTWAITING)) {
                List<String> names = Arrays.asList("Да", "Нет");
                List<String> codes = Arrays.asList("900", "100");
                unsaveExecute(key.sendKeyboard(chat_id, names, codes, "Вы уверены?"));
                session.setState(BotState.WAITING2);
            } else if (session.getState().equals(BotState.WAITING1)) {
                if (message_text.length() > 1130){
                    sendText(chat_id, "Ваше сообщение слишком длинное! Ограничение по символам 1130. Попробуйте ввести снова...");
                }else{
                    hhf.makeNote(chat_id, message_text, year, month, day);
                    List<String> names = Arrays.asList("Да", "Нет");
                    List<String> codes = Arrays.asList("YESPHOTO", "NOPHOTO");
                    unsaveExecute(key.sendKeyboard(chat_id, names, codes, "Хотите добавить фото к заметке?"));
                    session.setState(BotState.NOTWAITING);
                }
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
                if (day.charAt(0) == '0') {
                    day=day.substring(1, 2);
                }
                if (month.charAt(0) == '0') {
                    month=month.substring(1, 2);
                }
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
                List<String> names = Arrays.asList("2023", "2022", "2021");
                unsaveExecute(key.sendKeyboard(chat_id, names, names, "Года"));
                session.setState(BotState.WAITING5);
            } else if (call_data.equals("NOPHOTO")) {
                System.out.println("8888");
                sendText(chat_id, "Текст успешно записан");
                session.setState(BotState.NOTWAITING);
            } else if (call_data.equals("YESPHOTO")) {
                sendText(chat_id, "отправьте фото");
            } else if (session.getState().equals(BotState.WAITING3)) {
                year = call_data;
                List<String> months0;
                months0 = hhf.getMonths(chat_id, year);
                unsaveExecute(key.sendKeyboard(chat_id, months0, months0, "Выберите месяц"));
                session.setState(BotState.WAITING4);
            } else if (session.getState().equals(BotState.WAITING4)) {
                month = call_data;
                ArrayList<Integer> days0;
                days0 = hhf.getDays(chat_id, year, month);
                unsaveExecute(key.sendPreparedDays(chat_id, days0, "Выберите день"));
                session.setState(BotState.WAITING_AFTER_DAY);
            } else if (session.getState().equals(BotState.WAITING_AFTER_DAY)) {
                day = call_data;
                if (f == 0) {
                    String s = hhf.getMessage(chat_id, year, month, day);
                    String date = day + "." + month + "." + year;
                    String name = chat_id + "." + year + month + day + ".jpg";
                    if(checkPhoto(getPath(),name)==1) {
                        System.out.println("не зашел");
                        String photo=getPath()+"/"+name;
                        sendOverlappingImage(createPhotoText(chat_id, s, date),photo);
                    }else{
                        System.out.println("зашел");
                        sendPhotoText(chat_id, s, date);
                    }
                }
                if (f == 1) {
                    List<String> names = Arrays.asList("Перезапись", "Добавление");
                    List<String> codes = Arrays.asList("rewrite", "add");
                    unsaveExecute(key.sendKeyboard(chat_id, names, codes, "Что хотите сделать с записью?"));
                }
                session.setState(BotState.NOTWAITING);
            } else if (message_text.equals("rewrite")) {
                sendText(chat_id, "отправьте новый текст");
                session.setState(BotState.REWAITING);
            } else if (session.getState().equals(BotState.WAITING5)) {
                year = call_data;
                unsaveExecute(key.sendConstantMonths(chat_id));
                session.setState(BotState.WAITING6);
            } else if (session.getState().equals(BotState.WAITING6)) {
                month = call_data;
                ArrayList<Integer> daysInMonth = hhf.makeNoteNo(chat_id, year, month);
                unsaveExecute(key.sendPreparedDays(chat_id, daysInMonth, "Выберите день"));
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
                    List<String> names = Arrays.asList("Да", "Нет");
                    List<String> codes = Arrays.asList("800", "200");
                    unsaveExecute(key.sendKeyboard(chat_id, names, codes, "Вы точно уверены?"));
                    session.setState(BotState.WAITING8);
                }if (call_data.equals("100")){
                    sendText(chat_id, "Ну и хорошо!");
                    session.setState(BotState.NOTWAITING);
                }
            }else if (session.getState().equals(BotState.WAITING8)) {
                if (call_data.equals("800")){
                    hhf.deleteAll(chat_id);
                    deletePhotos(chat_id, getPath());
                    sendText(chat_id, "Все ваши заметки удалены!");
                }if (call_data.equals("200")){
                    sendText(chat_id, "Ну и хорошо!");
                }
                session.setState(BotState.NOTWAITING);
            }

        } else if (update.getMessage().hasPhoto()) {
            if (day.charAt(0) == '0') {
                day=day.substring(1, 2);
            }
            if (month.charAt(0) == '0') {
                month=month.substring(1, 2);
            }
            savePhotoToFile(update,chat_id, day, month, year);
            sendText(chat_id, "заметка записана");
            session.setState(BotState.NOTWAITING);
        }
    }

    private void unsaveExecute(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
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

    private void sendImageWhite() {
        path = "src/main/resources/newpic2.jpeg";
    }

    private void sendImageBlue() {
        path = "src/main/resources/newpic3.jpeg";
    }

    private void sendImageRad() {
        path = "src/main/resources/newpic1.jpeg";
    }

    private File addTextToImage(String text, File originalImage) {
        try {
            BufferedImage image = ImageIO.read(originalImage);
            Graphics2D g2d = image.createGraphics();

            g2d.setFont(new Font("SansSerif", Font.BOLD, 30));
            g2d.setColor(Color.black); // Цвет текста

            int maxWidth = 40; // Максимальная длина строки
            float lineHeight = 31.999982f; // Высота текста
            int maxWidth1 = 22;
            List<String> lines = wrapText(text, maxWidth); // Разделение текста на строки

            int y = 149; // Начальная позиция по оси Y
            int count = 0;
            boolean flag = false;
            for (String line : lines) {
                if (count < 800) {
                    g2d.drawString(line, 15, y); // Отображение строки
                    y += lineHeight; // Увеличение позиции по оси Y для следующей строки
                }else{
                    flag = true;
                }
                count += line.length();
                System.out.println(count);
            }
            if (flag){
                text = text.substring(801);
                List<String> lines2 = wrapText(text, maxWidth1); // Разделение текста на строки
                for (String line : lines2) {
                    g2d.drawString(line, 15, y); // Отображение строки
                    y += lineHeight; // Увеличение позиции по оси Y для следующей строки
                }
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

    private void savePhotoToFile(Update update, long chat_id, String day, String month, String year) {
        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            PhotoSize photo = update.getMessage().getPhoto().stream()
                    .sorted((ps1, ps2) -> Integer.compare(ps2.getFileSize(), ps1.getFileSize()))
                    .findFirst()
                    .orElse(null);
            if (photo != null) {
                try {
                    String filePath = getFilePath(photo.getFileId());

                    String t = fileOpener("src/main/resources/token.txt");
                    String fileURL = "https://api.telegram.org/file/bot" + t + "/" + filePath;

                    // Открываем поток для чтения фото
                    InputStream inputStream = new URL(fileURL).openStream();
                    // Создаем файл для сохранения фото в указанной папке
                    String savePath = getPath();
                    String name = chat_id + "." + year + month + day; // Пример значения переменной name
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

                } catch (TelegramApiException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getFilePath(String fileId) throws TelegramApiException {
        GetFile getFileRequest = new GetFile(fileId);
        return execute(getFileRequest).getFilePath();
    }
    private void sendOverlappingImage(File file1, String path2) {
        try {
            BufferedImage image1 = ImageIO.read(file1);
            BufferedImage image2 = ImageIO.read(new File(path2));

            BufferedImage overlappingImage = new BufferedImage(image1.getWidth(), image1.getHeight(), BufferedImage.TYPE_INT_ARGB);

            int newWidth = 321;
            int newHeight = 345;

            BufferedImage resizedImage2 = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2dResized = resizedImage2.createGraphics();
            g2dResized.drawImage(image2, 0, 0, newWidth, newHeight, null);
            g2dResized.dispose();

            Graphics2D g2d = overlappingImage.createGraphics();
            g2d.drawImage(image1, 0, 0, null);
            g2d.drawImage(resizedImage2, 456, 767, null);
            g2d.dispose();

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
        }
    }
    public static int checkPhoto(String path, String photoName) {
        File folder = new File(path);
        File[] files = folder.listFiles();

        if (files == null) {
            return 0;
        }

        for (File file : files) {
            if (file.isFile() && file.getName().equals(photoName)) {
                return 1;
            }
        }
        return 0;
    }
    private void deletePhotos(long chat_id, String path){
        File folder = new File(path);
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isFile() && file.getName().startsWith(String.valueOf(chat_id))) {
                file.delete();
            }
        }
    }
}