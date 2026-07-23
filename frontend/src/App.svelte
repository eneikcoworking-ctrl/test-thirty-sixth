<script>
  import SpintaxEditor from './components/SpintaxEditor.svelte';
  import CsvUploader from './components/CsvUploader.svelte';

  // Mock Campaigns database matching model/schema
  let campaigns = [
    { id: 1, name: "Summer Solstice Promo", status: "ACTIVE", createdAt: "2026-07-23T18:00:00Z", updatedAt: "2026-07-23T18:00:00Z" },
    { id: 2, name: "Q3 Core Tech Outbound", status: "DRAFT", createdAt: "2026-07-23T18:10:00Z", updatedAt: "2026-07-23T18:10:00Z" },
    { id: 3, name: "Referral Rewards Campaign", status: "PAUSED", createdAt: "2026-07-23T18:20:00Z", updatedAt: "2026-07-23T18:20:00Z" }
  ];

  let selectedCampaignId = 1;
  let activeTab = "campaigns"; // campaigns, uploads, templates, review
  let totalUploadedLeadsCount = 1402; // initial stats
  let errorRate = "0.04%";

  $: selectedCampaign = campaigns.find(c => c.id === selectedCampaignId) || campaigns[0];

  // Mock API Save handler for Spintax template
  async function saveSpintaxTemplate(text) {
    console.log(`API Call: PUT /api/campaigns/${selectedCampaignId}/spintax with templateText:`, text);
    // Simulate API network roundtrip
    await new Promise(resolve => setTimeout(resolve, 800));
    return {
      success: true,
      message: "Template updated successfully"
    };
  }

  function handleIngestSuccess() {
    // Increment the active uploaded stats when a lead list ingestion completes
    totalUploadedLeadsCount += 128; // increment nicely to reflect progress
  }

  function changeCampaign(id) {
    selectedCampaignId = id;
  }
</script>

<!-- TopAppBar -->
<header class="bg-surface-container-lowest w-full top-0 sticky z-40 border-b border-outline-variant flex items-center justify-between px-6 h-16 shadow-sm">
  <div class="flex items-center gap-4">
    <button
      class="text-primary hover:bg-surface-container-low transition-colors active:scale-95 duration-100 p-2 rounded-full focus:ring-2 focus:ring-primary/20 focus:outline-none"
      aria-label="Toggle navigation menu"
    >
      <span class="material-symbols-outlined text-[24px] block">menu</span>
    </button>
    <div class="flex items-center gap-2">
      <span class="material-symbols-outlined text-primary text-[28px]">rocket_launch</span>
      <h1 class="font-headline-lg font-bold text-primary tracking-tight">Campaign Command Center</h1>
    </div>
  </div>

  <div class="flex items-center gap-4">
    <!-- Quick Status Mode Indicator -->
    <div class="hidden sm:flex items-center gap-1.5 px-3 py-1 bg-green-50 text-green-700 rounded-full border border-green-200 text-xs font-semibold">
      <span class="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse"></span>
      System Live
    </div>

    <!-- User Avatar -->
    <button
      class="w-10 h-10 rounded-full overflow-hidden border border-outline-variant active:scale-95 duration-100 transition-transform focus:ring-2 focus:ring-primary/50 focus:outline-none"
      aria-label="User profile settings"
    >
      <img
        class="w-full h-full object-cover"
        alt="A professional campaign manager avatar"
        src="https://lh3.googleusercontent.com/aida-public/AB6AXuA6iaz4JHQ7pCQHq3onGjT2-gDMPUdqxo3AyIFD70cTOAyd1i4lTTOHF1oYbGGA1nbhEs2WIc3EZnSyY55BgpsD0Bomn2QKQOYd2vccSmOcDYUzir4oCrpHrOSy2CrWt3dMPeJgTqgHZDf9ToV-QYYKZX0RLwzHZl8BBFPsUG2JwjWkimXysZNDOAvaWsN0bmm5RD05qov--Kre8IGIAu-PSLrXUOUvk95O11j-IZRNR8posZR_pM1-krr2CpAWHOVSNTpAxOu4lhA"
      />
    </button>
  </div>
</header>

