package com.eneik.generated.parser;

import com.eneik.generated.model.Lead;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SpintaxParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parses the spintax template and replaces placeholders with dynamic fields from the Lead entity.
     * Uses a default Random instance.
     */
    public String parse(String template, Lead lead) {
        return parse(template, lead, new Random());
    }

    /**
     * Parses the spintax template and replaces placeholders with dynamic fields from the Lead entity
     * using the provided Random instance.
     */
    public String parse(String template, Lead lead, Random random) {
        if (template == null) {
            return "";
        }
        if (lead == null) {
            return parseSpintax(template, random);
        }

        // 1. Replace placeholders: {{username}}, {{phone_number}}, and any custom metadata keys
        String text = replacePlaceholders(template, lead);

        // 2. Parse Spintax: {option1|option2|...}
        return parseSpintax(text, random);
    }

    private String replacePlaceholders(String template, Lead lead) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("username", lead.getUsername() != null ? lead.getUsername() : "");
        placeholders.put("phone_number", lead.getPhoneNumber() != null ? lead.getPhoneNumber() : "");

        // Parse lead metadata
        if (lead.getMetadata() != null && !lead.getMetadata().isBlank()) {
            try {
                Map<String, Object> metadataMap = objectMapper.readValue(
                    lead.getMetadata(),
                    new TypeReference<Map<String, Object>>() {}
                );
                for (Map.Entry<String, Object> entry : metadataMap.entrySet()) {
                    if (entry.getValue() != null) {
                        placeholders.put(entry.getKey(), entry.getValue().toString());
                    }
                }
            } catch (Exception e) {
                // Log/ignore parsing error to be robust
            }
        }

        String text = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return text;
    }

    public String parseSpintax(String template, Random random) {
        if (template == null) {
            return "";
        }
        String text = template;
        while (true) {
            int closeIdx = text.indexOf('}');
            if (closeIdx == -1) {
                break;
            }
            int openIdx = text.lastIndexOf('{', closeIdx);
            if (openIdx == -1) {
                // Unmatched closing brace, break to avoid infinite loop
                break;
            }

            String inside = text.substring(openIdx + 1, closeIdx);
            String[] options = inside.split("\\|", -1);
            String choice = options[random.nextInt(options.length)];

            text = text.substring(0, openIdx) + choice + text.substring(closeIdx + 1);
        }
        return text;
    }
}
