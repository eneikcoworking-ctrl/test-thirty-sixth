<script>
  import { onMount, tick } from 'svelte';

  // State Management
  let dialogs = [
    {
      id: 1,
      telegram_chat_id: 10001,
      lead_username: "Sarah Henderson",
      lead_phone: "+1 (555) 234-5678",
      is_ai_active: true,
      avatar: "https://lh3.googleusercontent.com/aida-public/AB6AXuCHK-YpPtYZWq_zN-gLJ21F-rcnVGRuQ1RH1maGDSPWew1D7B3d180qBycd75p4WupHGCG3At19y6g-aaWYQA3pS4K-qat_buWrBvsLsE64KR6_oBE3O6MbdTf65-tc9Pgw_bOEDgL91at0Tx6DG73Dv4BtdF7hDsxaPEsrEZy9dJtomPot0GcobP6sUiuOfFLW9mkZEiaYgJOsENVyKcjZt9XgsQ8u44McXODsnUz4MF_0v63sSY8yGh9JayQIssq6S9ln_iO_M-62",
      status: "New",
      unread: true,
      updated_at: "14:02",
      messages: [
        { id: 101, text: "Hello! I am looking to automate my Telegram outreach.", sender_type: "LEAD", sent_at: "14:00" },
        { id: 102, text: "I can help with that. Let know your target audience.", sender_type: "AI", sent_at: "14:01" },
        { id: 103, text: "I'm still waiting on the verification email for my account...", sender_type: "LEAD", sent_at: "14:02" }
      ]
    },
    {
      id: 2,
      telegram_chat_id: 10002,
      lead_username: "James Miller",
      lead_phone: "+1 (555) 876-5432",
      is_ai_active: false,
      avatar: "https://lh3.googleusercontent.com/aida-public/AB6AXuCmOj98skGh9A9UJkmysRv-htg1OZdCAyTCbS2F_-idS-8a5F1FAf1cpsPk0O9Df8KIWJJ8l5jWiQ-QDDfLxjduMb_o1n6YaL5Euhp8gZIEt-09QYHiZfiHE_wHttuNLb8vbdPzBGcbyzUOP2uiD8RYHhYVu7auUP1jhDG3-bApItkOGY2W_MsxLggNKZiT-z9ZcczGJ79i5e3JkGN2YD6AZn6lQAEfiWURo23e4CebtFOqOULXoDwhgwkX_NiE7d1lm3t0rT-vTV_0",
      status: "Active",
      unread: false,
      updated_at: "13:58",
      messages: [
        { id: 201, text: "Hi, has my proxy been assigned successfully?", sender_type: "LEAD", sent_at: "13:50" },
        { id: 202, text: "Yes, we have assigned a dedicated SOCKS5 proxy to your session.", sender_type: "HUMAN", sent_at: "13:55" },
        { id: 203, text: "Thanks for the update. I'll check my dashboard now.", sender_type: "LEAD", sent_at: "13:58" }
      ]
    },
    {
      id: 3,
      telegram_chat_id: 10003,
      lead_username: "Alex Rivera",
      lead_phone: "+1 (555) 345-6789",
      is_ai_active: true,
      avatar: "https://lh3.googleusercontent.com/aida-public/AB6AXuCE6UINZvAt_tE4qvK989-xYO4iulmnj7o3UAqbqEjr0srDKkep2NHr8IkhbttuFmgJSSubmQcSla1xjzKjjVc6C53ORS9fpzq8G8qzNaQHTTOwMWMpZ7EEba6hYFgtbuycF8ay36DH8hIWt08E4sh4VUvNZhMxZfjb8LNezdvEGXt3qSktEB2L05wWlBFRAa2TF8vA7mM44A8m9wWpqZM_6sJB0bS974A0YW6wylnuUDPN86pr60KbIhtK9eotenihyQXhtwznzq9J",
      status: "Waiting",
      unread: false,
      updated_at: "12:45",
      messages: [
        { id: 301, text: "Hi! Can you tell me more about pricing options?", sender_type: "LEAD", sent_at: "12:40" },
        { id: 302, text: "We have starter and enterprise plans. I'll have an agent contact you.", sender_type: "AI", sent_at: "12:43" },
        { id: 303, text: "Can we schedule a call for later today to discuss pricing?", sender_type: "LEAD", sent_at: "12:45" }
      ]
    },
    {
      id: 4,
      telegram_chat_id: 10004,
      lead_username: "Elena Petrova",
      lead_phone: "+7 (999) 111-22-33",
      is_ai_active: false,
      avatar: "https://lh3.googleusercontent.com/aida-public/AB6AXuBkzPR0iQdpGZf8l6L_OrpRp0oOSDEg2ZZHeIxNz_aNYDDSSEV7CJaKnHsfaY65qNzA7SKY7nvbF7qXrjoAiSrTgm-YKP47ik2smIHpYc2_PC4SgVgtdEW661IxqILkTFg4DYowwP6g8WNY1J0UQHtopt8W42I_qoNE5rgzNr9vlmY-5srAjH67V0hSsa0_iY29TxPmrx0QkZ1guQDosHnw8c-hK3bJzP-Lc6A-GMpEXmdpSu46_WlDDRGCCv16kKdYFwICywnpOo_J",
      status: "Active",
      unread: false,
      updated_at: "11:30",
      messages: [
        { id: 401, text: "There is an issue with the layout in my onboarding page.", sender_type: "LEAD", sent_at: "11:20" },
        { id: 402, text: "Let me check that. Could you provide a screenshot?", sender_type: "HUMAN", sent_at: "11:25" },
        { id: 403, text: "The invoice #4521 seems to have a discrepancy.", sender_type: "LEAD", sent_at: "11:30" }
      ]
    },
    {
      id: 5,
      telegram_chat_id: 10005,
      lead_username: "Michael Chen",
      lead_phone: "+1 (555) 456-7890",
      is_ai_active: true,
      avatar: "https://lh3.googleusercontent.com/aida-public/AB6AXuB4wCPZj0YeUYqgPcrNWLwxhC4-xtP7rhRKoqFPx-3pxQVUsa_TOvdFaW_rPyS5BwGhvsFi0v7Il0GAeCpAiY2DCntHbwUzy5geSC_u5luw6tJ9Kr2Px3I_wTAlESqZaWJbY8tVNYLCL0Htcc_XHZXvsQ7gMty7O5i54I7YeYwJ_OcbqnpetxCjRsudUCjV_Nm9V4yGq9Zvakv_l4mE6KVYlu6ADQ5aIOtY1e2E9D9KBwHQ-QDOpqm7KwSkzEBMpFjya5mjWubeUcI6",
      status: "New",
      unread: true,
      updated_at: "09:12",
      messages: [
        { id: 501, text: "Hi, I am interested in testing your campaign scheduler.", sender_type: "LEAD", sent_at: "09:10" },
        { id: 502, text: "Are you available for a quick technical sync?", sender_type: "LEAD", sent_at: "09:12" }
      ]
    },
    {
      id: 6,
      telegram_chat_id: 10006,
      lead_username: "Sophia Lane",
      lead_phone: "+44 20 7946 0958",
      is_ai_active: false,
      avatar: "https://lh3.googleusercontent.com/aida-public/AB6AXuCq5x7KIvOrm0LXHTsOoA1i8aNLZxZcSnevQc8h6HDSEg_ZiPvfFTVUqi3dKnH8aTwGx9_UBjXXynxz4dD3lmzL2yzUrUfymX7M2wqZWPOmIc0EVmsiJJ0u6tMX_B-D5XXVJpWlEU6p5AqN9nCbVw52f7o17SpBi29F9gO81I4wR61dGu6_4eyViX3Vj_rfPNkVuRnLJYtfjKAikwhHPNxpYnxbX0c5OH_Oio6yFOKsloRaGsl7Uwc5oAfUB5fgn7hm8SIx7fCLav0I",
      status: "Active",
      unread: false,
      updated_at: "Yesterday",
      messages: [
        { id: 601, text: "Hi, I was able to successfully onboard.", sender_type: "LEAD", sent_at: "Yesterday" },
        { id: 602, text: "Awesome! Let us know if you need anything else.", sender_type: "HUMAN", sent_at: "Yesterday" },
        { id: 603, text: "Issue resolved. Closing this ticket. Thank you!", sender_type: "LEAD", sent_at: "Yesterday" }
      ]
    }
  ];

  // Selected State
  let selectedId = 1;
  let searchQuery = '';
  let activeFilter = 'All'; // 'All', 'AI Active', 'Human Controlled', 'Unread'
  let isDrawerOpen = false;
  let messageText = '';
  let chatFeedContainer;

  // Track viewport size for Svelte logic if needed (or do it in CSS)
  let isMobile = false;

  function handleResize() {
    isMobile = window.innerWidth < 768;
  }

  onMount(() => {
    handleResize();
    window.addEventListener('resize', handleResize);
    scrollToBottom();
    return () => window.removeEventListener('resize', handleResize);
  });

  // Derived Values
  $: selectedDialog = dialogs.find(d => d.id === selectedId);

  $: filteredDialogs = dialogs.filter(d => {
    const matchesSearch = d.lead_username.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          (d.messages.length > 0 && d.messages[d.messages.length - 1].text.toLowerCase().includes(searchQuery.toLowerCase()));

    if (!matchesSearch) return false;

    if (activeFilter === 'AI Active') {
      return d.is_ai_active;
    } else if (activeFilter === 'Human Controlled') {
      return !d.is_ai_active;
    } else if (activeFilter === 'Unread') {
      return d.unread;
    }
    return true;
  });

  // Scroll logic
  async function scrollToBottom() {
    await tick();
    if (chatFeedContainer) {
      chatFeedContainer.scrollTop = chatFeedContainer.scrollHeight;
    }
  }

  // Set active dialog
  function selectDialog(id) {
    selectedId = id;
    // Mark as read
    const idx = dialogs.findIndex(d => d.id === id);
    if (idx !== -1) {
      dialogs[idx].unread = false;
      dialogs = [...dialogs];
    }
    scrollToBottom();
  }

  // Handle send message
  function handleSendMessage() {
    if (!messageText.trim() || !selectedDialog) return;

    const targetId = selectedDialog.id;
    const now = new Date();
    const timeString = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    // 1. Append message
    const newMessage = {
      id: Date.now(),
      text: messageText,
      sender_type: "HUMAN",
      sent_at: timeString
    };

    // 2. Immediate state update - set as human controlled optimistically
    const updatedDialogs = dialogs.map(d => {
      if (d.id === targetId) {
        return {
          ...d,
          is_ai_active: false, // Turn off AI immediately! Human intervened!
          updated_at: timeString,
          messages: [...d.messages, newMessage]
        };
      }
      return d;
    });

    dialogs = updatedDialogs;
    messageText = '';
    scrollToBottom();
  }

  // Handle keydown for send message
  function handleKeyDown(event) {
    if (event.key === 'Enter') {
      event.preventDefault();
      handleSendMessage();
    }
  }

  // Toggle AI Active manually
  function toggleAIActive(dialogId) {
    dialogs = dialogs.map(d => {
      if (d.id === dialogId) {
        return {
          ...d,
          is_ai_active: !d.is_ai_active
        };
      }
      return d;
    });
  }

  // Toggle Nav Drawer
  function toggleDrawer() {
    isDrawerOpen = !isDrawerOpen;
  }
