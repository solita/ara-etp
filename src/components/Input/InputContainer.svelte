<script>
  import * as R from 'ramda';
  import * as Either from '@Utility/either-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as v from '@Utility/validation';
  import * as keys from '@Utility/keys';

  export let lens;
  export let model;
  export let i18n;
  export let parse = R.identity;
  export let format = R.identity;
  export let handleSubmit;
  export let validators = [];

  let viewValue;
  let errorMessage = '';
  let valid = true;

  $: validate = value =>
    v.validateModelValue(validators, value).cata(
      error => {
        valid = false;
        errorMessage = error(i18n);
      },
      () => (valid = true)
    );

  $: viewValue = R.compose(
    Maybe.orSome(viewValue),
    Either.toMaybe,
    R.map(format),
    Either.fromValueOrEither,
    R.view(lens)
  )(model);

  $: updateValue = value => {
    const parsedValue = parse(value);
    Either.fromValueOrEither(parsedValue).forEach(() => (viewValue = ''));
    model = R.set(lens, parsedValue, model);
    validate(parsedValue);
  };
</script>

<div
  on:focus|capture={event => validate(parse(event.target.value))}
  on:blur|capture={event => updateValue(event.target.value)}
  on:input={event => validate(parse(event.target.value))}
  on:keydown={event => {
    if (event.keyCode === keys.ENTER) {
      updateValue(event.target.value);
    }
  }}>
  <slot {viewValue} {valid} {errorMessage} />
</div>
