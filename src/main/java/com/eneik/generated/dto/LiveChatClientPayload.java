package com.eneik.generated.dto;

public class LiveChatClientPayload {
    private String command;
    private Long dialogId;
    private String text;

    public LiveChatClientPayload() {}

    public LiveChatClientPayload(String command, Long dialogId, String text) {
        this.command = command;
        this.dialogId = dialogId;
        this.text = text;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Long getDialogId() {
        return dialogId;
    }

    public void setDialogId(Long dialogId) {
        this.dialogId = dialogId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
