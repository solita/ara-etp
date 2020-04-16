<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as v from '@Utility/validation';
  import Label from '@Component/Label/Label';

  export let id;
  export let required;
  export let label = '';

  export let model = { empty: '' };
  export let lens = R.lensProp('empty');

  export let parse = R.identity;
  export let format = R.identity;
  export let validators = [];

  $: viewValue = R.compose(
    Maybe.orSome(viewValue),
    Either.toMaybe,
    R.map(format),
    Either.fromValueOrEither,
    R.view(lens)
  )(model);

  let valid = true;
  let errorMessage = '';
  let focused = false;

  export let i18n;

  $: highlightError = focused && !valid;

  $: validate = value =>
    v.validateModelValue(validators, value).cata(
      error => {
        valid = false;
        errorMessage = error(i18n);
      },
      () => (valid = true)
    );
</script>

<style type="text/postcss">
  div {
    @apply flex relative items-stretch border-b-3 border-disabled text-dark;
    min-height: 6em;
  }

  div:hover {
    @apply border-hover;
  }

  div.focused {
    @apply border-primary;
  }

  textarea {
    @apply font-medium flex-grow resize-none text-xl;
  }

  textarea:focus {
    @apply outline-none;
  }

  textarea::-webkit-scrollbar {
    @apply w-2;
  }

  textarea::-webkit-scrollbar-track {
    @apply bg-background;
  }

  textarea::-webkit-scrollbar-thumb {
    @apply bg-disabled;
  }

  textarea::-webkit-scrollbar-thumb:hover {
    @apply bg-dark;
  }
</style>

<Label {id} {required} {label} error={highlightError} {focused} />
<div class="inputwrapper">
  <textarea {id} {required} value={viewValue} />
</div>
