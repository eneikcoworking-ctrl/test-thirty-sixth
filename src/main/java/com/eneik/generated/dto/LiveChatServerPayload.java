package com.eneik.generated.dto;

import java.time.OffsetDateTime;

public class LiveChatServerPayload {
    private String event;
    private Long messageId;
    private Long dialogId;
    private Long telegramChatId;
    private String text;
    private String senderType;
    private OffsetDateTime sentAt;
    private Boolean isAiActive;

    public LiveChatServerPayload() {}

    public LiveChatServerPayload(String event, Long messageId, Long dialogId, Long telegramChatId, String text, String senderType, OffsetDateTime sentAt, Boolean isAiActive) {
        this.event = event;
        this.messageId = messageId;
        this.dialogId = dialogId;
        this.telegramChatId = telegramChatId;
        this.text = text;
        this.senderType = senderType;
        this.sentAt = sentAt;
        this.isAiActive = isAiActive;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getDialogId() {
        return dialogId;
    }

    public void setDialogId(Long dialogId) {
        this.dialogId = dialogId;
    }

    public Long getTelegramChatId() {
        return telegramChatId;
    }

    public void setTelegramChatId(Long telegramChatId) {
        this.telegramChatId = telegramChatId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public OffsetDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(OffsetDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public Boolean getIsAiActive() {
        return isAiActive;
    }

    public void setIsAiActive(Boolean isAiActive) {
        this.isAiActive = isAiActive;
    }
}
