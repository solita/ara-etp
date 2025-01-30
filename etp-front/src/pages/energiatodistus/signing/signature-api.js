import * as R from 'ramda';
import * as Future from '@Utility/future-utils.js';
import * as Fetch from '@Utility/fetch-utils.js';

const mpolluxUrl = 'https://localhost:53952';
const mpolluxVersionUrl = `${mpolluxUrl}/version`;
const mpolluxSignUrl = `${mpolluxUrl}/sign`;

const signatureOptions = version => ({
  version: version,
  selector: {
    keyusages: ['nonrepudiation'],
    keyalgorithms: ['rsa']
  },
  contentType: 'data',
  hashAlgorithm: 'SHA256',
  signatureType: 'cms-pades'
});

// Example of mPollux response can be found in etp-core/docker/mpollux/api/version
export const versionInfo = fetch => Fetch.getJson(fetch, mpolluxVersionUrl);

const getSignatureOptions = fetch =>
  R.map(R.compose(signatureOptions, R.prop('version')), versionInfo(fetch));

export const isValidSignatureResponse = R.compose(
  R.equals('ok'),
  R.prop('status')
);

export const getSignature = R.curry((fetch, content) =>
  R.chain(
    R.compose(
      R.map(R.pick(['signature', 'chain'])),
      Future.filter(isValidSignatureResponse, 'error'),
      Fetch.responseAsJson,
      Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', mpolluxSignUrl)),
      R.assoc('content', R.prop('digest', content))
    ),
    getSignatureOptions(fetch)
  )
);
