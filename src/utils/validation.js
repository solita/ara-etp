import * as dfns from 'date-fns';

import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';
import * as deep from '@Utility/deep-objects';
import BigInt from 'big-integer';

export const DATE_FORMAT = 'dd.MM.yyyy';

export const ytunnusChecksum = R.compose(
  R.unless(R.equals(0), R.subtract(11)),
  R.modulo(R.__, 11),
  R.reduce(R.add, 0),
  R.zipWith(R.multiply, [7, 9, 10, 5, 8, 4, 2]),
  R.map(parseInt),
  R.slice(0, 7)
);

export const isValidYtunnus = R.allPass([
  R.test(/^\d{7}-\d$/),
  R.converge(R.equals, [ytunnusChecksum, R.compose(parseInt, R.nth(8))])
]);

export const ytunnusValidator = {
  predicate: isValidYtunnus,
  label: R.applyTo('validation.invalid-ytunnus')
};

export const isFilled = R.complement(R.isEmpty);

export const isRequired = {
  predicate: isFilled,
  label: R.applyTo('validation.required')
};

export const isSome = {
  predicate: Maybe.isSome,
  label: R.applyTo('validation.required')
};

export const interpolate = R.curry((template, values) =>
  R.reduce(
    (result, value) => R.replace(value[0], value[1], result),
    template,
    R.toPairs(values)
  )
);

export const constraint = (predicate, name, labelValues) => ({
  predicate: predicate,
  label: R.compose(
    interpolate(R.__, labelValues),
    R.applyTo('validation.' + name)
  )
});

export const lengthConstraint = (predicate, name, labelValues) =>
  constraint(R.compose(predicate, R.length), name + '-length', labelValues);

export const minLengthConstraint = min =>
  lengthConstraint(R.lte(min), 'min', { '{min}': min });

export const maxLengthConstraint = max =>
  lengthConstraint(R.gte(max), 'max', { '{max}': max });

export const LimitedString = (min, max) => [
  minLengthConstraint(min),
  maxLengthConstraint(max)
];

export const isUrl = R.test(
  /^https?:\/\/(www\.)?[-a-zA-ZäöåÄÖÅ0-9@:%._\+~#=]{1,256}\.[a-zA-ZäöåÄÖÅ0-9()]{1,6}\b([-a-zA-ZäöåÄÖÅ0-9()@:%_\+.~#?&//=]*)$/
);

const Interval3 = (labelPrefix, min, max) => [
  constraint(R.lte(min), labelPrefix + 'min-number', { '{min}': min }),
  constraint(R.gte(max), labelPrefix + 'max-number', { '{max}': max })
];

export const Interval = (min, max) => Interval3('', min, max);

export const MaybeInterval = (min, max) =>
  R.map(liftValidator, Interval(min, max));

export const MaybeIntervalWarning = (min, max) =>
  R.map(liftValidator, Interval3('warning.', min, max));

export const urlValidator = {
  predicate: isUrl,
  label: R.applyTo('validation.invalid-url')
};

export const liftValidator = validator =>
  R.over(
    R.lensProp('predicate'),
    predicate => R.compose(Maybe.orSome(true), R.lift(predicate)), //value => value.map(predicate).orSome(true),
    validator
  );

export const isPostinumero = R.test(/^\d{5}$/);

export const postinumeroValidator = {
  predicate: isPostinumero,
  label: R.applyTo('validation.invalid-postinumero-fi')
};

export const henkilotunnusChecksum = R.compose(
  R.nth(R.__, '0123456789ABCDEFHJKLMNPRSTUVWXY'),
  R.modulo(R.__, 31),
  parseInt,
  R.join(''),
  R.filter(Number.isInteger),
  R.map(parseInt),
  R.slice(0, 10)
);

export const isValidHenkilotunnus = R.allPass([
  R.complement(R.isNil),
  R.test(
    /^(0[1-9]|[12]\d|3[01])(0[1-9]|1[0-2])([5-9]\d\+|\d\d-|[01]\dA)\d{3}[\dA-Z]$/
  ),
  R.converge(R.equals, [henkilotunnusChecksum, R.takeLast(1)])
]);

export const henkilotunnusValidator = {
  predicate: isValidHenkilotunnus,
  label: R.applyTo('validation.invalid-henkilotunnus')
};

export const isLaatijanToteaja = R.test(/^(FISE|KIINKO)$/);

export const isValidEmail = R.test(
  /^([a-zA-Z0-9_\-\.]+)@([a-zA-Z0-9_\-\.]+)\.([a-zA-Z]{2,5})$/
);

export const emailValidator = {
  predicate: isValidEmail,
  label: R.applyTo('validation.invalid-email')
};

export const isPuhelin = R.test(/^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\s\./0-9]*$/);

