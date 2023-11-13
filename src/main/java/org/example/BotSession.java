package org.example;

public class BotSession {
    private BotState state = BotState.START;
    public BotState getState() {
        return state;
    }

    public void setState(BotState state) {
        this.state = state;
    }
}