</script>

<!-- Main Scaffold -->
<div class="h-screen w-screen flex flex-col overflow-hidden bg-surface font-body-md text-on-surface">

  <!-- TopAppBar (No fixed/absolute positioning needed since we use standard flexbox layout) -->
  <header class="h-16 flex-shrink-0 border-b border-outline-variant bg-surface flex justify-between items-center px-container-margin glass-header z-20" aria-label="Global Header">
    <div class="flex items-center gap-3">
      <!-- Back button for mobile when chat is open -->
      {#if isMobile && selectedId !== null}
        <button
          type="button"
          class="p-2 -ml-2 rounded-full hover:bg-surface-container-low transition-colors text-on-surface-variant focus:outline-none focus:ring-2 focus:ring-secondary"
          on:click={() => selectedId = null}
          aria-label="Back to conversations list"
        >
          <span class="material-symbols-outlined text-[24px]">arrow_back</span>
        </button>
      {/if}

      <div class="w-8 h-8 rounded-full overflow-hidden bg-surface-container-high ring-1 ring-outline-variant">
        <img
          class="w-full h-full object-cover"
          alt="Support Operator Profile"
          src="https://lh3.googleusercontent.com/aida-public/AB6AXuDzptN2jBgjzRjrcvHNkIOi5N2aQA8QLav0TLxBo1hZptFHWcpgGKyaLUyMFRehkZV_Zk09WNZqXHWHKTePpEb0liv6DM2TtmG2Vqb1C7t5Tid76h45Hi6RkYRP4v79ZDcyehYIhb6xTb4Yz0opJ1sYB_JymOmhLhydn-HlolviHVMmH9BPOj34XZsoNBEWQKvsTlI4biAszgDYWbUZqiymxr1Wc5azO8sK5wiSsUPSJlwZOQwrBspJpW0e4Rtt-F69xlzrc_oZkOC4"
          width="32"
          height="32"
        />
      </div>
      <h1 class="font-headline-md text-headline-md text-primary tracking-tight">Operator Console</h1>
    </div>

    <div class="flex items-center gap-2">
      <button
        type="button"
        class="p-2 rounded-full hover:bg-surface-container-low transition-colors text-on-surface-variant focus:outline-none focus:ring-2 focus:ring-secondary"
        aria-label="Network Status: Good"
      >
        <span class="material-symbols-outlined text-[24px]">signal_cellular_alt</span>
      </button>
      <button
        type="button"
        class="p-2 rounded-full hover:bg-surface-container-low transition-colors text-on-surface-variant focus:outline-none focus:ring-2 focus:ring-secondary"
        on:click={toggleDrawer}
        aria-label="Toggle Navigation Menu"
        aria-expanded={isDrawerOpen}
      >
        <span class="material-symbols-outlined text-[24px]">menu</span>
      </button>
    </div>
  </header>

  <!-- Side Navigation Drawer overlay -->
  {#if isDrawerOpen}
    <!-- svelte-ignore a11y-click-events-have-key-events -->
    <!-- svelte-ignore a11y-no-static-element-interactions -->
    <div
      class="fixed inset-0 bg-black/40 z-[55] transition-opacity duration-300"
      on:click={toggleDrawer}
      aria-hidden="true"
    ></div>
  {/if}

  <!-- Side Navigation Drawer -->
  <aside
    class="fixed inset-y-0 left-0 z-[60] flex flex-col w-80 bg-surface shadow-2xl transition-transform duration-300 ease-in-out border-r border-outline-variant {isDrawerOpen ? 'translate-x-0' : '-translate-x-full'}"
    aria-label="Sidebar Navigation"
  >
    <div class="p-6 border-b border-outline-variant">
      <div class="flex items-center gap-4">
        <div class="w-12 h-12 rounded-full overflow-hidden flex-shrink-0">
          <img
            class="w-full h-full object-cover"
            alt="Operator Profile Headshot"
            src="https://lh3.googleusercontent.com/aida-public/AB6AXuDscMtLpScGRCxjvoK4Q5eCj_FPSMHFx7ydpzOI_ZiBlYwh6SqJVVkfrdSLKt6RsNmBDbIVvui8ATM8WE9bhsjAGfFOht74LbgbkuEOyOTXAD_b_1QopeopNB2R-zomUhSUyIzrPB6aizLDLa1lf_69BzV9myDEGD0ACOoc0UPBUJV_eDsEqa4ouGAmrvIPEegQ8w5qkMAx1N89dQeljFH2xk23ohvcg38plGEqf0epvt_JYmr4g7oxBlWkP-xBffK9LZE4zwmXscRC"
            width="48"
            height="48"
          />
        </div>
        <div>
          <p class="font-headline-md text-body-lg font-bold text-primary">Operator #402</p>
          <div class="flex items-center gap-1.5 mt-0.5">
            <span class="w-2.5 h-2.5 rounded-full bg-secondary" aria-hidden="true"></span>
            <p class="text-label-sm text-on-surface-variant font-medium">Available</p>
          </div>
        </div>
      </div>
    </div>

    <nav class="flex-1 py-4" aria-label="Drawer Navigation Links">
      <a class="flex items-center gap-4 px-6 py-3 text-on-surface-variant hover:bg-surface-container-high transition-all" href="#profile" on:click|preventDefault>
        <span class="material-symbols-outlined text-[24px]">account_circle</span>
        <span class="font-body-md text-body-md font-medium">My Profile</span>
      </a>
      <a class="flex items-center gap-4 px-6 py-3 text-on-surface-variant hover:bg-surface-container-high transition-all" href="#settings" on:click|preventDefault>
        <span class="material-symbols-outlined text-[24px]">settings</span>
        <span class="font-body-md text-body-md font-medium">Settings</span>
      </a>
      <a class="flex items-center gap-4 px-6 py-3 text-on-surface-variant hover:bg-surface-container-high transition-all" href="#health" on:click|preventDefault>
        <span class="material-symbols-outlined text-[24px]">dns</span>
        <span class="font-body-md text-body-md font-medium">System Health</span>
      </a>
    </nav>

    <div class="p-6 border-t border-outline-variant">
      <a class="flex items-center gap-4 text-error py-3 hover:opacity-80 transition-opacity" href="#logout" on:click|preventDefault>
        <span class="material-symbols-outlined text-[24px]">logout</span>
        <span class="font-body-md text-body-md font-bold">Logout</span>
      </a>
      <p class="mt-4 text-label-sm text-outline font-medium">v2.4.0</p>
    </div>
  </aside>

  <!-- Layout Container: split view on desktop, single pane switch on mobile -->
  <div class="flex-1 flex overflow-hidden min-h-0 relative">

    <!-- Left Pane: Conversation List / Inbox -->
    <section
      class="w-full md:w-[400px] border-r border-outline-variant bg-surface flex flex-col flex-shrink-0 h-full overflow-hidden {isMobile && selectedId !== null ? 'hidden' : 'flex'}"
      aria-label="Conversation List"
    >
      <!-- Search & Filters Container -->
      <div class="bg-surface px-container-margin py-3 space-y-3 border-b border-outline-variant glass-header">
        <div class="relative">
          <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-[20px]" aria-hidden="true">search</span>
          <input
            class="w-full pl-10 pr-4 py-2 bg-white border border-outline-variant rounded-xl focus:ring-2 focus:ring-secondary focus:border-transparent font-body-md text-body-md transition-shadow"
            placeholder="Search conversations..."
            type="text"
            bind:value={searchQuery}
            aria-label="Search conversations"
          />
        </div>

        <!-- Filter chips using list pattern with buttons -->
        <div class="flex gap-1.5 overflow-x-auto no-scrollbar pb-1" role="tablist" aria-label="Filter conversations">
          {#each ['All', 'AI Active', 'Human Controlled', 'Unread'] as filterType}
            <button
              type="button"
              role="tab"
              aria-selected={activeFilter === filterType}
              class="px-4 py-1.5 rounded-full font-label-md text-label-sm whitespace-nowrap transition-colors focus:outline-none focus:ring-2 focus:ring-secondary font-semibold
                {activeFilter === filterType
                  ? 'bg-primary text-on-primary'
                  : 'bg-surface-container-low text-on-surface-variant border border-outline-variant hover:bg-surface-container-high'}"
              on:click={() => activeFilter = filterType}
            >
              {filterType}
            </button>
          {/each}
        </div>
      </div>

      <!-- Conversations scrolling list -->
      <div class="flex-1 overflow-y-auto divide-y divide-outline-variant" role="feed" aria-label="Conversations list">
        {#each filteredDialogs as dialog (dialog.id)}
          <button
            type="button"
            class="w-full text-left relative bg-white p-[12px_16px] flex gap-3 hover:bg-surface-container-lowest transition-colors cursor-pointer group focus:outline-none focus:bg-surface-container-low
              {dialog.unread ? 'unread-indicator' : ''}
              {selectedId === dialog.id ? 'bg-surface-container-low border-r-4 border-secondary' : ''}"
            style="min-height: 80px;"
            on:click={() => selectDialog(dialog.id)}
            aria-label="Conversation with {dialog.lead_username}, {dialog.is_ai_active ? 'AI Active' : 'Human Controlled'}"
          >
            <!-- Avatar with fixed sizing to prevent layout shift -->
            <div class="w-10 h-10 rounded-full flex-shrink-0 overflow-hidden bg-surface-container-high ring-1 ring-black/5" style="width: 40px; height: 40px;">
              <img
                class="w-full h-full object-cover"
                alt="{dialog.lead_username} avatar"
                src={dialog.avatar}
                width="40"
                height="40"
              />
            </div>

            <div class="flex-1 min-w-0 flex flex-col justify-between">
              <div class="flex justify-between items-baseline mb-0.5">
                <h3 class="text-body-md font-bold text-on-surface truncate pr-2">{dialog.lead_username}</h3>
                <span class="font-timestamp text-timestamp text-on-surface-variant font-medium flex-shrink-0">{dialog.updated_at}</span>
              </div>

              <div class="flex justify-between items-center gap-2">
                <p class="text-body-sm text-on-surface-variant truncate flex-1 pr-1 font-medium">
                  {dialog.messages.length > 0 ? dialog.messages[dialog.messages.length - 1].text : "No messages yet"}
                </p>

                <!-- Status Tags Container with fixed layout reservation -->
                <div class="flex gap-1 items-center flex-shrink-0" style="min-width: 40px; justify-content: flex-end;">
                  {#if dialog.is_ai_active}
                    <span class="bg-secondary-container text-on-secondary-container font-label-sm text-[10px] leading-3 px-1.5 py-0.5 rounded-full font-bold flex items-center gap-0.5">
                      AI
                    </span>
                  {:else}
                    <span class="bg-surface-container-highest text-on-surface-variant font-label-sm text-[10px] leading-3 px-1.5 py-0.5 rounded-full font-bold flex items-center gap-0.5">
                      HUMAN
                    </span>
                  {/if}
                  <span class="
                    {dialog.status === 'New' ? 'bg-secondary-container text-on-secondary-container' : ''}
                    {dialog.status === 'Active' ? 'bg-primary-fixed text-on-primary-fixed-variant' : ''}
                    {dialog.status === 'Waiting' ? 'bg-tertiary-fixed text-on-tertiary-fixed-variant' : ''}
                    font-label-sm text-[10px] leading-3 px-1.5 py-0.5 rounded-full font-bold"
                  >
                    {dialog.status}
                  </span>
                </div>
              </div>
            </div>
          </button>
        {:else}
          <div class="p-8 text-center text-on-surface-variant font-medium">
            <span class="material-symbols-outlined text-[48px] text-outline opacity-40 mb-2">forum</span>
            <p>No conversations match your criteria.</p>
          </div>
        {/each}
      </div>
    </section>

    <!-- Right Pane: Active Chat Window -->
    <section
      class="flex-1 flex flex-col bg-background relative h-full overflow-hidden {isMobile && selectedId === null ? 'hidden' : 'flex'}"
      aria-label="Active Conversation: {selectedDialog ? selectedDialog.lead_username : 'None'}"
    >
      {#if selectedDialog}
        <!-- Active Chat Header -->
        <header class="h-16 border-b border-outline-variant bg-surface flex justify-between items-center px-container-margin flex-shrink-0 z-10 glass-header">
          <div class="flex items-center gap-3 min-w-0">
            <div class="w-10 h-10 rounded-full flex-shrink-0 overflow-hidden bg-surface-container-high ring-1 ring-black/5" style="width: 40px; height: 40px;">
              <img
                class="w-full h-full object-cover"
                alt="{selectedDialog.lead_username} avatar"
                src={selectedDialog.avatar}
                width="40"
                height="40"
              />
            </div>
            <div class="min-w-0">
              <h2 class="text-body-lg font-bold text-primary truncate leading-tight">{selectedDialog.lead_username}</h2>
              <p class="text-label-sm text-on-surface-variant font-medium truncate">{selectedDialog.lead_phone || "Telegram user"}</p>
            </div>
          </div>

          <!-- Manual Controls: AI active / Human controlled toggle -->
          <div class="flex items-center gap-2">
            <button
              type="button"
              class="flex items-center gap-1.5 px-3 py-1.5 rounded-full text-label-sm font-bold transition-all focus:outline-none focus:ring-2 focus:ring-secondary
                {selectedDialog.is_ai_active
                  ? 'bg-secondary-container text-on-secondary-container hover:bg-secondary/20'
                  : 'bg-surface-container-highest text-on-surface-variant hover:bg-surface-container-high'}"
              on:click={() => toggleAIActive(selectedDialog.id)}
              aria-label="Toggle AI control mode. Currently {selectedDialog.is_ai_active ? 'AI Active' : 'Human controlled'}"
            >
              <span class="material-symbols-outlined text-[18px]">
                {selectedDialog.is_ai_active ? 'smart_toy' : 'person'}
              </span>
              <span class="hidden sm:inline font-semibold">
                {selectedDialog.is_ai_active ? 'AI Copilot ON' : 'Human Controlled'}
              </span>
              <span class="sm:hidden font-semibold">
                {selectedDialog.is_ai_active ? 'AI ON' : 'Manual'}
              </span>
            </button>
          </div>
        </header>

        <!-- Message History Feed -->
        <div
          class="flex-1 overflow-y-auto p-4 space-y-3"
          bind:this={chatFeedContainer}
          role="log"
          aria-label="Message logs"
        >
          {#each selectedDialog.messages as message (message.id)}
            <div class="flex flex-col {message.sender_type === 'HUMAN' ? 'items-end' : 'items-start'}">

              <!-- Message card -->
              <div
                class="max-w-[75%] rounded-xl px-4 py-2.5 shadow-sm font-medium text-body-md transition-colors
                  {message.sender_type === 'HUMAN'
                    ? 'bg-primary text-on-primary rounded-br-none'
                    : message.sender_type === 'AI'
                      ? 'bg-secondary-container text-on-secondary-container border border-secondary/20 rounded-bl-none'
                      : 'bg-white text-on-surface border border-outline-variant/40 rounded-bl-none'}"
              >
                <!-- Badge for AI messages to denote automated response -->
                {#if message.sender_type === 'AI'}
                  <div class="flex items-center gap-1 text-[10px] leading-3 uppercase font-extrabold tracking-wider mb-1 text-on-secondary-container opacity-85">
                    <span class="material-symbols-outlined text-[12px]">smart_toy</span>
                    AI Assistant
                  </div>
                {/if}

                <p class="whitespace-pre-wrap break-words leading-relaxed">{message.text}</p>
              </div>

              <!-- Timestamp and sender detail -->
              <div class="mt-1 flex items-center gap-1 px-1">
                <span class="text-timestamp text-on-surface-variant font-medium">
                  {message.sender_type === 'HUMAN' ? 'You' : message.sender_type === 'AI' ? 'AI Bot' : selectedDialog.lead_username} • {message.sent_at}
                </span>
              </div>
            </div>
          {/each}
        </div>

        <!-- Chat Input Footer Panel -->
        <footer class="p-3 border-t border-outline-variant bg-surface flex-shrink-0">
          <div class="relative flex items-center bg-white border border-outline-variant rounded-xl focus-within:ring-2 focus-within:ring-secondary transition-shadow px-2 py-1.5">
            <textarea
              class="flex-1 pl-2 pr-10 py-1.5 focus:outline-none resize-none font-body-md text-body-md max-h-24 bg-transparent text-on-surface"
              placeholder="Type a message to take over human control..."
              rows="1"
              bind:value={messageText}
              on:keydown={handleKeyDown}
              aria-label="Type message"
            ></textarea>

            <button
              type="button"
              class="p-2 bg-primary text-on-primary rounded-full flex items-center justify-center hover:bg-primary/95 transition-colors focus:outline-none focus:ring-2 focus:ring-secondary disabled:opacity-40"
              disabled={!messageText.trim()}
              on:click={handleSendMessage}
              aria-label="Send message"
            >
              <span class="material-symbols-outlined text-[20px] font-bold">send</span>
            </button>
          </div>
          <div class="flex justify-between items-center px-1 mt-1.5">
            <span class="text-[11px] text-on-surface-variant font-medium">
              {#if selectedDialog.is_ai_active}
                <span class="text-secondary font-bold">🤖 AI Active.</span> Typing/sending overrides and sets to <span class="font-bold">Human controlled</span>.
              {:else}
                <span class="text-outline font-bold">🧑 Human controlled.</span> AI dialogue engine is paused.
              {/if}
            </span>
          </div>
        </footer>
      {:else}
        <!-- Empty / placeholder state -->
        <div class="flex-1 flex flex-col items-center justify-center p-8 text-center text-on-surface-variant font-medium bg-surface">
          <span class="material-symbols-outlined text-[64px] text-outline opacity-35 mb-4">chat</span>
          <h2 class="text-headline-md text-primary mb-1">No Active Chat</h2>
          <p class="max-w-xs">Select a conversation from the list to begin monitoring or managing communication.</p>
        </div>
      {/if}
    </section>

  </div>

  <!-- BottomNavBar (Only shown on mobile) -->
  <nav class="md:hidden h-16 flex-shrink-0 bg-surface border-t border-outline-variant flex justify-around items-center px-4 py-2 pb-safe shadow-sm glass-header" aria-label="Main Navigation Tabs">
    <button
      type="button"
      class="flex flex-col items-center justify-center bg-secondary-container text-on-secondary-container rounded-full px-5 py-1 transition-transform scale-95 active:scale-90 focus:outline-none focus:ring-2 focus:ring-secondary"
      aria-label="Inbox section"
    >
      <span class="material-symbols-outlined text-[24px]">inbox</span>
      <span class="font-label-md text-label-sm font-bold">Inbox</span>
    </button>

    <button
      type="button"
      class="flex flex-col items-center justify-center text-on-surface-variant px-5 py-1 transition-transform scale-95 active:scale-90 hover:bg-surface-container-high rounded-full focus:outline-none focus:ring-2 focus:ring-secondary"
      aria-label="Active conversations section"
    >
      <span class="material-symbols-outlined text-[24px]">chat_bubble</span>
      <span class="font-label-md text-label-sm font-bold">Active</span>
    </button>

    <button
      type="button"
      class="flex flex-col items-center justify-center text-on-surface-variant px-5 py-1 transition-transform scale-95 active:scale-90 hover:bg-surface-container-high rounded-full focus:outline-none focus:ring-2 focus:ring-secondary"
      aria-label="Dashboard section"
    >
      <span class="material-symbols-outlined text-[24px]">dashboard</span>
      <span class="font-label-md text-label-sm font-bold">Dashboard</span>
    </button>
  </nav>

</div>
