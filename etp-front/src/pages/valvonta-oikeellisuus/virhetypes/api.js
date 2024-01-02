import * as R from 'ramda';
import * as Fetch from '@Utility/fetch-utils';
import * as Future from '@Utility/future-utils';

const url = {
  virhetypes: 'api/private/valvonta/oikeellisuus/virhetypes',
  virhetype: id => `${url.virhetypes}/${id}`
};

export const virhetypes = Fetch.getJson(fetch, url.virhetypes);

export const postVirhetype = R.curry(virhetype =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', url.virhetypes))
  )(virhetype)
);

export const putVirhetype = R.curry((id, virhetype) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'put', url.virhetype(id))),
    R.dissoc('id')
  )(virhetype)
);
