<script>
  import * as R from 'ramda';
  import { quill } from './quill';

  import * as MD from './markdown';
  import Turndown from 'turndown';
  import Style from '@Component/text-editor/style.svelte';
  import Input from '@Component/Input/Input.svelte';

  const turndownService = new Turndown();
  turndownService.keep(['sub', 'sup', 'u']);

  export let id;
  export let required;
  export let label = '';
  export let disabled = false;
  export let name = '';

  export let model = '';
  export let lens = R.identity;

  export let parse = R.identity;
  export let format = R.identity;
  export let validators = [];
  export let warnValidators = [];

  export let compact;
  export let i18n;

  export let toolbar = [
    [{ header: [1, 2, 3, false] }],
    [{ list: 'ordered' }, { list: 'bullet' }],
    [{ script: 'sub' }, { script: 'super' }],
    ['bold', 'italic', 'underline'],
    ['link'],
    ['clean']
  ];

  const toMarkdown = R.bind(turndownService.turndown, turndownService);
</script>

<Input
  {id}
  {name}
  {required}
  {disabled}
  {label}
  {parse}
  {format}
  {validators}
  {warnValidators}
  {compact}
  {i18n}
  {lens}
  bind:model
  let:viewValue
  let:api>
  <Style>
    {#if !disabled}
      <div
        on:focusin={api.focus}
        on:editor-focus-out={event => api.blur(toMarkdown(event.detail.html))}
        on:text-change={event => api.input(toMarkdown(event.detail.html))}
        use:quill={{html: MD.toHtml(viewValue), toolbar}} />
    {:else}
      {@html MD.toHtml(viewValue)}
    {/if}
  </Style>
</Input>
