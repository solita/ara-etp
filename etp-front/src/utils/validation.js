/**
 * @module Validation
 * @description Functions to validate inputs
 */

import * as dfns from 'date-fns';

import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';
import * as deep from '@Utility/deep-objects';
import BigInt from 'big-integer';

export const DATE_FORMAT = 'dd.MM.yyyy';

/**
 * @typedef {Object} Validator
 * @property {Function} predicate
 * @property {Function} label
 */

/**
 * @sig string -> number
 */
export const ytunnusChecksum = R.compose(
  R.unless(R.equals(0), R.subtract(11)),
  R.modulo(R.__, 11),
  R.reduce(R.add, 0),
  R.zipWith(R.multiply, [7, 9, 10, 5, 8, 4, 2]),
  R.map(parseInt),
  R.slice(0, 7)
);

/**
 * @sig string -> boolean
 */
export const isValidYtunnus = R.allPass([
  R.test(/^\d{7}-\d$/),
  R.converge(R.equals, [ytunnusChecksum, R.compose(parseInt, R.nth(8))])
]);

export const ytunnusValidator = {
  predicate: isValidYtunnus,
  label: R.applyTo('validation.invalid-ytunnus')
};

export const isPaivamaara = R.compose(
  dfns.isValid,
  R.unless(R.anyPass([R.compose(R.equals('Date'), R.type), R.isNil]), date =>
    dfns.parse(date, DATE_FORMAT, 0)
  )
);

export const isFilled = R.complement(R.isEmpty);

export const isRequired = {
  predicate: isFilled,
  label: R.applyTo('validation.required')
};

export const isSome = {
  predicate: Maybe.isSome,
  label: R.applyTo('validation.required')
};

export const isValidId = (values, i18nKey) => ({
  predicate: R.compose(R.prop('valid'), id =>
    R.find(R.propEq(id, 'id'), values)
  ),
  label: R.applyTo(i18nKey)
});

export const interpolate = R.curry((template, values) =>
  R.reduce(
    (result, value) => R.replace(value[0], value[1], result),
    template,
    R.toPairs(values)
  )
);

/**
 * @sig Validator -> string -> Object -> Validator
 */
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

