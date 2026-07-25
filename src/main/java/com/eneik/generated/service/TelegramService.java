package com.eneik.generated.service;

public interface TelegramService {
    /**
     * Simulates sending a Telegram message from a given account to a target identifier (username or phone).
     * Throws TelegramFloodWaitException if flood wait is simulated.
     */
    void sendMessage(String senderUsername, String recipientIdentifier, String text);
}
