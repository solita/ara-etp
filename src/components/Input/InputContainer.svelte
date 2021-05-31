<script>
  import * as R from 'ramda';
  import * as Either from '@Utility/either-utils';
  import * as v from '@Utility/validation';
  import * as keys from '@Utility/keys';
  import * as objects from '@Utility/objects';
  import { tick } from 'svelte';

  export let id;
  export let lens;
  export let model;
  export let i18n;
  export let parse = R.identity;
  export let format = R.identity;
  export let validators = [];
  export let warnValidators = [];

  export let currentValue = '';
  export let tooltip = null;
  export let disabled = false;

  let blurred = false;
  let viewValue;
  let focused = false;
  let valid = true;
  let validationResult = {
    type: '',
    message: ''
  };

  const requireNotNil = objects.requireNotNil(
    R.__,
    'Nil value in input: ' +
      id +
      '. ' +
      'Nil values are not allowed. Use Maybe monad for optional values.'
  );

  $: validate = value => {
    valid = true;

    v.validateModelValue(warnValidators, value).forEachLeft(error => {
      valid = false;
      validationResult = {
        type: 'warning',
        message: error(i18n)
      };
    });

    v.validateModelValue(validators, value).forEachLeft(error => {
      valid = false;
      validationResult = {
        type: 'error',
        message: error(i18n)
      };
    });
  };

  const updateCurrentValue = value => (currentValue = parse(value));

  $: formatModelValue = R.compose(
    Either.toMaybe,
    R.map(format),
    Either.fromValueOrEither,
    requireNotNil,
    R.view(lens)
  );

  $: R.forEach(value => {
    viewValue = value;
  }, formatModelValue(model));

  $: updateModel = value => {
    viewValue = value;
    const parsedValue = parse(value);
    currentValue = parsedValue;
    tick().then(_ => {
      model = R.set(lens, parsedValue, model);
      validate(parsedValue);
    });
  };

  $: !disabled && blurred && validate(R.view(lens, model));
  $: error = focused && !valid && validationResult.type === 'error';
  $: warning = focused && !valid && validationResult.type === 'warning';
</script>

<div
  title={tooltip}
  on:focus|capture={event => {
    focused = true;
    validate(parse(event.target.value));
  }}
  on:blur|capture={event => {
    focused = false;
    blurred = true;
    updateModel(event.target.value);
  }}
  on:input={event => {
    updateCurrentValue(event.target.value);
    validate(parse(event.target.value));
  }}
  on:keydown={event => {
    if (event.keyCode === keys.ENTER) {
      updateModel(event.target.value);
    }
  }}>
  <slot {viewValue} {focused} {valid} {validationResult} {warning} {error} />
</div>