export const RequiredString = (min, max) => [
  isRequired,
  ...LimitedString(min, max)
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

export const isPostinumeroFI = R.test(/^\d{5}$/);

export const postinumeroFIValidator = {
  predicate: isPostinumeroFI,
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

/**
 * Returns the century that the century sign represents.
 *
 * @func
 * @sig string -> Maybe string
 * @example
 *
 *      centurysignToCentury('+');        //=>  Some('18')
 *      centurysignToCentury('-');        //=>  Some('19')
 *      centurysignToCentury('U');        //=>  Some('19')
 *      centurysignToCentury('A');        //=>  Some('20')
 *      centurysignToCentury('AB');       //=>  Nothing
 *
 */

const centurysignToCentury = R.compose(
  R.cond([
    [R.includes(R.__, ['+']), R.always(Maybe.Some('18'))],
    [
      R.includes(R.__, ['-', 'U', 'V', 'W', 'X', 'Y']),
      R.always(Maybe.Some('19'))
    ],
    [
      R.includes(R.__, ['A', 'B', 'C', 'D', 'E', 'F']),
      R.always(Maybe.Some('20'))
    ],
    [R.T, R.always(Maybe.None())]
  ])
);

/**
 * Returns the part of the henkilotunnus that is supposed to
 * be a year by concatenating the century from the century sign
 * and the 5th and the 6th character.
 *
 *  Does not check that it actually is a year.
 *
 *  Returns nothing if the century sign is invalid.
 *
 * @func
 * @sig string -> Maybe string
 *
 * @example
 *
 *      henkilotunnusYearPart('111111-111C'); //=> Some('1911')
 *      henkilotunnusYearPart('111111+111C'); //=> Some('1811')
 *      henkilotunnusYearPart('111111A111C'); //=> Some('2011')
 *      //NB: This is not a valid year.
 *      henkilotunnusYearPart('1111AB-111C'); //=> Some('19AB')
 *      henkilotunnusYearPart('1111ABK111C'); //=> Nothing
 *
 */
const henkilotunnusYearPart = R.compose(
  R.map(R.join('')),
  R.sequence(Maybe.of),
  R.props(['y1y2', 'y3y4']),
  R.applySpec({
    y1y2: R.compose(centurysignToCentury, R.nth(6)),
    y3y4: R.compose(Maybe.fromNull, R.slice(4, 6))
  })
);

/**
 * Returns the date of a henkilotunnus in form "dd.mm.yyyy".
 *
 * Does not check the validity of the date.
 *
 * @func
 * @sig string -> Maybe string
 *
 * @example
 *
 *      henkilotunnusDateString('111111-111C'); //=> Some('11.11.1911')
 *      henkilotunnusYearPart('111111+111C'); //=> Some('11.11.1811')
 *      henkilotunnusYearPart('111111A111C'); //=> Some('11.11.2011')
 *      //NB: This is not a valid date.
 *      henkilotunnusYearPart('1111AB-111C'); //=> Some('11.11.19AB')
 *      henkilotunnusYearPart('1111ABK111C'); //=> Nothing
 *
 */
const henkilotunnusDateString = R.compose(
  R.map(R.join('.')),
  R.sequence(Maybe.of),
  R.props(['dd', 'mm', 'yyyy']),
  R.applySpec({
    dd: R.compose(Maybe.fromNull, R.slice(0, 2)),
    mm: R.compose(Maybe.fromNull, R.slice(2, 4)),
    yyyy: henkilotunnusYearPart
  })
);

/**
 * @sig string -> boolean
 *
 * Checks the validity of a henkilotunnus.
 *
 */
export const isValidHenkilotunnus = R.allPass([
  R.complement(R.isNil),
  R.test(
    /^(0[1-9]|[12]\d|3[01])(0[1-9]|1[0-2])([5-9]\d\+|\d\d[-U-Y]|[012]\d[A-F])\d{3}[\dA-Y]$/
  ),
  R.compose(Maybe.orSome(false), R.map(isPaivamaara), henkilotunnusDateString),
  R.converge(R.equals, [henkilotunnusChecksum, R.takeLast(1)])
]);

export const henkilotunnusValidator = {
  predicate: isValidHenkilotunnus,
  label: R.applyTo('validation.invalid-henkilotunnus')
};

export const isLaatijanToteaja = R.test(/^(FISE|KIINKO)$/);

export const isValidEmail = R.test(
  /^([a-zA-Z0-9_\-\.]+)@([a-zA-Z0-9_\-\.]+)\.([a-zA-Z]{2,10})$/
);

export const emailValidator = {
  predicate: isValidEmail,
  label: R.applyTo('validation.invalid-email')
};

export const isValidApiPassword = R.test(/^([a-zA-Z0-9_\-\.\!]){8,200}$/);

export const apiPasswordValidator = {
  predicate: isValidApiPassword,
  label: R.applyTo('validation.invalid-api-key')
};

export const isPuhelin = R.test(/^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\s\./0-9]*$/);

const isIpAddress = R.compose(
  R.allPass([
    R.compose(R.equals(4), R.length),
    R.all(R.compose(R.both(R.lte(0), R.gte(255)), Number.parseInt))
  ]),
  R.split('.')
);

export const ipAddressValidator = {
  predicate: isIpAddress,
  label: R.applyTo('validation.invalid-ip-address')
};

export const ipAddress = [
  ...LimitedString(0, 15),
  { predicate: isIpAddress, label: R.applyTo('validation.invalid-ip-address') }
];

export const isPatevyystaso = R.test(/^(1|2)$/);

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

/**
 * @sig Array [Validator] -> a -> Either [Validator,a]
 *
 * This function checks a single value from the data
 * model against an array of validators.
 *
 * On success returns Either.Right(value),
 * on failure returns Either.Left(validator).
 */
const validate = (validators, value) =>
  Maybe.fromUndefined(
    R.find(R.compose(R.not, R.applyTo(value), R.prop('predicate')), validators)
  )
    .toEither(value)
    .swap();

/**
 * @sig Array [Validator] -> a -> Either [(Translate -> string),a]
 *
 * This function checks a single value from the data model
 * against an array of validators. The value is passed through
 * as-is from `validate` if the validations succeed, but on failure
 * the Left validator is mapped to a Left label function.
 */
export const validateModelValue = R.curry((validators, value) =>
  Either.fromValueOrEither(value).flatMap(modelValue =>
    validate(validators, modelValue).leftMap(R.prop('label'))
  )
);

const expandSchemaToObjectSize = (schemaObject, object) =>
  Array.isArray(schemaObject) &&
  schemaObject.length === 1 &&
  Array.isArray(object) &&
  object.length > 1
    ? R.map(R.always(schemaObject[0]), R.range(0, object.length))
    : schemaObject;

/**
 * @sig Object -> boolean
 *
 * deep.map needs this kind of a predicate function to decide what should be considered
 * a leaf object that is mapped  instead of walked through
 */
const isValidatorArray = R.allPass([
  R.is(Array),
  R.all(R.propIs(Function, 'predicate'))
]);

/**
 * @sig Object -> Object -> Object
 *
 * This function is used to validate a data model object against
 * a schema object.
 */
export const validateModelObject = R.curry((schemaObject, object) =>
  R.evolve(
    deep.map(
      isValidatorArray,
      validators => validateModelValue(validators),
      expandSchemaToObjectSize(schemaObject, object)
    ),
    object
  )
);

/**
 * @sig Object -> boolean
 */
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
 * @description Dispatch custom validation event for all the form inputs to ensure <br>
 * that the inputs are validated and they show error message <br>
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

export const isBoolean = val => 'boolean' === typeof val;
