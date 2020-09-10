import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as objects from '@Utility/objects';
import * as locales from '@Language/locale-utils';

const zeroPath = R.map(R.when(R.is(Number), R.always(0)));

const index = R.compose(
  Maybe.fromNull,
  R.head,
  R.filter(R.is(Number))
)

export const dataLens = (inputLanguage, path) => R.compose(
  R.lensPath,
  Maybe.orSome(path),
  Maybe.map(locales.path(R.__, path)),
)(inputLanguage);

export const typeLens = R.compose(
  R.lensPath,
  zeroPath
);

export const id = R.compose(
  R.replace(/-fi|-sv/g, ''),
  R.join('.'));

export const type = (schema, path) =>
  objects.requireNotNil(R.view(typeLens(path), schema),
    "Property: " + R.join('.', path) + " does not exists in schema.");

const localeKey = R.compose(
  R.replace(/-fi|-sv/g, ''),
  R.join('.'),
  zeroPath);

export const label = (i18n, inputLanguage, path) =>
  // input ordinal in array (starting from 1)
  R.compose(
    Maybe.orSome(''),
    R.map(i => `${i + 1}. `),
    index)(path) +
  // localized label text
  i18n('energiatodistus.' + localeKey(path)) +
  // input language symbol
  R.compose(
    Maybe.orSome(''),
    R.map(l => ` / ${l}`))(inputLanguage);

export function scrollIntoView(document, id) {
  document.getElementById(id)
    .parentElement.parentElement.scrollIntoView();
}