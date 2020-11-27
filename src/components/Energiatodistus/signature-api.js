import * as R from 'ramda';
import * as Future from '@Utility/future-utils';
import * as Fetch from '@Utility/fetch-utils';

const mpolluxUrl = 'https://localhost:53952';
const mpolluxVersionUrl = `${mpolluxUrl}/version`;
const mpolluxSignUrl = `${mpolluxUrl}/sign`;

const signatureOptions = {
  version: '1.1',
  selector: {
    keyusages: ['nonrepudiation']
  },
  contentType: 'data',
  hashAlgorithm: 'SHA256',
  signatureType: 'signature'
};

export const versionInfo = fetch =>
  Fetch.fetchFromUrl(fetch, mpolluxVersionUrl);

export const isValidSignatureResponse = R.compose(
  R.equals('ok'),
  R.prop('status')
);

export const getSignature = R.curry((fetch, content) =>
  R.compose(
    R.map(R.pick(['signature', 'chain'])),
    Future.filter(isValidSignatureResponse, 'error'),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', mpolluxSignUrl)),
    R.assoc('content', R.__, signatureOptions)
  )(content)
);
