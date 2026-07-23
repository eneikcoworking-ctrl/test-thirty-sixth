package com.eneik.generated;

import com.eneik.generated.dto.LiveChatClientPayload;
import com.eneik.generated.dto.LiveChatServerPayload;
import com.eneik.generated.model.Dialog;
import com.eneik.generated.model.Message;
import com.eneik.generated.model.SenderType;
import com.eneik.generated.repository.DialogRepository;
import com.eneik.generated.repository.MessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LiveChatWebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DialogRepository dialogRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        jdbcTemplate.execute("DELETE FROM messages");
        jdbcTemplate.execute("DELETE FROM dialogs");
    }

    @AfterEach
    public void tearDown() {
        jdbcTemplate.execute("DELETE FROM messages");
        jdbcTemplate.execute("DELETE FROM dialogs");
    }

    @Test
    public void testManualReplyViaWebSocketBlocksAIAndBroadcasts() throws Exception {
        // 1. Prepare data
        Dialog dialog = new Dialog();
        dialog.setTelegramChatId(987654321L);
        dialog.setLeadUsername("test_user");
        dialog.setLeadPhone("+999888777");
        dialog.setIsAiActive(true);
        dialog = dialogRepository.save(dialog);

        Long dialogId = dialog.getId();

        // 2. Set up test WebSocket client
        StandardWebSocketClient client = new StandardWebSocketClient();
        BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

        TextWebSocketHandler clientHandler = new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                messageQueue.offer(message.getPayload());
            }
        };

        String wsUrl = "ws://localhost:" + port + "/ws/live-chat";
        WebSocketSession clientSession = client.execute(clientHandler, wsUrl).get(5, TimeUnit.SECONDS);

        try {
            // 3. Send manual reply command from operator
            LiveChatClientPayload replyPayload = new LiveChatClientPayload("reply", dialogId, "Hello from support!");
            String requestJson = objectMapper.writeValueAsString(replyPayload);
            clientSession.sendMessage(new TextMessage(requestJson));

            // 4. Await and verify the broadcast response
            String receivedPayload = messageQueue.poll(5, TimeUnit.SECONDS);
            assertNotNull(receivedPayload, "Did not receive broadcasted message in time");

            LiveChatServerPayload serverPayload = objectMapper.readValue(receivedPayload, LiveChatServerPayload.class);
            assertEquals("message", serverPayload.getEvent());
            assertEquals(dialogId, serverPayload.getDialogId());
            assertEquals("Hello from support!", serverPayload.getText());
            assertEquals("HUMAN", serverPayload.getSenderType());
            assertFalse(serverPayload.getIsAiActive(), "AI active should be broadcasted as false");

            // 5. Verify DB updates
            Dialog updatedDialog = dialogRepository.findById(dialogId).orElseThrow();
            assertFalse(updatedDialog.getIsAiActive(), "isAiActive must be false in DB");

            List<Message> messages = messageRepository.findAll();
            assertEquals(1, messages.size());
            Message savedMessage = messages.get(0);
            assertEquals("Hello from support!", savedMessage.getText());
            assertEquals(SenderType.HUMAN, savedMessage.getSenderType());

        } finally {
            if (clientSession.isOpen()) {
                clientSession.close();
            }
        }
    }
}
