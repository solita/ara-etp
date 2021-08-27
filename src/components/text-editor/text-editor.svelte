<script>
  import * as R from 'ramda';
  import DOMPurify from 'dompurify';

  import { quill } from './quill';
  import Marked from 'marked';
  import Turndown from 'turndown';
  import Style from '@Component/text-editor/style.svelte';
  import Input from '@Component/Input/Input.svelte';

  const turndownService = new Turndown();

  turndownService.keep(['sub', 'sup', 'u']);

  Marked.use({
    tokenizer: {
      url: _ => null
    }
  });

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
        on:focusout={event => api.blur(toMarkdown(event.target.innerHTML))}
        on:text-change={event => api.input(toMarkdown(event.detail.html))}
        use:quill={{html: Marked(viewValue), toolbar}} />
    {:else}
      {@html DOMPurify.sanitize(Marked(viewValue))}
    {/if}
  </Style>
</Input>
