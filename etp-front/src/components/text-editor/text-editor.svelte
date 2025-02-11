<script>
  import * as R from 'ramda';
  import { quill } from './quill';

  import * as keys from '@Utility/keys';
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
  export let clearContent = false;

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

  const keyboard = {
    bindings: {
      tab: {
        key: keys.TAB,
        handler: R.always(true)
      },
      'remove tab': {
        key: keys.TAB,
        shiftKey: true,
        collapsed: true,
        prefix: /\t$/,
        handler: R.always(true)
      }
    }
  };

  const toMarkdown = R.bind(turndownService.turndown, turndownService);

  let editorElement = null;

  const onEditorSetup = editor => {
    editorElement = editor;
  };

  const setValid = (editorElement, valid) => {
    editorElement?.setAttribute('aria-invalid', valid ? 'false' : 'true');
  };

  let valid = true;
  $: setValid(editorElement, valid);

  let previousContent = '';

  const handleFocusOut = (event, viewValue, api) => {
    const html = event.detail.html;

    const markdown = toMarkdown(html);
    previousContent = markdown;

    if (markdown && markdown !== viewValue) {
      api.blur(markdown);
    }
  };

  // Watch for clearContent prop changes specifically
  $: if (clearContent) {
    const editor = document.querySelector('.ql-editor');
    if (editor && editor.innerHTML !== '') {
      editor.innerHTML = '';
      previousContent = '';
      clearContent = false;
    }
  }
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
  bind:valid
  let:viewValue
  let:api>
  <Style>
    {#if !disabled}
      <div
        on:focusin={api.focus}
        on:text-change={event => {
          const markdown = toMarkdown(event.detail.html);
          if (markdown !== previousContent) {
            api.input(markdown);
            previousContent = markdown;
          }
        }}
        on:editor-focus-out={event => handleFocusOut(event, viewValue, api)}
        use:quill={{
          html: MD.toHtml(viewValue),
          toolbar,
          keyboard,
          id,
          required,
          onEditorSetup
        }} />
    {:else}
      {@html MD.toHtml(viewValue)}
    {/if}
  </Style>
</Input>
