package com.eneik.generated.service;

import com.eneik.generated.exception.TelegramFloodWaitException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TelegramServiceImpl implements TelegramService {

    private final Set<String> floodWaitAccounts = Collections.synchronizedSet(new HashSet<>());
    private final List<String> sentMessagesLog = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void sendMessage(String senderUsername, String recipientIdentifier, String text) {
        if (floodWaitAccounts.contains(senderUsername)) {
            throw new TelegramFloodWaitException("FLOOD_WAIT error encountered for account: " + senderUsername);
        }
        sentMessagesLog.add(senderUsername + " -> " + recipientIdentifier + ": " + text);
    }

    public void addFloodWaitAccount(String username) {
        floodWaitAccounts.add(username);
    }

    public void removeFloodWaitAccount(String username) {
        floodWaitAccounts.remove(username);
    }

    public void clearFloodWaitAccounts() {
        floodWaitAccounts.clear();
    }

    public List<String> getSentMessagesLog() {
        return new ArrayList<>(sentMessagesLog);
    }

    public void clearLog() {
        sentMessagesLog.clear();
    }
}
