package org.example;

public class ButtonClick {
    static void processButtonClick(String buttonData, BotSession userSession, long chat_id) {
        if ("YES".equals(buttonData)) {
            InlineKeyboardMetods.noteForToday(userSession, chat_id, CodedChronicleBot.parametrs);
        } else if ("NO".equals(buttonData)) {
            InlineKeyboardMetods.notTodayNote(userSession, chat_id);
        } else if ("NO1".equals(buttonData)) {
            InlineKeyboardMetods.justNo(userSession, chat_id);
        } else if ("NOPHOTO".equals(buttonData)) {
            InlineKeyboardMetods.noPhoto(userSession, chat_id);
        } else if ("YESPHOTO".equals(buttonData)) {
            InlineKeyboardMetods.yesPhoto(userSession, chat_id);
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
            CodedChronicleBot.parametrs.year=StatesMetods.waitingYEAR(userSession, chat_id, CodedChronicleBot.parametrs, buttonData);
        } else if (userSession.getState().equals(BotState.WAITING4)) {
            StatesMetods.waitingMONTH(userSession, chat_id, CodedChronicleBot.parametrs, buttonData);
        } else if (userSession.getState().equals(BotState.WAITING_AFTER_DAY)) {
            StatesMetods.waitingDAY(userSession, chat_id, CodedChronicleBot.parametrs, buttonData, CodedChronicleBot.f);
        } else if (userSession.getState().equals(BotState.WAITING5)) {
            CodedChronicleBot.parametrs.year=StatesMetods.waitingToSendYear(userSession, chat_id, CodedChronicleBot.parametrs, buttonData);
        } else if (userSession.getState().equals(BotState.WAITING6)) {
            CodedChronicleBot.parametrs.month=StatesMetods.waitingToSendMonth(userSession, chat_id, CodedChronicleBot.parametrs, buttonData);
        } else if (userSession.getState().equals(BotState.WAITING7)) {
            StatesMetods.waitingToSendDay(userSession, chat_id, CodedChronicleBot.parametrs, buttonData);
        } else if (userSession.getState().equals(BotState.WAITING2)) {
            StatesMetods.delite1(userSession, chat_id, buttonData);
        } else if (userSession.getState().equals(BotState.WAITING8)) {
            StatesMetods.delite2(userSession, chat_id, buttonData);
        }
    }
}
