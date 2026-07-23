package com.eneik.generated;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DatabaseMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testDatabaseMigrationSuccessful() {
        // Clear tables from any persistent run to ensure test independence
        assertDoesNotThrow(() -> {
            jdbcTemplate.execute("DELETE FROM messages");
            jdbcTemplate.execute("DELETE FROM dialogs");
        });

        // Test that tables were created successfully and are queryable
        assertDoesNotThrow(() -> {
            jdbcTemplate.execute("SELECT * FROM dialogs");
            jdbcTemplate.execute("SELECT * FROM messages");
        });

        // Insert dummy dialog
        jdbcTemplate.update(
            "INSERT INTO dialogs (telegram_chat_id, lead_username, lead_phone, is_ai_active) VALUES (?, ?, ?, ?)",
            123456789L, "john_doe", "+123456789", true
        );

        // Fetch dialog and verify attributes
        List<Map<String, Object>> dialogs = jdbcTemplate.queryForList("SELECT * FROM dialogs");
        assertEquals(1, dialogs.size());
        Map<String, Object> dialog = dialogs.get(0);
        assertEquals(123456789L, ((Number) dialog.get("telegram_chat_id")).longValue());
        assertEquals("john_doe", dialog.get("lead_username"));
        assertEquals("+123456789", dialog.get("lead_phone"));
        assertTrue((Boolean) dialog.get("is_ai_active"));

        Long dialogId = ((Number) dialog.get("id")).longValue();

        // Insert message with correct sender type
        jdbcTemplate.update(
            "INSERT INTO messages (dialog_id, text, sender_type) VALUES (?, ?, ?)",
            dialogId, "Hello from AI", "AI"
        );

        // Fetch message and verify attributes
        List<Map<String, Object>> messages = jdbcTemplate.queryForList("SELECT * FROM messages");
        assertEquals(1, messages.size());
        Map<String, Object> message = messages.get(0);
        assertEquals(dialogId, ((Number) message.get("dialog_id")).longValue());
        assertEquals("Hello from AI", message.get("text"));
        assertEquals("AI", message.get("sender_type"));

        // Verify sender_type check constraint fails with incorrect value
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(
                "INSERT INTO messages (dialog_id, text, sender_type) VALUES (?, ?, ?)",
                dialogId, "Invalid message", "INVALID_SENDER"
            );
        });
    }
}
