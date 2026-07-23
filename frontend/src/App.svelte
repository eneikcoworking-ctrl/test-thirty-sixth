<script>
  import { onMount } from 'svelte';

  // State Management (Svelte 5 Runes)
  let activeTab = $state('dashboard');
  let sidebarOpen = $state(false); // Mobile sidebar drawer toggle

  // Spintax Template Editor State
  let spintaxTemplate = $state(
    '{Hi|Hello|Hey} {there|friend|prospect}! Check out our new platform {LeadGen Bot|LeadGen}! Let us know if you want to {test it|learn more}.'
  );
  let rerollCounter = $state(0);

  // Derived Previews for Spintax (reacts to spintaxTemplate and rerollCounter)
  const singlePreview = $derived.by(() => {
    // Accessing rerollCounter to establish reactive dependency
    const _ = rerollCounter;
    return parseSpintax(spintaxTemplate);
  });

  const alternativePreviews = $derived.by(() => {
    const _ = rerollCounter;
    const list = [];
    for (let i = 0; i < 4; i++) {
      list.push(parseSpintax(spintaxTemplate));
    }
    return list;
  });

  // Lead List Ingestion State
  let csvText = $state('');
  let parsedLeads = $state([]);
  let isDragging = $state(false);
  let validationErrors = $state([]);
  let importSuccess = $state(false);
  let campaignName = $state('Summer Retargeting Q3');
  let campaignStatus = $state('Running');

  // Interactive Lead Editing State
  let editingId = $state(null);
  let editUsername = $state('');
  let editPhone = $state('');
  let editStatus = $state('');
  let editMeta = $state('');

  // Dashboard Stats (Static mock stats)
  let totalLeadsCount = $derived(24892 + parsedLeads.length);
  let conversionRate = $state('4.82%');
  let activeCampaignsCount = $state(18);

  // Mock campaigns list
  let campaigns = $state([
    { id: 1, name: 'Summer Retargeting Q3', category: 'Email Marketing', status: 'Running', reach: '124.5k' },
    { id: 2, name: 'Cold Lead Outreach', category: 'Direct Messaging', status: 'Paused', reach: '45.2k' },
    { id: 3, name: 'Product Launch Viral', category: 'Omnichannel', status: 'Running', reach: '892.0k' },
    { id: 4, name: 'Holiday Referral Promo', category: 'Customer Retention', status: 'Running', reach: '12.1k' }
  ]);

  // Activity Feed
  let activities = $state([
    { id: 1, title: 'Leads Uploaded', desc: '5,420 new leads imported from CSV', time: '2 minutes ago', icon: 'upload', bg: 'bg-secondary-container text-on-secondary-container' },
    { id: 2, title: 'Campaign Started', desc: '"Holiday Referral" is now active', time: '45 minutes ago', icon: 'rocket', bg: 'bg-primary-container text-on-primary-container' },
    { id: 3, title: 'Template Updated', desc: 'Sarah edited "Welcome Series V2"', time: '3 hours ago', icon: 'edit', bg: 'bg-tertiary-container text-on-tertiary-container' }
  ]);

  // Spintax helper phrases for one-click insert
  const spintaxHelpers = [
    '{Hi|Hello|Hey}',
    '{there|friend|prospect}',
    '{LeadGen Bot|LeadGen}',
    '{test it|learn more|get a demo}'
  ];

  // Functions & Business Logic

  // 1. Spintax Parser
  function parseSpintax(text) {
    if (!text) return '';
    let replaced = text;
    const spintaxRegex = /\{([^{}]+)\}/g;
    let iterations = 0;
    while (spintaxRegex.test(replaced) && iterations < 100) {
      replaced = replaced.replace(spintaxRegex, (match, choicesStr) => {
        const choices = choicesStr.split('|');
        return choices[Math.floor(Math.random() * choices.length)];
      });
      iterations++;
    }
    return replaced;
  }

  // Insert spintax phrase at current textarea cursor position
  function insertHelper(phrase) {
    const textarea = document.getElementById('spintax-textarea');
    if (textarea) {
      const start = textarea.selectionStart;
      const end = textarea.selectionEnd;
      spintaxTemplate = spintaxTemplate.substring(0, start) + phrase + spintaxTemplate.substring(end);
      // Refocus textarea after DOM updates
      setTimeout(() => {
        textarea.focus();
        textarea.setSelectionRange(start + phrase.length, start + phrase.length);
      }, 0);
    } else {
      spintaxTemplate += ' ' + phrase;
    }
  }

  // Re-roll/Generate fresh previews
  function forceReroll() {
    rerollCounter += 1;
  }

  // 2. CSV Parser
  function handleCSVUpload(text) {
    csvText = text;
    const lines = text.split(/\r?\n/);
    if (lines.length === 0) return;

    let headers = [];
    let startIndex = 0;

    // Detect headers
    if (lines[0].toLowerCase().includes('username') || lines[0].toLowerCase().includes('phone') || lines[0].toLowerCase().includes('status')) {
      headers = lines[0].split(',').map(h => h.trim().replace(/^["']|["']$/g, ''));
      startIndex = 1;
    } else {
      headers = ['username', 'phone_number', 'routing_status', 'metadata'];
    }

    const leads = [];
    for (let i = startIndex; i < lines.length; i++) {
      const line = lines[i].trim();
      if (!line) continue;

      const values = parseCSVLine(line);
      const lead = {
        id: Date.now() + i + Math.random(),
        username: '',
        phoneNumber: '',
        routingStatus: 'PENDING',
        metadata: '{}'
      };

      headers.forEach((header, index) => {
        const val = values[index] || '';
        const lowerHeader = header.toLowerCase();
        if (lowerHeader.includes('user') || lowerHeader === 'username') {
          lead.username = val;
        } else if (lowerHeader.includes('phone') || lowerHeader.includes('number')) {
          lead.phoneNumber = val;
        } else if (lowerHeader.includes('status') || lowerHeader.includes('routing')) {
          lead.routingStatus = val || 'PENDING';
        } else if (lowerHeader.includes('meta')) {
          lead.metadata = val;
        }
      });

      if (lead.username || lead.phoneNumber) {
        leads.push(lead);
      }
    }

    parsedLeads = leads;
    importSuccess = false;
    validationErrors = [];
    validateLeads();
  }

  // Helper for parsing CSV lines considering simple quotes
  function parseCSVLine(line) {
    const result = [];
    let current = '';
    let inQuotes = false;
    for (let i = 0; i < line.length; i++) {
      const char = line[i];
      if (char === '"') {
        // Toggle quote state
        inQuotes = !inQuotes;
        // Keep the quote inside the value if it's not the very start or end
        current += char;
      } else if (char === ',' && !inQuotes) {
        result.push(cleanCSVField(current));
        current = '';
      } else {
        current += char;
      }
    }
    result.push(cleanCSVField(current));
    return result;
  }

  function cleanCSVField(field) {
    let clean = field.trim();
    // If wrapped in quotes, strip the outermost pair of quotes
    if (clean.startsWith('"') && clean.endsWith('"')) {
      clean = clean.substring(1, clean.length - 1);
    } else if (clean.startsWith("'") && clean.endsWith("'")) {
      clean = clean.substring(1, clean.length - 1);
    }
    // Replace doubled double-quotes standard in CSV escaping
    clean = clean.replace(/""/g, '"');
    return clean;
  }

  // Load sample CSV for demonstration (standards-compliant RFC 4180 with escaped JSON fields)
  function loadSampleCSV() {
    const sample = `username,phone_number,routing_status,metadata
@alex_dev,+14155552671,PENDING,"{""source"":""hn"",""bio"":""CTO at TechCorp""}"
@sarah_m,+13125553892,PENDING,"{""source"":""twitter"",""bio"":""Founder""}"
@mike_ops,+12065559821,PENDING,"{""source"":""linkedin"",""bio"":""Director of Operations""}"
@lisa_biz,+16505554103,PENDING,"{""source"":""csv_import""}"
@dan_eng,+12125550099,PENDING,"{""source"":""cold_outreach""}"`;
    handleCSVUpload(sample);
  }

  // File drop handler
  function handleDrop(e) {
    e.preventDefault();
    isDragging = false;
    const file = e.dataTransfer.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (event) => {
        handleCSVUpload(event.target.result);
      };
      reader.readAsText(file);
    }
  }

  function handleFileSelect(e) {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (event) => {
        handleCSVUpload(event.target.result);
      };
      reader.readAsText(file);
    }
  }

  // Lead CRUD Operations
  function deleteLead(id) {
    parsedLeads = parsedLeads.filter(lead => lead.id !== id);
    validateLeads();
  }

  // Edit fields
  function startEdit(lead) {
    editingId = lead.id;
    editUsername = lead.username;
    editPhone = lead.phoneNumber;
    editStatus = lead.routingStatus;
    editMeta = lead.metadata;
  }

  function saveEdit(id) {
    parsedLeads = parsedLeads.map(lead => {
      if (lead.id === id) {
        return {
          ...lead,
          username: editUsername,
          phoneNumber: editPhone,
          routingStatus: editStatus,
          metadata: editMeta
        };
      }
      return lead;
    });
    editingId = null;
    validateLeads();
  }

  function cancelEdit() {
    editingId = null;
  }

  // Add Row
  function addNewLead() {
    const newLead = {
      id: Date.now() + Math.random(),
      username: '@new_prospect',
      phoneNumber: '+100000000',
      routingStatus: 'PENDING',
      metadata: '{"source":"manual"}'
    };
    parsedLeads = [newLead, ...parsedLeads];
    validateLeads();
    startEdit(newLead);
  }

  // 3. Validation Logic
  function validateLeads() {
    const errors = [];
    parsedLeads.forEach((lead, idx) => {
      const rowNum = idx + 1;
      if (!lead.username && !lead.phoneNumber) {
        errors.push(`Row ${rowNum}: Both username and phone number are missing.`);
      }
      if (lead.username && !lead.username.startsWith('@')) {
        errors.push(`Row ${rowNum}: Username "${lead.username}" should start with '@' symbol.`);
      }
      if (lead.phoneNumber && !/^\+?[1-9]\d{1,14}$/.test(lead.phoneNumber.replace(/[\s-()]/g, ''))) {
        errors.push(`Row ${rowNum}: Phone number "${lead.phoneNumber}" is not a valid international format.`);
      }
      if (lead.metadata) {
        try {
          JSON.parse(lead.metadata);
        } catch (e) {
          errors.push(`Row ${rowNum}: Metadata is not a valid JSON string.`);
        }
      }
    });
    validationErrors = errors;
    return errors.length === 0;
  }

  function clearLeads() {
    parsedLeads = [];
    validationErrors = [];
    importSuccess = false;
  }

  // Submit parsed results
  function submitCampaign() {
    validateLeads();
    if (validationErrors.length > 0) {
      importSuccess = false;
      return;
    }

    if (parsedLeads.length === 0) {
      validationErrors = ['Cannot ingest an empty lead list. Please upload or add leads first.'];
      importSuccess = false;
      return;
    }

    // Add campaign to list
    const newCampaignId = campaigns.length + 1;
    campaigns = [
      {
        id: newCampaignId,
        name: campaignName,
        category: 'Ingested Lead Outreach',
        status: campaignStatus,
        reach: `${(parsedLeads.length / 1000).toFixed(1)}k`
      },
      ...campaigns
    ];

    // Log activity
    activities = [
      {
        id: Date.now(),
        title: 'Leads Ingested',
        desc: `${parsedLeads.length} leads successfully validated and imported to "${campaignName}"`,
        time: 'Just now',
        icon: 'check_circle',
        bg: 'bg-green-100 text-green-700'
      },
      ...activities
    ];

    importSuccess = true;
    validationErrors = [];

    // Clear leads upon successful submission so dashboard shows updated state
    setTimeout(() => {
      activeTab = 'dashboard';
    }, 1500);
  }
</script>

<div class="min-h-screen bg-background text-on-surface flex flex-col md:flex-row">
  <!-- Sidebar Navigation Drawer -->
  <aside
    class="border-r border-outline-variant flex-col p-md space-y-sm z-50 bg-surface-container-low transition-all duration-200 {sidebarOpen ? 'flex w-64 fixed left-0 top-0 h-screen' : 'hidden md:flex w-64 md:fixed md:left-0 md:top-0 md:h-screen'}"
    aria-label="Sidebar Navigation"
  >
    <div class="mb-xl px-sm flex justify-between items-center">
      <h1 class="font-headline-md text-headline-md font-black text-primary">Marketing Ops</h1>
      <!-- Mobile Close Burger button -->
      <button
        class="md:hidden text-on-surface p-xs hover:bg-surface-container-high rounded-full focus-visible:ring-2 focus-visible:ring-primary"
        onclick={() => sidebarOpen = false}
        aria-label="Close menu"
      >
        <span class="material-symbols-outlined">close</span>
      </button>
    </div>

    <nav class="flex-1 space-y-xs">
      <button
        class="w-full flex items-center gap-md px-md py-sm font-label-md text-label-md rounded-full transition-all duration-200 ease-in-out focus-visible:ring-2 focus-visible:ring-primary"
        class:bg-secondary-container={activeTab === 'dashboard'}
        class:text-on-secondary-container={activeTab === 'dashboard'}
        class:font-bold={activeTab === 'dashboard'}
        class:text-secondary={activeTab !== 'dashboard'}
        class:hover:bg-surface-container-highest={activeTab !== 'dashboard'}
        onclick={() => { activeTab = 'dashboard'; sidebarOpen = false; }}
      >
        <span class="material-symbols-outlined">analytics</span>
        <span>Dashboard</span>
      </button>

      <button
        class="w-full flex items-center gap-md px-md py-sm font-label-md text-label-md rounded-full transition-all duration-200 ease-in-out focus-visible:ring-2 focus-visible:ring-primary"
        class:bg-secondary-container={activeTab === 'lead-upload'}
        class:text-on-secondary-container={activeTab === 'lead-upload'}
        class:font-bold={activeTab === 'lead-upload'}
        class:text-secondary={activeTab !== 'lead-upload'}
        class:hover:bg-surface-container-highest={activeTab !== 'lead-upload'}
        onclick={() => { activeTab = 'lead-upload'; sidebarOpen = false; }}
      >
        <span class="material-symbols-outlined">upload_file</span>
        <span>Lead Upload</span>
      </button>

      <button
        class="w-full flex items-center gap-md px-md py-sm font-label-md text-label-md rounded-full transition-all duration-200 ease-in-out focus-visible:ring-2 focus-visible:ring-primary"
        class:bg-secondary-container={activeTab === 'template-editor'}
        class:text-on-secondary-container={activeTab === 'template-editor'}
        class:font-bold={activeTab === 'template-editor'}
        class:text-secondary={activeTab !== 'template-editor'}
        class:hover:bg-surface-container-highest={activeTab !== 'template-editor'}
        onclick={() => { activeTab = 'template-editor'; sidebarOpen = false; }}
      >
        <span class="material-symbols-outlined">edit_note</span>
        <span>Template Editor</span>
      </button>
    </nav>

    <div class="pt-xl border-t border-outline-variant mt-auto">
      <div class="flex items-center gap-md p-sm hover:bg-surface-container-low rounded-lg cursor-pointer transition-colors" role="button" tabindex="0">
        <div class="w-10 h-10 rounded-full bg-primary-fixed text-on-primary-fixed flex items-center justify-center font-bold">
          JD
        </div>
        <div>
          <p class="font-label-md text-label-md text-on-surface">John Doe</p>
          <p class="font-body-sm text-body-sm text-on-surface-variant">Marketing Lead</p>
        </div>
      </div>
    </div>
  </aside>

  <!-- Main Content Wrapper -->
  <div class="flex-1 flex flex-col overflow-x-hidden md:pl-64">
    <!-- Top App Bar -->
    <header class="w-full sticky top-0 bg-surface-container-lowest border-b border-outline-variant z-40">
      <div class="flex items-center justify-between px-lg py-md w-full max-w-container-max mx-auto">
        <div class="flex items-center gap-md">
          <!-- Burger Menu Trigger for Mobile -->
          <button
            class="md:hidden text-on-surface p-xs hover:bg-surface-container-high rounded-full focus-visible:ring-2 focus-visible:ring-primary focus-visible:outline-none"
            onclick={() => sidebarOpen = !sidebarOpen}
            aria-label="Toggle Navigation Drawer"
          >
            <span class="material-symbols-outlined">menu</span>
          </button>
          <span class="material-symbols-outlined text-primary" aria-hidden="true">dashboard</span>
          <h2 class="font-headline-lg text-headline-lg font-bold text-primary">Campaign Manager</h2>
        </div>
        <div class="flex items-center gap-lg">
          <span class="material-symbols-outlined text-on-surface-variant cursor-pointer hover:bg-surface-container-low p-xs rounded-full transition-colors" role="button" tabindex="0" aria-label="Search">search</span>
          <span class="material-symbols-outlined text-on-surface-variant cursor-pointer hover:bg-surface-container-low p-xs rounded-full transition-colors" role="button" tabindex="0" aria-label="Notifications">notifications</span>
          <div class="w-8 h-8 rounded-full bg-surface-variant overflow-hidden">
            <span class="material-symbols-outlined text-on-surface-variant p-1">account_circle</span>
          </div>
        </div>
      </div>
    </header>

    <!-- Main Content Area -->
    <main class="p-lg max-w-container-max w-full mx-auto flex-1">

      <!-- Tab 1: Dashboard View -->
      {#if activeTab === 'dashboard'}
        <section aria-label="Overview Header" class="mb-xl flex flex-col sm:flex-row justify-between items-start sm:items-end gap-md">
          <div>
            <p class="font-body-md text-body-md text-on-surface-variant">Performance Overview</p>
            <h3 class="font-headline-xl text-headline-xl text-on-surface">System Health</h3>
          </div>
          <div class="flex gap-sm">
            <button class="px-md py-sm bg-surface-container-lowest border border-outline-variant text-on-surface font-label-md text-label-md rounded-lg hover:bg-surface-container-low transition-colors flex items-center gap-xs focus-visible:ring-2 focus-visible:ring-primary focus-visible:outline-none">
              <span class="material-symbols-outlined text-[20px]" aria-hidden="true">calendar_today</span>
              Last 30 Days
            </button>
            <button class="px-md py-sm bg-primary text-on-primary font-label-md text-label-md rounded-lg hover:brightness-110 active:scale-95 transition-all flex items-center gap-xs shadow-sm focus-visible:ring-2 focus-visible:ring-primary focus-visible:outline-none">
              <span class="material-symbols-outlined text-[20px]" aria-hidden="true">download</span>
              Export Report
            </button>
          </div>
        </section>

        <!-- Bento Grid Metrics -->
        <section aria-label="Key Metrics" class="grid grid-cols-1 md:grid-cols-3 gap-gutter mb-xl">
          <!-- Metric 1: Total Leads -->
          <div class="bg-surface-container-lowest border border-outline-variant p-md rounded-xl hover:bg-surface-container-low transition-colors group">
            <div class="flex justify-between items-start mb-md">
              <div class="p-sm bg-primary-container/10 rounded-lg text-primary">
                <span class="material-symbols-outlined" aria-hidden="true">group</span>
              </div>
              <span class="text-green-600 font-label-sm text-label-sm flex items-center">+12.5%</span>
            </div>
            <p class="font-body-md text-body-md text-on-surface-variant mb-xs">Total Leads</p>
            <h4 class="font-headline-lg text-headline-lg text-on-surface font-bold">{totalLeadsCount.toLocaleString()}</h4>
            <div class="mt-md h-1 w-full bg-surface-container overflow-hidden rounded-full">
              <div class="h-full bg-primary w-[75%] transition-all duration-1000"></div>
            </div>
          </div>

          <!-- Metric 2: Conversion Rate -->
          <div class="bg-surface-container-lowest border border-outline-variant p-md rounded-xl hover:bg-surface-container-low transition-colors">
            <div class="flex justify-between items-start mb-md">
              <div class="p-sm bg-tertiary-container/10 rounded-lg text-tertiary">
                <span class="material-symbols-outlined" aria-hidden="true">ads_click</span>
              </div>
              <span class="text-red-500 font-label-sm text-label-sm flex items-center">-2.1%</span>
            </div>
            <p class="font-body-md text-body-md text-on-surface-variant mb-xs">Conversion Rate</p>
            <h4 class="font-headline-lg text-headline-lg text-on-surface font-bold">{conversionRate}</h4>
            <div class="mt-md h-1 w-full bg-surface-container overflow-hidden rounded-full">
              <div class="h-full bg-tertiary w-[45%] transition-all duration-1000"></div>
            </div>
          </div>

          <!-- Metric 3: Active Campaigns -->
          <div class="bg-surface-container-lowest border border-outline-variant p-md rounded-xl hover:bg-surface-container-low transition-colors">
            <div class="flex justify-between items-start mb-md">
              <div class="p-sm bg-secondary-container/10 rounded-lg text-secondary">
                <span class="material-symbols-outlined" aria-hidden="true">rocket_launch</span>
              </div>
              <span class="text-on-surface-variant font-label-sm text-label-sm">Active Now</span>
            </div>
            <p class="font-body-md text-body-md text-on-surface-variant mb-xs">Active Campaigns</p>
            <h4 class="font-headline-lg text-headline-lg text-on-surface font-bold">{activeCampaignsCount}</h4>
            <div class="mt-md flex gap-xs">
              <div class="h-1 flex-1 bg-primary rounded-full"></div>
              <div class="h-1 flex-1 bg-primary rounded-full"></div>
              <div class="h-1 flex-1 bg-primary rounded-full opacity-30"></div>
              <div class="h-1 flex-1 bg-primary rounded-full opacity-30"></div>
            </div>
          </div>
        </section>

        <!-- Dashboard Subsections -->
        <section aria-label="Dashboard Details" class="grid grid-cols-1 lg:grid-cols-12 gap-gutter">
          <!-- Active Campaigns Table -->
          <div class="lg:col-span-8 bg-surface-container-lowest border border-outline-variant rounded-xl overflow-hidden flex flex-col">
            <div class="p-md border-b border-outline-variant flex justify-between items-center bg-surface-container-low/50">
              <h5 class="font-headline-md text-headline-md text-on-surface">Recent Campaigns</h5>
              <button class="text-primary font-label-md text-label-md hover:underline focus-visible:ring-2 focus-visible:ring-primary focus-visible:outline-none">View All</button>
            </div>
            <div class="overflow-x-auto">
              <table class="w-full text-left" aria-label="Recent Campaigns Table">
                <thead class="bg-surface-container-low">
                  <tr>
                    <th class="px-md py-sm font-label-md text-label-md text-on-surface-variant">Campaign Name</th>
                    <th class="px-md py-sm font-label-md text-label-md text-on-surface-variant">Status</th>
                    <th class="px-md py-sm font-label-md text-label-md text-on-surface-variant">Reach</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-outline-variant">
                  {#each campaigns as camp}
                    <tr class="hover:bg-surface-container-low transition-colors group">
                      <td class="px-md py-md">
                        <div class="flex items-center gap-sm">
                          <div class="w-2 h-2 rounded-full bg-primary"></div>
                          <div>
                            <p class="font-label-md text-label-md text-on-surface">{camp.name}</p>
                            <p class="font-body-sm text-body-sm text-on-surface-variant">{camp.category}</p>
                          </div>
                        </div>
                      </td>
                      <td class="px-md py-md">
                        <span
                          class="px-sm py-[2px] rounded-full font-label-sm text-label-sm border"
                          class:bg-green-100={camp.status === 'Running'}
                          class:text-green-700={camp.status === 'Running'}
                          class:border-green-200={camp.status === 'Running'}
                          class:bg-surface-container-highest={camp.status === 'Paused'}
                          class:text-on-surface-variant={camp.status === 'Paused'}
                          class:border-outline-variant={camp.status === 'Paused'}
                        >
                          {camp.status}
                        </span>
                      </td>
                      <td class="px-md py-md font-body-md text-body-md text-on-surface">{camp.reach}</td>
                    </tr>
                  {/each}
                </tbody>
              </table>
            </div>
          </div>

          <!-- Live Activity Sidebar -->
          <div class="lg:col-span-4 bg-surface-container-lowest border border-outline-variant p-md rounded-xl flex flex-col">
            <h5 class="font-headline-md text-headline-md text-on-surface mb-md">Live Activity</h5>
            <div class="space-y-md flex-1">
              {#each activities as act}
                <div class="flex gap-md">
                  <div class="relative flex flex-col items-center">
                    <div class="w-8 h-8 rounded-full flex items-center justify-center text-sm {act.bg}">
                      <span class="material-symbols-outlined text-sm">{act.icon}</span>
                    </div>
                  </div>
                  <div>
                    <p class="font-label-md text-label-md text-on-surface">{act.title}</p>
                    <p class="font-body-sm text-body-sm text-on-surface-variant">{act.desc}</p>
                    <p class="font-body-sm text-body-sm text-primary mt-xs">{act.time}</p>
                  </div>
                </div>
              {/each}
            </div>
          </div>
        </section>
      {/if}

      <!-- Tab 2: Lead Upload View -->
      {#if activeTab === 'lead-upload'}
        <section aria-label="Lead Upload and Ingestion">
          <div class="mb-xl">
            <p class="font-body-md text-body-md text-on-surface-variant">Ingestion Workflow (BARCAN-TAG-11)</p>
            <h3 class="font-headline-xl text-headline-xl text-on-surface">Campaign Configuration & Ingestion</h3>
          </div>

          <div class="grid grid-cols-1 lg:grid-cols-12 gap-gutter items-start">
            <!-- Left Config Forms -->
            <div class="lg:col-span-4 space-y-md">
              <div class="bg-surface-container-lowest border border-outline-variant p-md rounded-xl space-y-md shadow-sm">
                <h4 class="font-headline-md text-headline-md text-on-surface border-b border-outline-variant pb-xs">Campaign Info</h4>

                <div class="space-y-xs">
                  <label for="campaign-name-input" class="block font-label-md text-label-md text-on-surface-variant">Campaign Name</label>
                  <input
                    id="campaign-name-input"
                    type="text"
                    bind:value={campaignName}
                    placeholder="E.g. Summer Retargeting Q3"
                    class="w-full px-md py-sm border border-outline-variant rounded-lg bg-surface text-on-surface focus-visible:ring-2 focus-visible:ring-primary focus-visible:outline-none"
                  />
                </div>

                <div class="space-y-xs">
                  <label for="campaign-status-select" class="block font-label-md text-label-md text-on-surface-variant">Initial Status</label>
                  <select
                    id="campaign-status-select"
                    bind:value={campaignStatus}
                    class="w-full px-md py-sm border border-outline-variant rounded-lg bg-surface text-on-surface focus-visible:ring-2 focus-visible:ring-primary focus-visible:outline-none"
                  >
                    <option value="Running">Running (Active)</option>
                    <option value="Paused">Paused (Draft)</option>
                  </select>
                </div>
              </div>

              <!-- CSV File Drag and Drop Zone -->
              <div class="bg-surface-container-lowest border border-outline-variant p-md rounded-xl shadow-sm space-y-md">
                <h4 class="font-headline-md text-headline-md text-on-surface">Lead Source Ingestion</h4>

                <button
                  type="button"
                  class="w-full border-2 border-dashed rounded-xl p-lg text-center cursor-pointer transition-colors flex flex-col items-center justify-center gap-sm {isDragging ? 'border-primary bg-primary-container/10' : 'border-outline-variant hover:bg-surface-container-low'} focus-visible:ring-2 focus-visible:ring-primary focus-visible:outline-none"
                  ondragover={(e) => { e.preventDefault(); isDragging = true; }}
                  ondragleave={() => isDragging = false}
                  ondrop={handleDrop}
                  onclick={() => document.getElementById('csv-file-input').click()}
                  aria-label="Drag and drop CSV files here or click to upload"
                >
                  <span class="material-symbols-outlined text-[48px] text-primary" aria-hidden="true">cloud_upload</span>
                  <div>
                    <p class="font-label-md text-label-md text-on-surface font-semibold">Drag and drop CSV here</p>
                    <p class="font-body-sm text-body-sm text-on-surface-variant mt-xs">Or click to browse from system</p>
                  </div>
                  <input
                    id="csv-file-input"
                    type="file"
                    accept=".csv,.txt"
                    class="hidden"
                    onchange={handleFileSelect}
                  />
                </button>

                <div class="flex justify-between items-center pt-xs">
                  <span class="text-body-sm text-on-surface-variant">Need test data?</span>
                  <button
                    onclick={loadSampleCSV}
                    class="px-md py-sm border border-primary text-primary hover:bg-primary-container/10 rounded-lg font-label-md text-label-md transition-colors focus-visible:ring-2 focus-visible:ring-primary focus-visible:outline-none"
                  >
                    Load Sample CSV
                  </button>
                </div>
              </div>
            </div>

            <!-- Right Contacts Data Grid Preview -->
            <div class="lg:col-span-8 space-y-md">
              <div class="bg-surface-container-lowest border border-outline-variant rounded-xl shadow-sm overflow-hidden flex flex-col">
                <div class="p-md border-b border-outline-variant flex flex-col sm:flex-row justify-between items-start sm:items-center gap-md bg-surface-container-low/30">
                  <div>
                    <h4 class="font-headline-md text-headline-md text-on-surface">Leads Preview Grid</h4>
                    <p class="font-body-sm text-body-sm text-on-surface-variant">Parsed {parsedLeads.length} total leads from source file</p>
                  </div>
                  <div class="flex gap-sm self-stretch sm:self-auto">
                    <button
                      onclick={addNewLead}
                      class="flex-1 sm:flex-none px-md py-sm bg-secondary text-on-primary font-label-md text-label-md rounded-lg hover:brightness-110 active:scale-95 transition-all flex items-center justify-center gap-xs focus-visible:ring-2 focus-visible:ring-primary focus-visible:outline-none"
                    >
                      <span class="material-symbols-outlined text-[18px]" aria-hidden="true">person_add</span>
                      Add Row
                    </button>
                    <button
                      onclick={clearLeads}
                      class="flex-1 sm:flex-none px-md py-sm border border-outline-variant text-error hover:bg-error-container/10 font-label-md text-label-md rounded-lg transition-all focus-visible:ring-2 focus-visible:ring-error focus-visible:outline-none"
                    >
                      Clear
                    </button>
                  </div>
                </div>

                <!-- Live Data Grid -->
                <div class="overflow-x-auto min-h-[250px]">
                  {#if parsedLeads.length === 0}
                    <div class="flex flex-col items-center justify-center p-xl text-center space-y-xs min-h-[250px]">
                      <span class="material-symbols-outlined text-[48px] text-outline" aria-hidden="true">table_rows</span>
                      <p class="font-label-md text-label-md text-on-surface">No leads parsed yet</p>
                      <p class="font-body-sm text-body-sm text-on-surface-variant max-w-sm">Please upload a CSV list or click "Load Sample CSV" to test the ingestion preview.</p>
                    </div>
                  {:else}
                    <table class="w-full text-left border-collapse" aria-label="Parsed Contacts Grid">
                      <thead class="bg-surface-container-low">
                        <tr>
                          <th class="px-md py-sm font-label-md text-label-md text-on-surface-variant">Username</th>
                          <th class="px-md py-sm font-label-md text-label-md text-on-surface-variant">Phone Number</th>
                          <th class="px-md py-sm font-label-md text-label-md text-on-surface-variant">Status</th>
                          <th class="px-md py-sm font-label-md text-label-md text-on-surface-variant">Metadata</th>
                          <th class="px-md py-sm font-label-md text-label-md text-on-surface-variant text-right">Actions</th>
                        </tr>
                      </thead>
                      <tbody class="divide-y divide-outline-variant">
                        {#each parsedLeads as lead}
                          <tr class="hover:bg-surface-container-low/50 transition-colors">
                            {#if editingId === lead.id}
                              <!-- Editable Row Inputs -->
                              <td class="px-md py-sm">
                                <input
                                  type="text"
                                  bind:value={editUsername}
                                  class="w-full px-sm py-[4px] border border-primary rounded text-body-md text-on-surface bg-surface focus:outline-none focus:ring-1 focus:ring-primary"
                                />
                              </td>
                              <td class="px-md py-sm">
                                <input
                                  type="text"
                                  bind:value={editPhone}
                                  class="w-full px-sm py-[4px] border border-primary rounded text-body-md text-on-surface bg-surface focus:outline-none focus:ring-1 focus:ring-primary"
                                />
                              </td>
                              <td class="px-md py-sm">
                                <select
                                  bind:value={editStatus}
                                  class="w-full px-xs py-[4px] border border-primary rounded text-body-md text-on-surface bg-surface focus:outline-none focus:ring-1 focus:ring-primary"
                                >
                                  <option value="PENDING">PENDING</option>
                                  <option value="PROCESSING">PROCESSING</option>
                                  <option value="COMPLETED">COMPLETED</option>
                                  <option value="FAILED">FAILED</option>
                                </select>
                              </td>
                              <td class="px-md py-sm">
                                <input
                                  type="text"
                                  bind:value={editMeta}
                                  class="w-full px-sm py-[4px] border border-primary rounded text-body-md text-on-surface bg-surface focus:outline-none focus:ring-1 focus:ring-primary"
                                />
                              </td>
                              <td class="px-md py-sm text-right flex items-center justify-end gap-sm h-full">
                                <button
                                  onclick={() => saveEdit(lead.id)}
                                  class="text-green-600 hover:text-green-800 p-xs focus-visible:ring-2 focus-visible:ring-green-600 focus-visible:outline-none"
                                  aria-label="Save changes for this row"
                                >
                                  <span class="material-symbols-outlined">check</span>
                                </button>
                                <button
                                  onclick={cancelEdit}
                                  class="text-on-surface-variant hover:text-on-surface p-xs focus-visible:ring-2 focus-visible:ring-primary focus-visible:outline-none"
                                  aria-label="Cancel editing this row"
                                >
                                  <span class="material-symbols-outlined">close</span>
                                </button>
                              </td>
                            {:else}
                              <!-- Standard View Row -->
                              <td class="px-md py-md font-body-md text-body-md text-on-surface">
                                {#if lead.username}
                                  <span class="font-medium text-primary">{lead.username}</span>
                                {:else}
                                  <span class="text-on-surface-variant italic">No username</span>
                                {/if}
                              </td>
                              <td class="px-md py-md font-body-md text-body-md text-on-surface">
                                {#if lead.phoneNumber}
                                  <span>{lead.phoneNumber}</span>
                                {:else}
                                  <span class="text-on-surface-variant italic">No phone</span>
                                {/if}
                              </td>
                              <td class="px-md py-md">
                                <span
                                  class="px-sm py-[2px] rounded-full font-label-sm text-label-sm border"
                                  class:bg-blue-100={lead.routingStatus === 'PENDING'}
                                  class:text-blue-700={lead.routingStatus === 'PENDING'}
                                  class:border-blue-200={lead.routingStatus === 'PENDING'}
                                  class:bg-yellow-100={lead.routingStatus === 'PROCESSING'}
                                  class:text-yellow-700={lead.routingStatus === 'PROCESSING'}
                                  class:border-yellow-200={lead.routingStatus === 'PROCESSING'}
                                  class:bg-green-100={lead.routingStatus === 'COMPLETED'}
                                  class:text-green-700={lead.routingStatus === 'COMPLETED'}
                                  class:border-green-200={lead.routingStatus === 'COMPLETED'}
                                  class:bg-red-100={lead.routingStatus === 'FAILED'}
                                  class:text-red-700={lead.routingStatus === 'FAILED'}
                                  class:border-red-200={lead.routingStatus === 'FAILED'}
                                >
                                  {lead.routingStatus}
                                </span>
                              </td>
                              <td class="px-md py-md font-body-sm text-body-sm text-on-surface-variant max-w-[200px] truncate animate-pulse">
                                {lead.metadata}
                              </td>
                              <td class="px-md py-md text-right">
                                <div class="flex items-center justify-end gap-xs">
                                  <button
                                    onclick={() => startEdit(lead)}
                                    class="text-on-surface-variant hover:text-primary p-xs transition-colors focus-visible:ring-2 focus-visible:ring-primary focus-visible:outline-none"
                                    aria-label="Edit lead row"
                                  >
                                    <span class="material-symbols-outlined text-[18px]">edit</span>
                                  </button>
                                  <button
                                    onclick={() => deleteLead(lead.id)}
                                    class="text-on-surface-variant hover:text-error p-xs transition-colors focus-visible:ring-2 focus-visible:ring-error focus-visible:outline-none"
                                    aria-label="Delete lead row"
                                  >
                                    <span class="material-symbols-outlined text-[18px]">delete</span>
                                  </button>
                                </div>
                              </td>
                            {/if}
                          </tr>
                        {/each}
                      </tbody>
                    </table>
                  {/if}
                </div>
              </div>

              <!-- Validation Warnings & Submit Actions -->
              <div class="space-y-sm">
                {#if validationErrors.length > 0}
                  <div class="p-md bg-error-container/30 border border-error/50 rounded-xl space-y-xs" role="alert" aria-live="polite">
                    <div class="flex items-center gap-xs text-error font-semibold">
                      <span class="material-symbols-outlined">warning</span>
                      <span>Import Validation Blockers ({validationErrors.length})</span>
                    </div>
                    <ul class="list-disc pl-lg text-body-sm text-on-error-container space-y-1">
                      {#each validationErrors as err}
                        <li>{err}</li>
                      {/each}
                    </ul>
                  </div>
                {/if}

                {#if importSuccess}
                  <div class="p-md bg-green-100 border border-green-300 text-green-800 rounded-xl flex items-center gap-sm shadow-sm" role="status" aria-live="polite">
                    <span class="material-symbols-outlined text-green-700">check_circle</span>
                    <div>
                      <p class="font-label-md text-label-md font-bold">Campaign Successfully Configured!</p>
                      <p class="text-body-sm text-green-700">Ingested {parsedLeads.length} verified leads. Launching outreach...</p>
                    </div>
                  </div>
                {/if}

                <div class="flex justify-end pt-sm">
                  <button
                    onclick={submitCampaign}
                    class="px-lg py-sm bg-primary text-on-primary rounded-xl font-label-md text-label-md hover:brightness-110 active:scale-95 transition-all shadow-md focus-visible:ring-2 focus-visible:ring-primary focus-visible:outline-none flex items-center gap-sm"
                    disabled={parsedLeads.length === 0}
                    class:opacity-50={parsedLeads.length === 0}
                  >
                    <span class="material-symbols-outlined" aria-hidden="true">rocket_launch</span>
                    Finalize & Launch Outreach
                  </button>
                </div>
              </div>

            </div>
          </div>
        </section>
      {/if}

      <!-- Tab 3: Template Editor View -->
      {#if activeTab === 'template-editor'}
        <section aria-label="Spintax Template Configuration">
          <div class="mb-xl">
            <p class="font-body-md text-body-md text-on-surface-variant">Message Randomizer Engine (FEAT-CMP-02)</p>
            <h3 class="font-headline-xl text-headline-xl text-on-surface">Spintax Template Configuration</h3>
          </div>

          <div class="grid grid-cols-1 lg:grid-cols-12 gap-gutter items-start">
            <!-- Left inputs & helpers -->
            <div class="lg:col-span-6 bg-surface-container-lowest border border-outline-variant p-md rounded-xl shadow-sm space-y-md">
              <div class="flex justify-between items-center border-b border-outline-variant pb-xs">
                <h4 class="font-headline-md text-headline-md text-on-surface">Template Text</h4>
                <button
                  onclick={forceReroll}
                  class="text-primary font-label-md text-label-md flex items-center gap-xs hover:underline focus-visible:ring-2 focus-visible:ring-primary animate-pulse"
                  aria-label="Generate new preview variants"
                >
                  <span class="material-symbols-outlined text-[18px]">sync</span>
                  Re-roll Previews
                </button>
              </div>

              <div class="space-y-xs">
                <label for="spintax-textarea" class="block font-label-md text-label-md text-on-surface-variant">Spintax Message Template</label>
                <textarea
                  id="spintax-textarea"
                  bind:value={spintaxTemplate}
                  rows="6"
                  placeholder="Enter spintax syntax here, e.g. {'{Hi|Hello|Hey}'}"
                  class="w-full px-md py-sm border border-outline-variant rounded-xl bg-surface text-on-surface font-body-md text-body-md focus-visible:ring-2 focus-visible:ring-primary focus-visible:outline-none"
                ></textarea>
              </div>

              <!-- Quick Insert Spintax Helpers -->
              <div class="space-y-sm">
                <p class="font-label-md text-label-md text-on-surface-variant">Quick Insert Spintax Templates</p>
                <div class="flex flex-wrap gap-xs">
                  {#each spintaxHelpers as help}
                    <button
                      onclick={() => insertHelper(help)}
                      class="px-md py-sm bg-surface-container-low hover:bg-surface-container-highest border border-outline-variant text-on-surface font-body-sm text-body-sm rounded-lg transition-colors focus-visible:ring-2 focus-visible:ring-primary focus-visible:outline-none"
                    >
                      {help}
                    </button>
                  {/each}
                </div>
              </div>

              <div class="p-sm bg-primary-container/10 border border-primary/20 rounded-xl space-y-xs">
                <div class="flex items-center gap-xs text-primary font-semibold text-body-md">
                  <span class="material-symbols-outlined">info</span>
                  <span>How Spintax Works</span>
                </div>
                <p class="text-body-sm text-on-surface-variant leading-relaxed">
                  Sections wrapped in curly braces <code class="text-primary">{`{...}`}</code> and separated by a pipe character <code class="text-primary">|</code> represent randomized paths. The system randomly selects one of the options for each prospect outbound conversation to bypass Telegram's automated spam filters.
                </p>
              </div>
            </div>

            <!-- Right Live Preview panel (Telegram chat mock and lists of options) -->
            <div class="lg:col-span-6 space-y-md">
              <!-- Mock Telegram Phone Preview -->
              <div class="bg-[#eef2f7] border border-outline-variant rounded-2xl overflow-hidden shadow-md max-w-sm mx-auto flex flex-col h-[350px]">
                <!-- Telegram App Header -->
                <div class="bg-primary py-sm px-md flex items-center gap-md text-on-primary">
                  <span class="material-symbols-outlined text-[20px]">arrow_back</span>
                  <div class="w-8 h-8 rounded-full bg-white/20 flex items-center justify-center font-bold text-sm">
                    AI
                  </div>
                  <div class="flex-1">
                    <p class="font-label-md text-label-md font-bold leading-tight">LeadGen Bot AI</p>
                    <p class="text-[10px] text-primary-fixed opacity-90 leading-tight">typing...</p>
                  </div>
                  <span class="material-symbols-outlined text-[20px]">more_vert</span>
                </div>

                <!-- Chat Body (Background pattern simulated by background colors) -->
                <div class="flex-1 p-md flex flex-col justify-end space-y-md overflow-y-auto">
                  <div class="bg-white p-md rounded-xl rounded-tl-none text-body-sm text-on-surface max-w-[85%] self-start shadow-sm border border-neutral-200">
                    <p class="font-bold text-primary text-[10px] uppercase mb-xs tracking-wider">AI Dialogue Engine Preview</p>
                    <p class="whitespace-pre-line text-body-md leading-relaxed">{singlePreview || 'No template content entered yet.'}</p>
                    <p class="text-[9px] text-on-surface-variant text-right mt-xs">11:42 AM</p>
                  </div>
                </div>
              </div>

              <!-- List of Alternative Random Variations -->
              <div class="bg-surface-container-lowest border border-outline-variant p-md rounded-xl shadow-sm space-y-sm">
                <h4 class="font-headline-md text-headline-md text-on-surface border-b border-outline-variant pb-xs">Alternative Variations Preview</h4>
                <div class="space-y-sm max-h-[160px] overflow-y-auto pr-xs">
                  {#each alternativePreviews as preview, idx}
                    <div class="p-sm bg-surface rounded-lg border border-outline-variant text-body-sm text-on-surface-variant relative">
                      <span class="absolute top-sm right-sm bg-surface-container-highest text-on-surface-variant font-bold text-[9px] px-[4px] rounded">Variant {idx + 1}</span>
                      <p class="whitespace-pre-line leading-relaxed pr-md">{preview}</p>
                    </div>
                  {/each}
                </div>
              </div>
            </div>
          </div>
        </section>
      {/if}

    </main>
  </div>
</div>
