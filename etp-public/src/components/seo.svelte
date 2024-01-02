<script>
  export let title;
  export let noindex = false;
  export let nofollow = false;
  export let descriptionFi;
  export let descriptionSv;

  $: {
    document.title = title;

    if (descriptionSv) document.documentElement.setAttribute('lang', 'sv');
    if (descriptionFi) document.documentElement.setAttribute('lang', 'fi');
  }
</script>

<svelte:head>
  {#if title}
    <title>{title}</title>
  {/if}

  <meta
    name="robots"
    content={`${noindex ? 'noindex' : 'index'},${
      nofollow ? 'nofollow' : 'follow'
    }`} />
  <meta
    name="googlebot"
    content={`${noindex ? 'noindex' : 'index'},${
      nofollow ? 'nofollow' : 'follow'
    }`} />

  {#if descriptionFi}
    <meta name="description" lang="fi" content={descriptionFi} />
  {/if}
  {#if descriptionSv}
    <meta name="description" lang="sv" content={descriptionSv} />
  {/if}
</svelte:head>
