package com.eneik.generated.service;

import com.eneik.generated.exception.NoAvailableAccountsException;
import com.eneik.generated.exception.TelegramFloodWaitException;
import com.eneik.generated.model.Account;
import com.eneik.generated.model.Campaign;
import com.eneik.generated.model.Lead;
import com.eneik.generated.model.SpintaxConfig;
import com.eneik.generated.parser.SpintaxParser;
import com.eneik.generated.repository.AccountRepository;
import com.eneik.generated.repository.CampaignRepository;
import com.eneik.generated.repository.LeadRepository;
import com.eneik.generated.repository.SpintaxConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CampaignRoutingService {

    private final CampaignRepository campaignRepository;
    private final SpintaxConfigRepository spintaxConfigRepository;
    private final LeadRepository leadRepository;
    private final AccountRepository accountRepository;
    private final SpintaxParser spintaxParser;
    private final TelegramService telegramService;

    public CampaignRoutingService(
            CampaignRepository campaignRepository,
            SpintaxConfigRepository spintaxConfigRepository,
            LeadRepository leadRepository,
            AccountRepository accountRepository,
            SpintaxParser spintaxParser,
            TelegramService telegramService) {
        this.campaignRepository = campaignRepository;
        this.spintaxConfigRepository = spintaxConfigRepository;
        this.leadRepository = leadRepository;
        this.accountRepository = accountRepository;
        this.spintaxParser = spintaxParser;
        this.telegramService = telegramService;
    }

    /**
     * Dispatches outreach messages to all PENDING leads associated with an active campaign.
     * Manages automatic rotation of active aged accounts on FLOOD_WAIT and rate limit constraints.
     */
    @Transactional
    public void dispatchPendingLeads(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found: " + campaignId));

        if (!"ACTIVE".equalsIgnoreCase(campaign.getStatus())) {
            // Only process active campaigns
            return;
        }

        List<Lead> pendingLeads = leadRepository.findByCampaignIdAndRoutingStatus(campaignId, "PENDING");
        if (pendingLeads.isEmpty()) {
            return;
        }

        // Fetch all available active aged accounts
        List<Account> availableAccounts = new ArrayList<>(
                accountRepository.findByStatusAndIsAgedOrderByUsernameAsc("ACTIVE", true)
        );

        if (availableAccounts.isEmpty()) {
            throw new NoAvailableAccountsException("No available active aged accounts found.");
        }

        int accountIndex = 0;

        for (Lead lead : pendingLeads) {
            boolean messageSent = false;
            while (!messageSent) {
                if (availableAccounts.isEmpty() || accountIndex >= availableAccounts.size()) {
                    throw new NoAvailableAccountsException("No available active aged accounts remaining during dispatch.");
                }

                Account currentAccount = availableAccounts.get(accountIndex);

                // Enforce daily message limit (e.g. 15 messages)
                if (currentAccount.getDailyMsgCount() >= 15) {
                    currentAccount.setStatus("LIMIT_REACHED");
                    currentAccount.setUpdatedAt(LocalDateTime.now());
                    accountRepository.save(currentAccount);
                    availableAccounts.remove(accountIndex);
                    // Do not increment accountIndex since the element at current index has been removed,
                    // so the next element shifted to this index.
                    continue;
                }

                try {
                    // Fetch campaign SpintaxConfig
                    SpintaxConfig spintaxConfig = spintaxConfigRepository.findByCampaignId(campaignId)
                            .orElseThrow(() -> new IllegalStateException("No spintax configuration found for campaign: " + campaignId));

                    // Generate parsed message text
                    String text = spintaxParser.parse(spintaxConfig.getTemplateText(), lead);

                    // Determine recipient
                    String recipient = (lead.getUsername() != null && !lead.getUsername().isBlank())
                            ? lead.getUsername()
                            : lead.getPhoneNumber();

                    if (recipient == null || recipient.isBlank()) {
                        lead.setRoutingStatus("FAILED");
                        lead.setUpdatedAt(LocalDateTime.now());
                        leadRepository.save(lead);
                        break; // Skip this lead
                    }

                    // Dispatch via Telegram
                    telegramService.sendMessage(currentAccount.getUsername(), recipient, text);

                    // Success! Increment account count
                    currentAccount.setDailyMsgCount(currentAccount.getDailyMsgCount() + 1);
                    currentAccount.setUpdatedAt(LocalDateTime.now());
                    accountRepository.save(currentAccount);

                    // Update Lead Status
                    lead.setRoutingStatus("SENT");
                    lead.setUpdatedAt(LocalDateTime.now());
                    leadRepository.save(lead);

                    messageSent = true;

                } catch (TelegramFloodWaitException e) {
                    // Account encountered flood control. Mark as FLOOD_WAIT and rotate.
                    currentAccount.setStatus("FLOOD_WAIT");
                    currentAccount.setUpdatedAt(LocalDateTime.now());
                    accountRepository.save(currentAccount);

                    // Remove current account from the available pool
                    availableAccounts.remove(accountIndex);
                }
            }
        }
    }
}