export const isPatevyystaso = R.test(/^(1|2)$/);

export const isPaivamaara = R.compose(
  dfns.isValid,
  R.unless(R.compose(R.equals('Date'), R.type), date =>
    dfns.parse(date, DATE_FORMAT, 0)
  )
);

export const isRakennustunnus = R.allPass([
  R.compose(R.equals(10), R.length),
  R.test(/^1\d{8}[A-Z0-9]{1}$/),
  R.converge(R.equals, [
    R.compose(henkilotunnusChecksum, R.slice(0, 9)),
    R.takeLast(1)
  ])
]);

export const rakennustunnusValidator = {
  predicate: isRakennustunnus,
  label: R.applyTo('validation.invalid-rakennustunnus')
};

export const isOVTTunnus = R.allPass([
  R.test(/^0037\d{8,13}$/),
  R.compose(
    isValidYtunnus,
    R.join(''),
    R.adjust(7, R.concat('-')),
    R.take(8),
    R.drop(4)
  )
]);

export const isIBAN = R.allPass([
  R.compose(R.gt(R.__, 4), R.length),
  R.test(/^[A-Z]{2}[A-Z0-9]*$/),
  R.compose(
    n => n.equals(1),
    n => n.mod(97),
    R.tryCatch(BigInt, R.always(BigInt(0))),
    R.join(''),
    R.map(
      R.when(
        R.compose(R.lte(65), item => item.charCodeAt(0)),
        R.compose(R.subtract(R.__, 55), item => item.charCodeAt(0))
      )
    ),
    R.toUpper,
    R.converge(R.concat, [R.drop(4), R.take(4)])
  )
]);

export const isTEOVTTunnus = R.allPass([
  R.compose(R.gte(R.__, 2), R.length),
  R.compose(R.startsWith('te'), R.toLower),
  R.compose(isOVTTunnus, R.drop(2))
]);

export const isVerkkolaskuosoite = R.anyPass([
  isOVTTunnus,
  isIBAN,
  isTEOVTTunnus
]);

export const VerkkolaskuosoiteValidator = {
  predicate: isVerkkolaskuosoite,
  label: R.applyTo('validation.invalid-verkkolaskuosoite')
};

export const validate = (validators, value) =>
  Maybe.fromUndefined(
    R.find(R.compose(R.not, R.applyTo(value), R.prop('predicate')), validators)
  )
    .toEither(value)
    .swap();

export const validateModelValue = R.curry((validators, value) =>
  Either.fromValueOrEither(value).flatMap(modelValue =>
    validate(validators, modelValue).leftMap(R.prop('label'))
  )
);

export const validateModelObject = R.curry((schemaObject, object) =>
  R.evolve(
    deep.map(
      R.allPass([R.is(Array), R.all(R.propIs(Function, 'predicate'))]),
      v => validateModelValue(v),
      schemaObject
    ),
    object
  )
);

export const isValidForm = schemaObject =>
  R.compose(
    R.all(Either.isRight),
    R.filter(Either.isEither),
    deep.values(Either.isEither),
    validateModelObject(schemaObject)
  );

const dispatchValidationEvent = blurred => element =>
  element.dispatchEvent(
    new CustomEvent('validation', { detail: { blurred: blurred } })
  );

const dispatchValidationEvents = (blurred, elements) =>
  R.forEach(dispatchValidationEvent(blurred), elements);

/**
 * Dispatch custom validation event for all the form inputs to ensure
 * that the inputs are validated and they show error message
 * if not valid.
 */
export const blurForm = form =>
  dispatchValidationEvents(
    true,
    form.getElementsByClassName('input-container')
  );

export const blurFormExcludeNested = form =>
  dispatchValidationEvents(
    true,
    R.filter(
      container => container.closest('form') === form,
      form.getElementsByClassName('input-container')
    )
  );

export const unblurForm = form =>
  dispatchValidationEvents(
    false,
    form.getElementsByClassName('input-container')
  );
