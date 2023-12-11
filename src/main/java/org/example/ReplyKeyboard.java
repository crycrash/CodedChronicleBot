package org.example;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReplyKeyboard {
    public static void main(String[] args) {
    }
    public SendMessage sendKeyboard(long chat_id){
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
        return sm;
    }
}
