package com.eneik.generated.event;

import com.eneik.generated.dto.LiveChatServerPayload;

public class MessageBroadcastEvent {
    private final LiveChatServerPayload payload;

    public MessageBroadcastEvent(LiveChatServerPayload payload) {
        this.payload = payload;
    }

    public LiveChatServerPayload getPayload() {
        return payload;
    }
}
