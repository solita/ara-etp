import * as R from 'ramda';
import * as etApi from '@Pages/energiatodistus/energiatodistus-api.js';

const capitalize = R.compose(
  R.join(''),
  R.juxt([R.compose(R.toUpper, R.head), R.tail])
);

const statuses = [
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

export const status = R.compose(R.map(parseInt), R.invertObj)(statuses);

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

export const pdfUrl = (energiatodistus, language) =>
  etApi.url.pdf(energiatodistus.versio, energiatodistus.id, language);