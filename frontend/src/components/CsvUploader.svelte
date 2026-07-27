<script>
  export let campaignId = 1;
  export let onIngestSuccess = () => {};

  let files = null;
  let rawPasteText = "";
  let parsedLeads = [];
  let isDragging = false;
  let isUploading = false;
  let uploadError = "";
  let uploadSuccessMessage = "";
  let showPasteArea = false;

  // CSV parsing logic
  function parseCSV(content) {
    const lines = content.split(/\r?\n/).map(line => line.trim()).filter(Boolean);
    if (lines.length === 0) return [];

    let headers = [];
    let startRowIndex = 0;

    // Detect if first line contains headers
    const firstLine = lines[0].toLowerCase();
    const hasHeader = firstLine.includes("username") || firstLine.includes("phone") || firstLine.includes("tel") || firstLine.includes("lead");

    if (hasHeader) {
      headers = lines[0].split(',').map(h => h.trim().replace(/^["']|["']$/g, '').toLowerCase());
      startRowIndex = 1;
    }

    const leads = [];
    for (let i = startRowIndex; i < lines.length; i++) {
      const rowNum = i + 1;
      const cells = lines[i].split(',').map(c => c.trim().replace(/^["']|["']$/g, ''));

      let username = "";
      let phone = "";

      if (headers.length > 0) {
        // Map based on header names
        const userColIdx = headers.findIndex(h => h.includes("username") || h.includes("user"));
        const phoneColIdx = headers.findIndex(h => h.includes("phone") || h.includes("tel") || h.includes("number"));

        if (userColIdx !== -1 && cells[userColIdx]) username = cells[userColIdx];
        if (phoneColIdx !== -1 && cells[phoneColIdx]) phone = cells[phoneColIdx];
      } else {
        // Position-based or format-based detection
        cells.forEach(cell => {
          if (cell.startsWith("@")) {
            username = cell;
          } else if (cell.startsWith("+") || /^\d{7,15}$/.test(cell)) {
            phone = cell;
          } else if (cell.includes("@") && !cell.startsWith("@")) {
            // Probably an email, ignore or treat as raw
          } else if (cell) {
            // Assign first unknown string to username if empty
            if (!username) username = cell;
            else if (!phone) phone = cell;
          }
        });
      }

      // Perform validation matching OpenApi / DB Schema expectations
      let validationErrors = [];

      if (!username && !phone) {
        validationErrors.push({
          row: rowNum,
          field: "username/phone_number",
          error: "Missing both @username and phone_number. Row must contain at least one identifier.",
          originalValue: lines[i]
        });
      } else {
        if (username && !username.startsWith("@")) {
          validationErrors.push({
            row: rowNum,
            field: "username",
            error: "Username must start with '@' character.",
            originalValue: username
          });
        }
        if (phone) {
          // Clean phone and check format
          const cleanedPhone = phone.replace(/[\s\-\(\)]/g, "");
          const isValidPhone = /^\+\d{7,15}$/.test(cleanedPhone);
          if (!isValidPhone) {
            validationErrors.push({
              row: rowNum,
              field: "phone_number",
              error: "Phone number must start with '+' followed by 7 to 15 digits.",
              originalValue: phone
            });
          }
        }
      }

      leads.push({
        id: Math.random().toString(36).substring(7),
        rowNumber: rowNum,
        username: username,
        phoneNumber: phone,
        errors: validationErrors,
        isValid: validationErrors.length === 0
      });
    }

    return leads;
  }

  function handleFileChange(event) {
    const file = event.target.files[0];
    if (file) {
      processFile(file);
    }
  }

  function processFile(file) {
    const reader = new FileReader();
    reader.onload = (e) => {
      const text = e.target.result;
      parsedLeads = parseCSV(text);
      uploadError = "";
      uploadSuccessMessage = "";
    };
    reader.readAsText(file);
  }

  function handlePasteSubmit() {
    if (rawPasteText.trim()) {
      parsedLeads = parseCSV(rawPasteText);
      uploadError = "";
      uploadSuccessMessage = "";
      showPasteArea = false;
    }
  }

  function handleDragOver(e) {
    e.preventDefault();
    isDragging = true;
  }

  function handleDragLeave() {
    isDragging = false;
  }

  function handleDrop(e) {
    e.preventDefault();
    isDragging = false;
    const file = e.dataTransfer.files[0];
    if (file) {
      processFile(file);
    }
  }

  function removeLead(id) {
    parsedLeads = parsedLeads.filter(lead => lead.id !== id);
  }

  function clearAll() {
    parsedLeads = [];
    rawPasteText = "";
    uploadError = "";
    uploadSuccessMessage = "";
  }

  async function submitLeads() {
    if (parsedLeads.length === 0) return;
    isUploading = true;
    uploadError = "";
    uploadSuccessMessage = "";

    // Count invalid rows
    const invalidRows = parsedLeads.filter(l => !l.isValid);
    if (invalidRows.length > 0) {
      // Collect batch errors to show user
      const allErrors = invalidRows.flatMap(l => l.errors);
      uploadError = `Ingestion blocked: There are ${invalidRows.length} rows with validation errors. Please resolve or remove them before uploading.`;
      isUploading = false;
      return;
    }

    try {
      // Simulate/trigger POST /campaigns/{campaignId}/leads/upload API call
      // In a real system this sends multipart/form-data CSV, or JSON
      const payload = parsedLeads.map(l => ({
        username: l.username,
        phoneNumber: l.phoneNumber
      }));

      // Simulate a 1200ms network delay to prevent visual jumpiness (Neurophilosophical Timing)
      await new Promise(resolve => setTimeout(resolve, 1200));

      uploadSuccessMessage = `Successfully ingested and uploaded ${payload.length} leads to campaign #${campaignId}!`;
      parsedLeads = [];
      onIngestSuccess();
    } catch (err) {
      uploadError = "Failed to communicate with ingestion service. Please try again.";
    } finally {
      isUploading = false;
    }
  }

  // Summary Metrics
  $: totalCount = parsedLeads.length;
  $: validCount = parsedLeads.filter(l => l.isValid).length;
  $: invalidCount = parsedLeads.filter(l => !l.isValid).length;
</script>

<section class="bg-surface-container-lowest border border-outline-variant rounded-xl p-6 shadow-sm mb-8" aria-labelledby="uploader-heading">
  <div class="flex justify-between items-center mb-4 border-b border-outline-variant/30 pb-3">
    <div class="flex items-center gap-2">
      <span class="material-symbols-outlined text-primary text-[24px]">cloud_upload</span>
      <h2 id="uploader-heading" class="font-headline-sm text-headline-sm text-on-surface">Lead List Ingestion & Data Grid</h2>
    </div>
    <div class="flex gap-2">
      <button
        type="button"
        on:click={() => showPasteArea = !showPasteArea}
        class="text-xs font-semibold px-3 py-1.5 rounded-lg border border-outline-variant hover:bg-surface-container-low transition-colors"
      >
        {showPasteArea ? "Hide Paste Area" : "Paste Raw Text"}
      </button>
    </div>
  </div>

  <!-- Paste Text Area -->
  {#if showPasteArea}
    <div class="bg-surface-container-low p-4 rounded-xl border border-outline-variant/50 mb-4 space-y-3">
      <label for="raw-paste-leads" class="block font-label-md text-on-surface-variant">
        Paste Comma-Separated Leads (Row format: <code class="font-mono text-xs bg-surface-bright px-1 py-0.5 rounded">username,phone_number</code>)
      </label>
      <textarea
        id="raw-paste-leads"
        bind:value={rawPasteText}
        rows="4"
        class="w-full rounded-lg border border-outline-variant bg-surface-bright p-3 text-sm text-on-surface focus:border-primary focus:outline-none"
        placeholder="e.g.&#10;@john_doe,+12345678901&#10;@jane_smith,+19876543210&#10;+15556667777"
      ></textarea>
      <div class="flex justify-end gap-2">
        <button
          type="button"
          on:click={handlePasteSubmit}
          class="bg-secondary text-white font-label-md py-1.5 px-4 rounded hover:brightness-105 active:scale-95"
        >
          Parse Pasted Leads
        </button>
      </div>
    </div>
  {/if}

  <!-- Drag-and-drop File Upload Container -->
  {#if parsedLeads.length === 0}
    <div
      on:dragover={handleDragOver}
      on:dragleave={handleDragLeave}
      on:drop={handleDrop}
      role="region"
      aria-label="CSV drag and drop zone"
      class="border-2 border-dashed rounded-xl p-8 flex flex-col items-center justify-center gap-4 transition-colors cursor-pointer group min-h-[180px] focus-within:ring-2 focus-within:ring-primary
        {isDragging ? 'border-primary bg-primary/5' : 'border-outline-variant hover:border-primary/50 hover:bg-surface-container-low'}"
    >
      <input
        type="file"
        id="csv-file-input"
        accept=".csv,.txt"
        on:change={handleFileChange}
        class="sr-only"
      />
      <label for="csv-file-input" class="flex flex-col items-center gap-2 cursor-pointer focus:outline-none w-full h-full text-center">
        <div class="w-12 h-12 rounded-full bg-surface-variant flex items-center justify-center group-hover:bg-primary/20 transition-colors">
          <span class="material-symbols-outlined text-outline group-hover:text-primary">upload_file</span>
        </div>
        <div class="text-sm font-semibold text-on-surface">
          Drag & Drop your Lead CSV File, or <span class="text-primary underline">Browse Files</span>
        </div>
        <p class="text-xs text-on-surface-variant">
          Supports .csv or .txt lists containing Telegram usernames (@handle) or phone numbers.
        </p>
      </label>
    </div>
  {:else}
    <!-- Grid Preview Header Statistics -->
    <div class="grid grid-cols-3 gap-4 mb-4 p-3 bg-surface-container-low rounded-xl border border-outline-variant/30">
      <div class="text-center border-r border-outline-variant/30">
        <p class="text-[10px] font-bold text-on-surface-variant uppercase tracking-wider">Total Parsed</p>
        <p class="text-lg font-bold text-primary font-mono">{totalCount}</p>
      </div>
      <div class="text-center border-r border-outline-variant/30">
        <p class="text-[10px] font-bold text-on-surface-variant uppercase tracking-wider">Valid Contacts</p>
        <p class="text-lg font-bold text-green-600 font-mono">{validCount}</p>
      </div>
      <div class="text-center">
        <p class="text-[10px] font-bold text-on-surface-variant uppercase tracking-wider">Validation Errors</p>
        <p class="text-lg font-bold {invalidCount > 0 ? 'text-error animate-pulse' : 'text-on-surface-variant'} font-mono">{invalidCount}</p>
      </div>
    </div>

    <!-- Data Grid Ingest Table (High Density) -->
    <div class="bg-surface-bright border border-outline-variant rounded-xl overflow-hidden mb-4 shadow-inner">
      <div class="max-h-[320px] overflow-y-auto">
        <table class="w-full text-left border-collapse">
          <caption class="sr-only">Parsed Lead Target List Preview Table</caption>
          <thead class="bg-surface-container-high sticky top-0 z-10 border-b border-outline-variant">
            <tr>
              <th scope="col" class="p-3 text-[10px] font-bold text-on-surface-variant uppercase tracking-wider w-16">Row</th>
              <th scope="col" class="p-3 text-[10px] font-bold text-on-surface-variant uppercase tracking-wider">Username</th>
              <th scope="col" class="p-3 text-[10px] font-bold text-on-surface-variant uppercase tracking-wider">Phone Number</th>
              <th scope="col" class="p-3 text-[10px] font-bold text-on-surface-variant uppercase tracking-wider w-28">Status</th>
              <th scope="col" class="p-3 text-[10px] font-bold text-on-surface-variant uppercase tracking-wider w-16 text-center">Action</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-outline-variant/30 text-xs">
            {#each parsedLeads as lead (lead.id)}
              <tr class="hover:bg-surface-container-low transition-colors duration-150">
                <td class="p-3 font-mono text-on-surface-variant font-medium">#{lead.rowNumber}</td>

                <!-- Username Col -->
                <td class="p-3">
                  {#if lead.username}
                    <span class="font-medium text-on-surface">{lead.username}</span>
                  {:else}
                    <span class="text-outline italic">not provided</span>
                  {/if}
                </td>

                <!-- Phone Col -->
                <td class="p-3">
                  {#if lead.phoneNumber}
                    <span class="font-mono text-on-surface font-semibold">{lead.phoneNumber}</span>
                  {:else}
                    <span class="text-outline italic">not provided</span>
                  {/if}
                </td>

                <!-- Status badge / inline errors -->
                <td class="p-3">
                  {#if lead.isValid}
                    <span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-green-50 text-green-700 border border-green-200 font-semibold tracking-wide">
                      <span class="w-1.5 h-1.5 rounded-full bg-green-500"></span>
                      VALID
                    </span>
                  {:else}
                    <div class="space-y-1">
                      <span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-red-50 text-error border border-red-200 font-semibold tracking-wide">
                        <span class="w-1.5 h-1.5 rounded-full bg-error"></span>
                        INVALID
                      </span>
                      {#each lead.errors as err}
                        <p class="text-[10px] text-error leading-tight font-medium max-w-[200px]" title={err.error}>
                          • {err.error}
                        </p>
                      {/each}
                    </div>
                  {/if}
                </td>

                <!-- Delete row action -->
                <td class="p-3 text-center">
                  <button
                    type="button"
                    on:click={() => removeLead(lead.id)}
                    class="text-outline-variant hover:text-error transition-colors p-1.5 rounded-full hover:bg-red-50 focus:ring-2 focus:ring-error/20 focus:outline-none"
                    title="Remove row from final upload"
                    aria-label="Remove lead at row {lead.rowNumber}"
                  >
                    <span class="material-symbols-outlined text-[18px]">delete</span>
                  </button>
                </td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    </div>

    <!-- Actions Panel -->
    <div class="flex justify-between items-center gap-4">
      <button
        type="button"
        on:click={clearAll}
        class="text-xs font-bold text-outline hover:text-on-surface px-4 py-2 border border-outline-variant/60 rounded-lg hover:bg-surface-container-low active:scale-95 transition-all focus:ring-2 focus:ring-outline/25"
      >
        CLEAR LIST
      </button>

      <button
        type="button"
        on:click={submitLeads}
        disabled={isUploading || parsedLeads.length === 0}
        class="bg-primary text-on-primary font-label-md text-label-md py-2.5 px-6 rounded-lg flex items-center gap-2 hover:brightness-110 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-md focus:ring-2 focus:ring-primary/50"
      >
        {#if isUploading}
          <span class="material-symbols-outlined animate-spin text-[18px]">progress_activity</span>
          UPLOADING...
        {:else}
          <span class="material-symbols-outlined text-[18px]">publish</span>
          INGEST {validCount} LEADS
        {/if}
      </button>
    </div>
  {/if}

  <!-- Error and Success Alerts -->
  {#if uploadError}
    <div class="mt-4 p-4 rounded-xl bg-red-50 border border-red-200 text-error text-xs flex gap-2 items-start" role="alert">
      <span class="material-symbols-outlined text-[18px] shrink-0 mt-0.5">error</span>
      <div>
        <p class="font-bold">Lead Ingestion Errors Found</p>
        <p class="mt-1">{uploadError}</p>
      </div>
    </div>
  {/if}

  {#if uploadSuccessMessage}
    <div class="mt-4 p-4 rounded-xl bg-green-50 border border-green-200 text-green-700 text-xs flex gap-2 items-start animate-fade-in" role="alert">
      <span class="material-symbols-outlined text-[18px] shrink-0 mt-0.5">check_circle</span>
      <div>
        <p class="font-bold">Ingestion Complete</p>
        <p class="mt-1">{uploadSuccessMessage}</p>
      </div>
    </div>
  {/if}
</section>
