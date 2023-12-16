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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.awt.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;

import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class CodedChronicleBot extends TelegramLongPollingBot {
    private static CodedChronicleBot instance;


    // Метод для получения единственного экземпляра класса
    public static synchronized CodedChronicleBot getInstance() {
        if (instance == null) {
            instance = new CodedChronicleBot();
        }
        return instance;
    }
    private Connection hff;

    public CodedChronicleBot() {
        try {
            this.hff = SQLite.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    long chat_id;
    static BotSession session = new BotSession();
    String message_text;
    String call_data;
    static String path = "src/main/resources/newpic2.jpeg"; //дефолтная картинка
    String date;

    String year;
    String month;
    String day;
    static int f = 1;
    private Map<Long, BotSession> userSessions = new HashMap<>();
    private InlineKeyboards key = new InlineKeyboards();
    private SQLite hhf;
    Parametrs parametrs=new Parametrs();

    public static void main(String[] args) throws TelegramApiException {
        SQLite.makeTable();
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(new CodedChronicleBot());
    }

    private static String fileOpener(String pa) {
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

    private boolean mes(Update update) { //чтобы работали teкнопки
        if (update.hasMessage() && update.getMessage().hasText()) {
            chat_id = update.getMessage().getChatId();
            message_text = update.getMessage().getText();
            return true;
        } else {
            return false;
        }
    }

    private boolean button(Update update) { //чтобы работали teкнопки
        if (update.hasCallbackQuery()) {
            chat_id = update.getCallbackQuery().getMessage().getChatId();
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
        if (button(update)) {
            String buttonData = update.getCallbackQuery().getData();
            BotSession userSession = userSessions.computeIfAbsent(chat_id, k -> new BotSession());
            processButtonClick(buttonData, userSession, chat_id, hhf, parametrs, message_text);
        } else if (mes(update)) {
            SQLite.makeTable();
            BotSession userSession = userSessions.computeIfAbsent(chat_id, k -> new BotSession());
            processMessage(update, userSession, chat_id);
        } else if (update.getMessage().hasPhoto()) {
            BotSession userSession = userSessions.computeIfAbsent(chat_id, k -> new BotSession());
            MainKeyboard.photo(userSession, chat_id, parametrs, update);
        }
    }


    private void processMessage(Update update, BotSession userSession, long chat_id) {
        String messageText = update.getMessage().getText();
        if ("/start".equals(messageText)) {
            MainKeyboard.handleStartCommand(userSession, chat_id);
        } else if ("Создать заметку".equals(messageText)) {
            MainKeyboard.handleCreateNoteCommand(userSession, chat_id);
        } else if ("Поменять оформление".equals(messageText)) {
            MainKeyboard.handleChangeDesignCommand(userSession, chat_id);
        } else if ("Посмотреть заметку".equals(messageText)) {
            MainKeyboard.handleViewNoteCommand(userSession, chat_id);
        } else if ("Редактировать запись".equals(messageText)) {
            MainKeyboard.handleEditNoteCommand(userSession, chat_id);
        } else if ("Удалить записи".equals(messageText)) {
            MainKeyboard.handleDeleteNotesCommand(userSession, chat_id);
        } else if (userSession.getState().equals(BotState.WAITING1)) {
            StatesMetods.waiting1(userSession, chat_id, message_text, parametrs);
        } else if (userSession.getState().equals(BotState.WAITINGAFTERADD)) {
            StatesMetods.waitingafteradd(userSession, chat_id, message_text, parametrs);
        } else if (userSession.getState().equals(BotState.REWAITING)) {
            StatesMetods.rewaiting(userSession, chat_id, message_text,parametrs);
        }
    }

    static void processButtonClick(String buttonData, BotSession userSession, long chat_id, SQLite hhf, Parametrs parametrs, String message_text) {
        if ("YES".equals(buttonData)) {
            //InlineKeyboardMetods.noteForToday(userSession, chat_id, year, month, day, message_text);
        } else if ("NO".equals(buttonData)) {
            InlineKeyboardMetods.notTodayNote(userSession, chat_id);
        } else if ("NO1".equals(buttonData)) {
            InlineKeyboardMetods.justNo(userSession, chat_id);
        } else if ("NOPHOTO".equals(buttonData)) {
            InlineKeyboardMetods.noPhoto(userSession, chat_id);
        } else if ("YESPHOTO".equals(buttonData)) {
            InlineKeyboardMetods.yesPhoto(userSession, chat_id);
        } else if ("NOPHOTO".equals(buttonData)) {
            InlineKeyboardMetods.noPhoto(userSession, chat_id);
        } else if ("БЕЛОЕ".equals(buttonData)) {
            InlineKeyboardMetods.greenHeart(userSession, chat_id);
        } else if ("СИНЕЕ".equals(buttonData)) {
            InlineKeyboardMetods.blueHeart(userSession, chat_id);
        } else if ("КРАСНОЕ".equals(buttonData)) {
            InlineKeyboardMetods.redHeart(userSession, chat_id);
        } else if ("rewrite".equals(buttonData)) {
            InlineKeyboardMetods.rewrite(userSession, chat_id);
        } else if ("add".equals(buttonData)) {
            InlineKeyboardMetods.add(userSession, chat_id);

        } else if (userSession.getState().equals(BotState.WAITING3)) {
            parametrs.year=StatesMetods.waitingYEAR(userSession, chat_id, message_text, parametrs, buttonData, f);
        } else if (userSession.getState().equals(BotState.WAITING4)) {
            StatesMetods.waitingMONTH(userSession, chat_id, message_text, parametrs, buttonData, f, getInstance().year);
        } else if (userSession.getState().equals(BotState.WAITING_AFTER_DAY)) {
            StatesMetods.waitingDAY(userSession, chat_id, message_text, parametrs, buttonData, f);
        } else if (userSession.getState().equals(BotState.WAITING5)) {
            parametrs.year=StatesMetods.waitingToSendYear(userSession, chat_id, message_text, parametrs, buttonData);
        } else if (userSession.getState().equals(BotState.WAITING6)) {
            parametrs.month=StatesMetods.waitingToSendMonth(userSession, chat_id, message_text, parametrs, buttonData,getInstance().year);
        } else if (userSession.getState().equals(BotState.WAITING7)) {
            StatesMetods.waitingToSendDay(userSession, chat_id, message_text, parametrs, buttonData,getInstance().year,getInstance().month);
        } else if (userSession.getState().equals(BotState.WAITING2)) {
            StatesMetods.delite1(userSession, chat_id, message_text, parametrs, buttonData);
        } else if (userSession.getState().equals(BotState.WAITING8)) {
            StatesMetods.delite2(userSession, chat_id, message_text, parametrs, buttonData);
        }
    }

    void unsaveExecute(SendMessage sendMessage) {
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

    void sendImageWhite() {
        path = "src/main/resources/newpic2.jpeg";
    }

    void sendImageBlue() {
        path = "src/main/resources/newpic3.jpeg";
    }

    void sendImageRad() {
        path = "src/main/resources/newpic1.jpeg";
    }

    private static File addTextToImage(String text, File originalImage) {
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
                } else {
                    flag = true;
                }
                count += line.length();
                System.out.println(count);
            }
            if (flag) {
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

    private static File addTextToImage1(String text, File originalImage) {
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

    private static List<String> wrapText(String text, int maxLength) {
        List<String> lines = new ArrayList<>();
        int length = text.length();
        for (int i = 0; i < length; i += maxLength) {
            int endIndex = Math.min(i + maxLength, length);
            lines.add(text.substring(i, endIndex));
        }
        return lines;
    }

    static File createPhotoText(Long who, String str, String date) {
        File originalImage = new File(path);
        File processedImage = addTextToImage(str, originalImage);
        File processedImage1 = addTextToImage1(date, processedImage);
        return processedImage1;
    }

    static void savePhotoToFile(Update update, long chat_id, String day, String month, String year) {
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


    private static String getFilePath(String fileId) throws TelegramApiException {
        CodedChronicleBot instance = new CodedChronicleBot();  // Replace YourClass with the actual class name
        GetFile getFileRequest = new GetFile(fileId);
        return instance.execute(getFileRequest).getFilePath();
    }

    void sendOverlappingImage(File file1, String path2) {
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

    void sendPhotoText(Long who, String str, String date) {
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

    static void deletePhotos(long chat_id, String path) {
        File folder = new File(path);
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isFile() && file.getName().startsWith(String.valueOf(chat_id))) {
                file.delete();
            }
        }
    }
}