package org.example;

import org.telegram.telegrambots.meta.api.objects.Update;

public class MessageExecute {
    static void processMessage(Update update, BotSession userSession, long chat_id) {
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
            StatesMetods.waiting1(userSession, chat_id, CodedChronicleBot.message_text, CodedChronicleBot.parametrs);
        } else if (userSession.getState().equals(BotState.WAITINGAFTERADD)) {
            StatesMetods.waitingafteradd(userSession, chat_id, CodedChronicleBot.message_text, CodedChronicleBot.parametrs);
        } else if (userSession.getState().equals(BotState.REWAITING)) {
            StatesMetods.rewaiting(userSession, chat_id, CodedChronicleBot.message_text, CodedChronicleBot.parametrs);
        }
    }
}
