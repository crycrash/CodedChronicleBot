package org.example;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InlineKeyboards {
    public static void main(String[] args) {
    }
    public SendMessage sendKeyboard(long id, List<String> names, List<String> data, String text){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(id));
        message.setText(text);
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> rowsInLine = new ArrayList<>();
        for(int i = 0;i < names.size();i++){
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(names.get(i));
            button.setCallbackData(data.get(i));
            rowsInLine.add(button);
        }
        markupInLine.setKeyboard(Collections.singletonList(rowsInLine));
        message.setReplyMarkup(markupInLine);

        return message;
    }
    public SendMessage sendPreparedDays(Long who, ArrayList<Integer> daysInMonth, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(who));
        message.setText(text);
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
                    rowInLine1.add(helper(s));
                } else if (count <= 12) {
                    rowInLine2.add(helper(s));
                } else if (count <= 18) {
                    rowInLine3.add(helper(s));
                } else if (count <= 24) {
                    rowInLine4.add(helper(s));
                } else {
                    rowInLine5.add(helper(s));
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
    public SendMessage sendConstantMonths(Long who) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(who));
        message.setText("Месяца");
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine1 = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            rowInLine1.add(helper(i));
        }
        List<InlineKeyboardButton> rowInLine2 = new ArrayList<>();
        for (int i = 7; i <= 12; i++) {
            rowInLine2.add(helper(i));
        }
        rowsInLine.add(rowInLine1);
        rowsInLine.add(rowInLine2);
        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);
        return message;
    }

    private InlineKeyboardButton helper(int s){
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(String.valueOf(s));
        button.setCallbackData(String.valueOf(s));
        return button;
    }
}
