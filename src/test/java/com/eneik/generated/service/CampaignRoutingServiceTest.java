package com.eneik.generated.service;

import com.eneik.generated.exception.NoAvailableAccountsException;
import com.eneik.generated.model.Account;
import com.eneik.generated.model.Campaign;
import com.eneik.generated.model.Lead;
import com.eneik.generated.model.SpintaxConfig;
import com.eneik.generated.repository.AccountRepository;
import com.eneik.generated.repository.CampaignRepository;
import com.eneik.generated.repository.LeadRepository;
import com.eneik.generated.repository.SpintaxConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class CampaignRoutingServiceTest {

    @Autowired
    private CampaignRoutingService campaignRoutingService;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private SpintaxConfigRepository spintaxConfigRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TelegramService telegramService;

    private TelegramServiceImpl mockTelegramService;

    private Campaign campaign;
    private SpintaxConfig spintaxConfig;
    private Account account1;
    private Account account2;

    @BeforeEach
    public void setUp() {
        // Cast injected service to access test-only methods
        mockTelegramService = (TelegramServiceImpl) telegramService;
        mockTelegramService.clearLog();
        mockTelegramService.clearFloodWaitAccounts();

        // Clear tables
        leadRepository.deleteAll();
        spintaxConfigRepository.deleteAll();
        accountRepository.deleteAll();
        campaignRepository.deleteAll();

        // Create campaign
        campaign = new Campaign();
        campaign.setName("Autumn Campaign");
        campaign.setStatus("ACTIVE");
        campaign = campaignRepository.save(campaign);

        // Create Spintax configuration
        spintaxConfig = new SpintaxConfig();
        spintaxConfig.setCampaign(campaign);
        spintaxConfig.setTemplateText("{Hi|Hello} {{username}}!");
        spintaxConfig = spintaxConfigRepository.save(spintaxConfig);

        // Create accounts
        account1 = new Account();
        account1.setUsername("acc_alpha");
        account1.setStatus("ACTIVE");
        account1.setAged(true);
        account1.setDailyMsgCount(0);
        account1 = accountRepository.save(account1);

        account2 = new Account();
        account2.setUsername("acc_beta");
        account2.setStatus("ACTIVE");
        account2.setAged(true);
        account2.setDailyMsgCount(0);
        account2 = accountRepository.save(account2);
    }

    @Test
    public void testSuccessfulDispatchWithDynamicFields() {
        // Create pending leads
        Lead lead1 = new Lead();
        lead1.setCampaign(campaign);
        lead1.setUsername("alice_wonder");
        lead1.setRoutingStatus("PENDING");
        lead1 = leadRepository.save(lead1);

        Lead lead2 = new Lead();
        lead2.setCampaign(campaign);
        lead2.setUsername("bob_builder");
        lead2.setRoutingStatus("PENDING");
        lead2 = leadRepository.save(lead2);

        // Execute dispatch
        campaignRoutingService.dispatchPendingLeads(campaign.getId());

        // Verify leads updated to SENT
        Lead updatedLead1 = leadRepository.findById(lead1.getId()).orElseThrow();
        Lead updatedLead2 = leadRepository.findById(lead2.getId()).orElseThrow();
        assertEquals("SENT", updatedLead1.getRoutingStatus());
        assertEquals("SENT", updatedLead2.getRoutingStatus());

        // Verify account dispatch count
        Account updatedAcc1 = accountRepository.findById(account1.getId()).orElseThrow();
        assertEquals(2, updatedAcc1.getDailyMsgCount());

        // Verify messages in mock log
        List<String> log = mockTelegramService.getSentMessagesLog();
        assertEquals(2, log.size());
        assertTrue(log.get(0).contains("acc_alpha -> alice_wonder:"));
        assertTrue(log.get(1).contains("acc_alpha -> bob_builder:"));
    }

    @Test
    public void testAutomaticRotationOnFloodWait() {
        // Set up flood wait simulation on the first account
        mockTelegramService.addFloodWaitAccount("acc_alpha");

        // Create pending leads
        Lead lead1 = new Lead();
        lead1.setCampaign(campaign);
        lead1.setUsername("alice_wonder");
        lead1.setRoutingStatus("PENDING");
        lead1 = leadRepository.save(lead1);

        Lead lead2 = new Lead();
        lead2.setCampaign(campaign);
        lead2.setUsername("bob_builder");
        lead2.setRoutingStatus("PENDING");
        lead2 = leadRepository.save(lead2);

        // Execute dispatch
        campaignRoutingService.dispatchPendingLeads(campaign.getId());

        // Verify account1 status updated to FLOOD_WAIT in DB
        Account updatedAcc1 = accountRepository.findById(account1.getId()).orElseThrow();
        assertEquals("FLOOD_WAIT", updatedAcc1.getStatus());

        // Verify account2 processed both leads successfully
        Account updatedAcc2 = accountRepository.findById(account2.getId()).orElseThrow();
        assertEquals("ACTIVE", updatedAcc2.getStatus());
        assertEquals(2, updatedAcc2.getDailyMsgCount());

        // Verify messages in mock log were dispatched from acc_beta
        List<String> log = mockTelegramService.getSentMessagesLog();
        assertEquals(2, log.size());
        assertTrue(log.get(0).startsWith("acc_beta -> alice_wonder:"));
        assertTrue(log.get(1).startsWith("acc_beta -> bob_builder:"));

        // Verify lead status
        assertEquals("SENT", leadRepository.findById(lead1.getId()).orElseThrow().getRoutingStatus());
        assertEquals("SENT", leadRepository.findById(lead2.getId()).orElseThrow().getRoutingStatus());
    }

    @Test
    public void testAutomaticRotationOnDailyLimitReached() {
        // Set account1 close to limit (14 messages sent)
        account1.setDailyMsgCount(14);
        account1 = accountRepository.save(account1);

        // Create 3 pending leads
        Lead lead1 = new Lead();
        lead1.setCampaign(campaign);
        lead1.setUsername("lead_1");
        lead1.setRoutingStatus("PENDING");
        lead1 = leadRepository.save(lead1);

        Lead lead2 = new Lead();
        lead2.setCampaign(campaign);
        lead2.setUsername("lead_2");
        lead2.setRoutingStatus("PENDING");
        lead2 = leadRepository.save(lead2);

        Lead lead3 = new Lead();
        lead3.setCampaign(campaign);
        lead3.setUsername("lead_3");
        lead3.setRoutingStatus("PENDING");
        lead3 = leadRepository.save(lead3);

        // Execute dispatch
        campaignRoutingService.dispatchPendingLeads(campaign.getId());

        // First message goes to account1, bringing it to 15 (exhausted)
        Account updatedAcc1 = accountRepository.findById(account1.getId()).orElseThrow();
        assertEquals(15, updatedAcc1.getDailyMsgCount());
        assertEquals("LIMIT_REACHED", updatedAcc1.getStatus());

        // The remaining 2 messages rotate to account2
        Account updatedAcc2 = accountRepository.findById(account2.getId()).orElseThrow();
        assertEquals(2, updatedAcc2.getDailyMsgCount());
        assertEquals("ACTIVE", updatedAcc2.getStatus());

        // Check logs
        List<String> log = mockTelegramService.getSentMessagesLog();
        assertEquals(3, log.size());
        assertTrue(log.get(0).startsWith("acc_alpha -> lead_1:"));
        assertTrue(log.get(1).startsWith("acc_beta -> lead_2:"));
        assertTrue(log.get(2).startsWith("acc_beta -> lead_3:"));
    }

    @Test
    public void testThrowsNoAvailableAccountsExceptionWhenAllExhausted() {
        // Set both accounts to limit
        account1.setDailyMsgCount(15);
        accountRepository.save(account1);
        account2.setDailyMsgCount(15);
        accountRepository.save(account2);

        Lead lead = new Lead();
        lead.setCampaign(campaign);
        lead.setUsername("lead_xyz");
        lead.setRoutingStatus("PENDING");
        leadRepository.save(lead);

        // Execute dispatch and expect exception
        assertThrows(NoAvailableAccountsException.class, () -> {
            campaignRoutingService.dispatchPendingLeads(campaign.getId());
        });
    }
}
