package com.eneik.generated.parser;

import com.eneik.generated.model.Lead;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class SpintaxParserTest {

    private final SpintaxParser parser = new SpintaxParser();

    @Test
    public void testBasicSpintaxParsing() {
        String template = "{Hi|Hello|Hey} there!";

        // Let's use a seeded Random to test deterministic choices
        Random r1 = new Random(12345);
        String result1 = parser.parseSpintax(template, r1);
        assertNotNull(result1);
        assertTrue(result1.equals("Hi there!") || result1.equals("Hello there!") || result1.equals("Hey there!"));

        Random r2 = new Random(42);
        String result2 = parser.parseSpintax(template, r2);
        assertNotNull(result2);
        assertTrue(result2.equals("Hi there!") || result2.equals("Hello there!") || result2.equals("Hey there!"));
    }

    @Test
    public void testNestedSpintaxParsing() {
        String template = "{Hi {there|friend}|Hello}!";

        boolean gotNestedFriend = false;
        boolean gotNestedThere = false;
        boolean gotHello = false;

        Random r = new Random(42);
        for (int i = 0; i < 100; i++) {
            String result = parser.parseSpintax(template, r);
            if (result.equals("Hi friend!")) {
                gotNestedFriend = true;
            } else if (result.equals("Hi there!")) {
                gotNestedThere = true;
            } else if (result.equals("Hello!")) {
                gotHello = true;
            }
        }

        assertTrue(gotNestedFriend, "Should generate 'Hi friend!'");
        assertTrue(gotNestedThere, "Should generate 'Hi there!'");
        assertTrue(gotHello, "Should generate 'Hello!'");
    }

    @Test
    public void testPlaceholderReplacementWithStandardFields() {
        Lead lead = new Lead();
        lead.setUsername("john_doe");
        lead.setPhoneNumber("+123456789");
        lead.setRoutingStatus("PENDING");

        String template = "Hello {{username}}, is your number {{phone_number}}?";
        String result = parser.parse(template, lead);
        assertEquals("Hello john_doe, is your number +123456789?", result);
    }

    @Test
    public void testPlaceholderReplacementWithMetadataFields() {
        Lead lead = new Lead();
        lead.setUsername("jane_doe");
        lead.setMetadata("{\"name\": \"Jane\", \"discount\": \"20%\", \"product\": \"SaaS App\"}");

        String template = "Hi {{name}}! Get {{discount}} off on our {{product}}.";
        String result = parser.parse(template, lead);
        assertEquals("Hi Jane! Get 20% off on our SaaS App.", result);
    }

    @Test
    public void testPlaceholderAndSpintaxCombined() {
        Lead lead = new Lead();
        lead.setUsername("alice_wonder");
        lead.setMetadata("{\"city\": \"London\"}");

        String template = "{Hey|Hello} {{username}} from {{city}}!";

        // Randomly resolving spintax
        Random r = new Random(100);
        String result = parser.parse(template, lead, r);
        assertTrue(result.equals("Hey alice_wonder from London!") || result.equals("Hello alice_wonder from London!"));
    }
}
