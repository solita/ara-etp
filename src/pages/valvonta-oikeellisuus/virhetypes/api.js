import * as R from 'ramda';
import * as Fetch from '@Utility/fetch-utils';
import * as Future from '@Utility/future-utils';

const url = {
  virhetyypit: 'api/private/valvonta/oikeellisuus/virhetyypit',
  virhetyyppi: id => `${url.virhetyypit}/${id}`
};

export const virhetyypit = Fetch.getJson(fetch, url.virhetyypit);

export const postVirhetype = R.curry((virhetype) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(Fetch.fetchWithMethod(fetch, 'post', url.virhetyypit)),
  )(virhetype)
);

export const putVirhetype = R.curry((id, virhetype) =>
  R.compose(
    R.chain(Fetch.rejectWithInvalidResponse),
    Future.encaseP(
      Fetch.fetchWithMethod(fetch, 'put', url.virhetyyppi(id))
    ),
    R.dissoc('id')
  )(virhetype)
);
