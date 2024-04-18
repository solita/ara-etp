import * as R from 'ramda';

const capitalize = R.compose(
  R.join(''),
  R.juxt([R.compose(R.toUpper, R.head), R.tail])
);

export const statuses = [
  'not_started',
  'already_started',
  'start',
  'digest',
  'signature',
  'pdf',
  'finish',
  'signed',
  'aborted'
];

const statusKey = id => statuses[id];

export const statusText = R.curry((i18n, state) => {
  const languageAdjectiveName = i18n(
    'energiatodistus.signing.language-adjective.' + state.language
  );
  const languageAdjectiveGenetiveName = i18n(
    'energiatodistus.signing.language-genitive.' + state.language
  );

  return R.compose(
    capitalize,
    R.replace('{language}', languageAdjectiveName),
    R.replace('{language-genitive}', languageAdjectiveGenetiveName),
    key => i18n('energiatodistus.signing.status.' + key),
    R.replace('_', '-'),
    statusKey
  )(state.status);
});
