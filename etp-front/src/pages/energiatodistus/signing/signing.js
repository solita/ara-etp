import * as R from 'ramda';
import * as etApi from '@Pages/energiatodistus/energiatodistus-api.js';
import * as Fetch from '@Utility/fetch-utils.js';
import * as Future from '@Utility/future-utils';

const capitalize = R.compose(
  R.join(''),
  R.juxt([R.compose(R.toUpper, R.head), R.tail])
);

const statuses = [
  'not_started',
  'confirming_start',
  'in_progress',
  'in_progress_reloaded',
  'signed'
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

export const signingAllowed = fetch =>
  R.compose(
    R.map(R.prop('signing-allowed')),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    R.always('/api/private/energiatodistukset/validate-session')
  )();
