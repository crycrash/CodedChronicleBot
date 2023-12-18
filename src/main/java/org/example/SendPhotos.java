package org.example;

import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SendPhotos {
    private static String path = "src/main/resources/newpic2.jpeg";
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
            java.util.List<String> lines = wrapText(text, maxWidth); // Разделение текста на строки

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
            }
            if (flag) {
                text = text.substring(801);
                java.util.List<String> lines2 = wrapText(text, maxWidth1); // Разделение текста на строки
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

    private static java.util.List<String> wrapText(String text, int maxLength) {
        List<String> lines = new ArrayList<>();
        int length = text.length();
        for (int i = 0; i < length; i += maxLength) {
            int endIndex = Math.min(i + maxLength, length);
            lines.add(text.substring(i, endIndex));
        }
        return lines;
    }

    static File createPhotoText(String str, String date) {
        File originalImage = new File(path);
        File processedImage = addTextToImage(str, originalImage);
        return addTextToImage1(date, processedImage);
    }

    static void savePhotoToFile(Update update, long chat_id, String day, String month, String year) {
        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            PhotoSize photo = update.getMessage().getPhoto().stream().min((ps1, ps2) -> Integer.compare(ps2.getFileSize(), ps1.getFileSize()))
                    .orElse(null);
            if (photo != null) {
                try {
                    String filePath = getFilePath(photo.getFileId());

                    String t = CodedChronicleBot.fileOpener("src/main/resources/token.txt");
                    String fileURL = "https://api.telegram.org/file/bot" + t + "/" + filePath;

                    // Открываем поток для чтения фото
                    InputStream inputStream = new URL(fileURL).openStream();
                    // Создаем файл для сохранения фото в указанной папке
                    String savePath = CodedChronicleBot.getPath();
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

    void sendOverlappingImage(File file1, String path2, long chat) {
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
            new CodedChronicleBot().photoExecute(chat, outputImage);

        } catch (IOException e) {
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
            new CodedChronicleBot().execute(sendPhoto);
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
