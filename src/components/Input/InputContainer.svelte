<script>
  import * as R from 'ramda';
  import * as Either from '@Utility/either-utils';
  import * as v from '@Utility/validation';
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

  $: !disabled && !focused && blurred && validate(R.view(lens, model));
  $: error = focused && !valid && validationResult.type === 'error';
  $: warning = focused && !valid && validationResult.type === 'warning';

  $: api = {
    focus: _ => {
      if (!disabled) {
        focused = true;
        validate(R.view(lens, model));
      }
    },
    blur: value => {
      if (!disabled) {
        focused = false;
        blurred = true;
        updateModel(value);
      }
    },
    input: value => {
      if (!disabled) {
        updateCurrentValue(value);
        validate(parse(value));
      }
    },
    update: updateModel
  };
</script>

<div
  title={tooltip}
  class="input-container"
  on:validation={event => {
    blurred = event.detail.blurred;
  }}>
  <slot
    {api}
    {viewValue}
    {focused}
    {valid}
    {validationResult}
    {warning}
    {error} />
</div>
