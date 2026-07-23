package com.eneik.generated;

import com.eneik.generated.model.Campaign;
import com.eneik.generated.model.Lead;
import com.eneik.generated.model.SpintaxConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class DatabaseSchemaTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    public void testPersistAndRetrieveCampaignWithSpintaxAndLeads() {
        // Create Campaign
        Campaign campaign = new Campaign();
        campaign.setName("Summer Promo");
        campaign.setStatus("ACTIVE");
        entityManager.persist(campaign);
        entityManager.flush();

        assertNotNull(campaign.getId());

        // Create SpintaxConfig
        SpintaxConfig config = new SpintaxConfig();
        config.setCampaign(campaign);
        config.setTemplateText("{Hi|Hello} check our promo!");
        entityManager.persist(config);
        entityManager.flush();

        assertNotNull(config.getId());

        // Create Lead
        Lead lead = new Lead();
        lead.setCampaign(campaign);
        lead.setUsername("john_doe");
        lead.setPhoneNumber("+123456789");
        lead.setRoutingStatus("PENDING");
        lead.setMetadata("{\"source\": \"csv\"}");
        entityManager.persist(lead);
        entityManager.flush();

        assertNotNull(lead.getId());

        // Clear persistence context to force retrieval from DB
        entityManager.clear();

        // Retrieve Campaign and verify
        Campaign retrievedCampaign = entityManager.find(Campaign.class, campaign.getId());
        assertNotNull(retrievedCampaign);
        assertEquals("Summer Promo", retrievedCampaign.getName());
        assertEquals("ACTIVE", retrievedCampaign.getStatus());
        assertNotNull(retrievedCampaign.getCreatedAt());
        assertNotNull(retrievedCampaign.getUpdatedAt());

        // Retrieve SpintaxConfig and verify
        SpintaxConfig retrievedConfig = entityManager.find(SpintaxConfig.class, config.getId());
        assertNotNull(retrievedConfig);
        assertEquals(retrievedCampaign.getId(), retrievedConfig.getCampaign().getId());
        assertEquals("{Hi|Hello} check our promo!", retrievedConfig.getTemplateText());
        assertNotNull(retrievedConfig.getCreatedAt());

        // Retrieve Lead and verify
        Lead retrievedLead = entityManager.find(Lead.class, lead.getId());
        assertNotNull(retrievedLead);
        assertEquals(retrievedCampaign.getId(), retrievedLead.getCampaign().getId());
        assertEquals("john_doe", retrievedLead.getUsername());
        assertEquals("+123456789", retrievedLead.getPhoneNumber());
        assertEquals("PENDING", retrievedLead.getRoutingStatus());
        assertEquals("{\"source\": \"csv\"}", retrievedLead.getMetadata());
        assertNotNull(retrievedLead.getCreatedAt());
        assertNotNull(retrievedLead.getUpdatedAt());
    }
}
