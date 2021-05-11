<script>
  import * as R from 'ramda';
  import * as Either from '@Utility/either-utils';
  import * as Validation from '@Utility/validation';
  import Label from '@Component/Label/Label';

  import { quill } from './quill';
  import Marked from 'marked';
  import Turndown from 'turndown';
  import Style from '@Component/text-editor/style.svelte';

  const turndownService = new Turndown();

  turndownService.keep(['sub', 'sup', 'u']);

  Marked.use({
    tokenizer: {
      url: _ => null
    }
  });

  export let id;
  export let label = '';
  export let required = true;
  export let compact = false;

  export let model = { empty: 'fdsjafidjsiafds\n\nfdjsfpdjsipjfds\n' };
  export let lens = R.lensProp('empty');

  export let parse = R.identity;
  export let format = R.identity;
  export let validators = [];

  $: viewValue = R.compose(
    Either.orSome(viewValue),
    R.map(R.compose(Marked, format)),
    Either.fromValueOrEither,
    R.view(lens)
  )(model);

  let focused = false;
  let valid = true;
  let errorMessage = '';

  export let i18n = R.identity;

  $: highlightError = focused && !valid;

  $: validate = value =>
    Validation.validateModelValue(validators, value).cata(
      error => {
        valid = false;
        errorMessage = error(i18n);
      },
      () => (valid = true)
    );
</script>

<style type="text/postcss">
  .error-icon {
    @apply text-error;
  }

  .error-label {
    @apply absolute top-auto;
    font-size: smaller;
  }
</style>

<Label {id} {required} {label} {compact} error={highlightError} {focused} />
<Style>
  <div
    on:focusout={evt => {
      focused = false;
      model = R.set(
        lens,
        R.compose(
          parse,
          R.bind(turndownService.turndown, turndownService)
        )(evt.target.innerHTML),
        model
      );
    }}
    on:focusin={_ => (focused = true)}
    on:text-change={evt =>
      R.compose(
        validate,
        R.join(''),
        R.filter(R.test(/(\w|[ÅÄÖ])/i)),
        R.replace(/<\/?[^>]+(>|$)/g, ''),
        parse,
        R.bind(turndownService.turndown, turndownService)
      )(evt.detail.html)}
    use:quill={viewValue} />
</Style>

{#if !valid}
  <div class="error-label">
    <span class="font-icon error-icon">error</span>
    {errorMessage}
  </div>
{/if}
