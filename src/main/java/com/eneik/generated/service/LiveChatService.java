package com.eneik.generated.service;

import com.eneik.generated.dto.LiveChatServerPayload;
import com.eneik.generated.event.MessageBroadcastEvent;
import com.eneik.generated.model.Dialog;
import com.eneik.generated.model.Message;
import com.eneik.generated.model.SenderType;
import com.eneik.generated.repository.DialogRepository;
import com.eneik.generated.repository.MessageRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LiveChatService {

    private final DialogRepository dialogRepository;
    private final MessageRepository messageRepository;
    private final ApplicationEventPublisher eventPublisher;

    public LiveChatService(DialogRepository dialogRepository,
                           MessageRepository messageRepository,
                           ApplicationEventPublisher eventPublisher) {
        this.dialogRepository = dialogRepository;
        this.messageRepository = messageRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Message saveMessage(Long dialogId, String text, SenderType senderType) {
        Dialog dialog = dialogRepository.findById(dialogId)
                .orElseThrow(() -> new IllegalArgumentException("Dialog not found with id: " + dialogId));

        Message message = new Message();
        message.setDialog(dialog);
        message.setText(text);
        message.setSenderType(senderType);

        Message savedMessage = messageRepository.save(message);

        // Publish broadcast event
        LiveChatServerPayload payload = new LiveChatServerPayload(
                "message",
                savedMessage.getId(),
                dialog.getId(),
                dialog.getTelegramChatId(),
                savedMessage.getText(),
                savedMessage.getSenderType().name(),
                savedMessage.getSentAt(),
                dialog.getIsAiActive()
        );
        eventPublisher.publishEvent(new MessageBroadcastEvent(payload));

        return savedMessage;
    }

    @Transactional
    public Message processManualReply(Long dialogId, String text) {
        Dialog dialog = dialogRepository.findById(dialogId)
                .orElseThrow(() -> new IllegalArgumentException("Dialog not found with id: " + dialogId));

        // Immediately block further AI generation
        dialog.setIsAiActive(false);
        dialogRepository.save(dialog);

        Message message = new Message();
        message.setDialog(dialog);
        message.setText(text);
        message.setSenderType(SenderType.HUMAN);

        Message savedMessage = messageRepository.save(message);

        // Publish broadcast event
        LiveChatServerPayload payload = new LiveChatServerPayload(
                "message",
                savedMessage.getId(),
                dialog.getId(),
                dialog.getTelegramChatId(),
                savedMessage.getText(),
                savedMessage.getSenderType().name(),
                savedMessage.getSentAt(),
                dialog.getIsAiActive()
        );
        eventPublisher.publishEvent(new MessageBroadcastEvent(payload));

        return savedMessage;
    }
}
