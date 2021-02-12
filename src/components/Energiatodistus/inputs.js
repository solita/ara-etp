import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as objects from '@Utility/objects';
import * as locales from '@Language/locale-utils';

const zeroPath = R.map(R.when(R.is(Number), R.always(0)));

const index = R.compose(Maybe.fromNull, R.head, R.filter(R.is(Number)));

export const dataLens = (inputLanguage, path) =>
  R.compose(
    R.lensPath,
    Maybe.orSome(path),
    Maybe.map(locales.path(R.__, path))
  )(inputLanguage);

export const typeLens = R.compose(R.lensPath, zeroPath);

export const removeLocalePostfix = R.replace(/-fi|-sv/g, '');

export const id = R.compose(removeLocalePostfix, R.join('.'));

export const type = (schema, path) =>
  objects.requireNotNil(
    R.view(typeLens(path), schema),
    'Property: ' + R.join('.', path) + ' does not exists in schema.'
  );

export const required = (inputLanguage, type, model) =>
  R.defaultTo(
    R.F,
    R.path(['required', Maybe.orSome('all', inputLanguage)], type)
  )(model);

const localeKey = R.compose(removeLocalePostfix, R.join('.'), zeroPath);

export const label = (i18n, inputLanguage, path) =>
  // input ordinal in array (starting from 1)
  R.compose(
    Maybe.orSome(''),
    R.map(i => `${i + 1}. `),
    index
  )(path) +
  // localized label text
  i18n('energiatodistus.' + localeKey(path)) +
  // input language symbol
  R.compose(
    Maybe.orSome(''),
    R.map(l => ` / ${l}`)
  )(inputLanguage);

const labelContext = (i18n, path) =>
  R.length(path) > 1
    ? R.compose(
        R.filter(R.complement(R.equals('$unused'))),
        Maybe.fromEmpty
      )(
        i18n(
          'energiatodistus.' +
            localeKey(R.slice(0, -1, path)) +
            '.label-context'
        )
      )
    : Maybe.None();

export const fullLabel = (i18n, inputLanguage, path) =>
  labelContext(i18n, path)
    .map(t => t + ' / ')
    .orSome('') + label(i18n, inputLanguage, path);

export function scrollIntoView(document, id) {
  document
    .getElementById(R.replace(/-fi|-sv/g, '', id))
    .parentElement.parentElement.scrollIntoView();
}

const language = R.compose(
  R.map(R.slice(-2, Infinity)),
  R.filter(R.either(R.endsWith('-fi'), R.endsWith('-sv'))),
  Maybe.Some
);

export const propertyLabel = R.curry((i18n, propertyName) =>
  fullLabel(i18n, language(propertyName), R.split('.', propertyName))
);