<main class="max-w-screen-md mx-auto px-4 sm:px-6 pt-6 pb-32">
  <!-- Summary Header -->
  <section class="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6 gap-4" aria-labelledby="dashboard-title">
    <div>
      <h2 id="dashboard-title" class="font-display-lg text-3xl font-extrabold text-on-surface tracking-tight mb-1">
        Campaign Configuration
      </h2>
      <p class="text-sm text-on-surface-variant font-medium">
        Configure message templates and ingest target lead lists.
      </p>
    </div>

    <!-- Active Campaign Switcher Dropdown (Accessible) -->
    <div class="flex flex-col sm:items-end gap-1.5 w-full sm:w-auto">
      <label for="campaign-selector" class="text-xs font-bold text-on-surface-variant uppercase tracking-wider">
        Active Campaign Context
      </label>
      <select
        id="campaign-selector"
        value={selectedCampaignId}
        on:change={(e) => changeCampaign(parseInt(e.target.value))}
        class="w-full sm:w-64 rounded-lg border border-outline-variant bg-surface-container-lowest p-2.5 text-sm font-semibold text-primary focus:border-primary focus:ring-2 focus:ring-primary/20 focus:outline-none shadow-sm cursor-pointer"
      >
        {#each campaigns as camp}
          <option value={camp.id}>
            {camp.name} ({camp.status})
          </option>
        {/each}
      </select>
    </div>
  </section>

  <!-- Metrics Bento Grid -->
  <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
    <div class="p-4 bg-surface-container-lowest border border-outline-variant rounded-xl shadow-sm">
      <p class="text-[10px] font-bold text-on-surface-variant uppercase tracking-wider mb-1">Active Clusters</p>
      <p class="text-xl font-bold text-primary font-mono">24</p>
    </div>
    <div class="p-4 bg-surface-container-lowest border border-outline-variant rounded-xl shadow-sm">
      <p class="text-[10px] font-bold text-on-surface-variant uppercase tracking-wider mb-1">Ingested Leads</p>
      <p class="text-xl font-bold text-primary font-mono">{totalUploadedLeadsCount.toLocaleString()}</p>
    </div>
    <div class="p-4 bg-surface-container-lowest border border-outline-variant rounded-xl shadow-sm">
      <p class="text-[10px] font-bold text-on-surface-variant uppercase tracking-wider mb-1">Daily Dispatch Cap</p>
      <p class="text-xl font-bold text-primary font-mono">15-20 / acc</p>
    </div>
    <div class="p-4 bg-surface-container-lowest border border-outline-variant rounded-xl shadow-sm">
      <p class="text-[10px] font-bold text-on-surface-variant uppercase tracking-wider mb-1">System Error Rate</p>
      <p class="text-xl font-bold text-error font-mono">{errorRate}</p>
    </div>
  </div>

  <!-- Dynamic Active Section Tabs -->
  <div class="space-y-6">
    <!-- Active Campaign Context Summary Card -->
    <div class="p-4 bg-primary text-white rounded-2xl shadow-md relative overflow-hidden flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
      <div class="relative z-10 space-y-1">
        <div class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-white/10 border border-white/20 text-[10px] font-bold uppercase tracking-wider">
          {selectedCampaign.status}
        </div>
        <h3 class="text-xl font-extrabold tracking-tight">{selectedCampaign.name}</h3>
        <p class="text-xs text-primary-fixed-dim font-medium">
          Created: {new Date(selectedCampaign.createdAt).toLocaleDateString()}
        </p>
      </div>
      <div class="flex gap-2 relative z-10 shrink-0">
        <button
          type="button"
          class="bg-white/10 hover:bg-white/20 text-white border border-white/20 text-xs font-bold py-2 px-4 rounded-lg transition-colors focus:ring-2 focus:ring-white/50"
        >
          PAUSE CAMPAIGN
        </button>
        <button
          type="button"
          class="bg-white text-primary text-xs font-extrabold py-2 px-4 rounded-lg transition-colors hover:brightness-110 focus:ring-2 focus:ring-white/50"
        >
          LAUNCH DISPATCH
        </button>
      </div>
      <!-- Background pattern symbol -->
      <span class="absolute -right-8 -bottom-8 opacity-10 material-symbols-outlined text-[120px] select-none pointer-events-none">
        insights
      </span>
    </div>

    <!-- Tab Section 1: Spintax Configuration -->
    <SpintaxEditor
      campaignId={selectedCampaign.id}
      onSave={saveSpintaxTemplate}
    />

    <!-- Tab Section 2: CSV Lead Ingestion Upload -->
    <CsvUploader
      campaignId={selectedCampaign.id}
      onIngestSuccess={handleIngestSuccess}
    />
  </div>
</main>

<!-- Bottom Navigation Bar matching mockup styling and placement -->
<nav class="fixed bottom-0 left-0 right-0 w-full z-50 bg-surface-container border-t border-outline-variant flex justify-around items-center h-20 px-4 pb-safe shadow-lg">
  <!-- Overview Tab -->
  <button
    type="button"
    on:click={() => activeTab = "overview"}
    class="flex flex-col items-center justify-center p-2 rounded-xl transition-all focus:ring-2 focus:ring-primary/20 focus:outline-none
      {activeTab === 'overview' ? 'bg-primary-container text-white font-extrabold px-5' : 'text-on-surface-variant hover:text-primary'}"
  >
    <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' {activeTab === 'overview' ? 1 : 0};">dashboard</span>
    <span class="text-[10px] tracking-wide font-medium mt-0.5">Overview</span>
  </button>

  <!-- Campaigns Tab -->
  <button
    type="button"
    on:click={() => activeTab = "campaigns"}
    class="flex flex-col items-center justify-center p-2 rounded-xl transition-all focus:ring-2 focus:ring-primary/20 focus:outline-none
      {activeTab === 'campaigns' ? 'bg-primary-container text-white font-extrabold px-5' : 'text-on-surface-variant hover:text-primary'}"
  >
    <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' {activeTab === 'campaigns' ? 1 : 0}; font-variation-settings: 'FILL' 1;">rocket_launch</span>
    <span class="text-[10px] tracking-wide font-medium mt-0.5">Campaigns</span>
  </button>

  <!-- Uploads Tab -->
  <button
    type="button"
    on:click={() => activeTab = "uploads"}
    class="flex flex-col items-center justify-center p-2 rounded-xl transition-all focus:ring-2 focus:ring-primary/20 focus:outline-none
      {activeTab === 'uploads' ? 'bg-primary-container text-white font-extrabold px-5' : 'text-on-surface-variant hover:text-primary'}"
  >
    <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' {activeTab === 'uploads' ? 1 : 0};">cloud_upload</span>
    <span class="text-[10px] tracking-wide font-medium mt-0.5">Uploads</span>
  </button>

  <!-- Settings Tab -->
  <button
    type="button"
    on:click={() => activeTab = "settings"}
    class="flex flex-col items-center justify-center p-2 rounded-xl transition-all focus:ring-2 focus:ring-primary/20 focus:outline-none
      {activeTab === 'settings' ? 'bg-primary-container text-white font-extrabold px-5' : 'text-on-surface-variant hover:text-primary'}"
  >
    <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' {activeTab === 'settings' ? 1 : 0};">settings</span>
    <span class="text-[10px] tracking-wide font-medium mt-0.5">Settings</span>
  </button>
</nav>
