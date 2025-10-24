<script>
  /*
  The purpose of this component is to be used as an accessor when you want to just view a value.
  The value is looked up from the model and the type is looked up from the schema.
  Then the appropriate format function defined in the type is applied to the value.
  You can provide a custom value to be used in case the value is `None`.
  */

  import * as R from 'ramda';
  import * as Either from '@Utility/either-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as objects from '@Utility/objects';

  export let lens;
  export let model;
  export let schema;
  export let valueOnNone;

  const valueWithValidationWrapper = R.view(lens, model);
  const type = R.view(lens, schema);
  const format = type.format;

  // Every value inside the validation wrapper (Either) should be wrapped
  // with `Maybe`. Throw an error if this is not true.
  const requireNotNilValue = objects.requireNotNil(
    R.__,
    'Nil value in ViewModelValue with lens: ' +
      lens +
      '. ' +
      'Nil values are not allowed. Use Maybe monad for optional values.'
  );

  // In our model there are sometimes leaves that are not of type `Either (Maybe a)` but just of type `Maybe a`.
  // In this case we want to wrap them with `Either` to make every value be of the same type.
  // Meaning our model is of type `Tree ((Either (Maybe a)) | Maybe a)` and we make it to be just of type
  // `Tree (Either (Maybe a))` here.
  const handleValuesWithoutValidation = Either.fromValueOrEither;

  const applyFormat = R.ifElse(
    R.isNil(valueOnNone),
    format,
    R.ifElse(Maybe.isNone, R.always(valueOnNone), format)
  );
</script>

{R.compose(
  Maybe.get,
  Either.toMaybe,
  Either.map(format),
  Either.map(applyFormat),
  R.tap(Either.map(requireNotNilValue)),
  handleValuesWithoutValidation
)(valueWithValidationWrapper)}
