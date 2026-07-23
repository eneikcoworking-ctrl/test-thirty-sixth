<script context="module">
  /**
   * Helper to recursively spin template string
   */
  export function parseSpintax(text) {
    if (!text) return "";
    let spun = text;
    const regex = /\{([^{}]+)\}/g;
    let iterations = 0;
    // Limit iterations to prevent infinite loop on malformed regex
    while (regex.test(spun) && iterations < 100) {
      spun = spun.replace(regex, (match, choicesText) => {
        const choices = choicesText.split('|');
        const randomChoice = choices[Math.floor(Math.random() * choices.length)];
        return randomChoice;
      });
      iterations++;
    }
    return spun;
  }

  /**
   * Helper to count the total variations possible in the spintax template
   */
  export function countSpintaxVariations(text) {
    if (!text || !text.includes('{')) return 1;
    let temp = text;
    const regex = /\{([^{}]+)\}/g;
    let values = [];
    let iterations = 0;
    while (regex.test(temp) && iterations < 100) {
      temp = temp.replace(regex, (match, choicesText) => {
        const count = choicesText.split('|').length;
        values.push(count);
        return 'X';
      });
      iterations++;
    }
    return values.reduce((acc, curr) => acc * curr, 1);
  }
</script>

<script>
  export let templateText = "{Hi|Hello|Hey} lead, we would love to connect! Check {our link|the website}!";
  export let campaignId = 1;
  export let onSave = async () => {};

  let previewText = "";
  let variationsCount = 1;
  let validationError = "";
  let isSaving = false;
  let saveSuccess = false;

  // Reactively calculate preview and variations when templateText changes
  $: {
    // Basic bracket mismatch validation
    const openBrackets = (templateText.match(/\{/g) || []).length;
    const closeBrackets = (templateText.match(/\}/g) || []).length;
    if (openBrackets !== closeBrackets) {
      validationError = `Mismatched brackets: ${openBrackets} open vs ${closeBrackets} close.`;
    } else {
      validationError = "";
    }

    previewText = parseSpintax(templateText);
    variationsCount = countSpintaxVariations(templateText);
  }

  function reRollPreview() {
    previewText = parseSpintax(templateText);
  }

  async function handleSave() {
    if (validationError) return;
    isSaving = true;
    saveSuccess = false;
    try {
      await onSave(templateText);
      saveSuccess = true;
      setTimeout(() => {
        saveSuccess = false;
      }, 3000);
    } catch (err) {
      console.error(err);
    } finally {
      isSaving = false;
    }
  }
</script>

<section class="bg-surface-container-lowest border border-outline-variant rounded-xl p-6 shadow-sm mb-8" aria-labelledby="spintax-heading">
  <div class="flex justify-between items-center mb-4 border-b border-outline-variant/30 pb-3">
    <div class="flex items-center gap-2">
      <span class="material-symbols-outlined text-primary text-[24px]">description</span>
      <h2 id="spintax-heading" class="font-headline-sm text-headline-sm text-on-surface">Spintax Template Engine</h2>
    </div>
    <span class="text-xs font-semibold px-2 py-1 rounded bg-surface-container-low text-on-surface-variant font-mono">
      Campaign ID: #{campaignId}
    </span>
  </div>

  <div class="space-y-4">
    <!-- Template Textarea -->
    <div>
      <label for="spintax-template" class="block font-label-md text-on-surface-variant mb-2">
        Outreach Template (Spintax supported, e.g. <code class="font-mono text-xs bg-surface-container-low px-1 rounded">&#123;Hi|Hello|Hey&#125;</code>)
      </label>
      <textarea
        id="spintax-template"
        bind:value={templateText}
        rows="4"
        class="w-full rounded-lg border border-outline-variant bg-surface-bright p-3 text-sm text-on-surface placeholder:text-outline focus:border-primary focus:ring-2 focus:ring-primary/20 focus:outline-none transition-shadow"
        placeholder="Enter template here... e.g. &#123;Hi|Hello|Hey&#125; check our new promo at &#123;link|url&#125;!"
        aria-describedby="spintax-help"
      ></textarea>

      {#if validationError}
        <p class="text-xs text-error font-medium mt-1 flex items-center gap-1">
          <span class="material-symbols-outlined text-[14px]">error</span>
          {validationError}
        </p>
      {:else}
        <p id="spintax-help" class="text-xs text-on-surface-variant mt-1">
          Each set of curly braces <code class="font-mono bg-surface-container-low px-1 rounded">&#123;a|b&#125;</code> specifies options separated by vertical bars.
        </p>
      {/if}
    </div>

    <!-- Live Preview Display -->
    <div class="bg-surface-container-low rounded-xl p-4 border border-outline-variant/50 relative">
      <div class="flex justify-between items-center mb-3">
        <h3 class="text-xs font-bold text-primary tracking-wider uppercase flex items-center gap-1">
          <span class="material-symbols-outlined text-[16px]">visibility</span>
          Live Preview
        </h3>
        <div class="flex items-center gap-2">
          <span class="text-[11px] text-on-surface-variant font-medium">
            Unique Combinations: <strong>{variationsCount.toLocaleString()}</strong>
          </span>
          <button
            type="button"
            on:click={reRollPreview}
            class="flex items-center gap-1 text-xs text-primary font-bold hover:bg-surface-container-high px-2 py-1 rounded transition-colors active:scale-95 border border-primary/20 bg-surface-container-lowest focus:ring-2 focus:ring-primary/20 focus:outline-none"
            title="Generate another random variation of this template"
            aria-label="Generate new spintax preview variation"
          >
            <span class="material-symbols-outlined text-[16px]">refresh</span>
            Spin Again
          </button>
        </div>
      </div>

      <div class="bg-surface-bright rounded border border-outline-variant/20 p-3 min-h-[64px] text-sm text-on-surface whitespace-pre-wrap leading-relaxed">
        {previewText || "Template is empty. Start typing to see live preview."}
      </div>
    </div>

    <!-- Save Action Panel -->
    <div class="flex justify-end items-center gap-4 pt-2">
      {#if saveSuccess}
        <span class="text-xs text-green-600 font-bold flex items-center gap-1" role="status">
          <span class="material-symbols-outlined text-[16px]">check_circle</span>
          Spintax configuration saved successfully!
        </span>
      {/if}
      <button
        type="button"
        on:click={handleSave}
        disabled={isSaving || !!validationError}
        class="bg-primary text-on-primary font-label-md text-label-md py-2.5 px-5 rounded-lg flex items-center gap-2 hover:brightness-110 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-md focus:ring-2 focus:ring-primary/50 focus:outline-none"
      >
        {#if isSaving}
          <span class="material-symbols-outlined animate-spin text-[18px]">progress_activity</span>
          SAVING...
        {:else}
          <span class="material-symbols-outlined text-[18px]">save</span>
          SAVE TEMPLATE
        {/if}
      </button>
    </div>
  </div>
</section>
