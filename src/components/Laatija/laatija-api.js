import * as R from "ramda";
import * as Fetch from "@Utility/fetch-utils";
import * as Future from "@Utility/future-utils";
import * as yritysApi from "@Component/Yritys/yritys-utils"

export const url = {
  laatijat: '/api/private/laatijat',
  laatija: id => `${url.laatijat}/${id}`,
  yritykset: id => `${url.laatija(id)}/yritykset`
};

export const getYritykset = R.curry((fetch, id) =>
  R.compose(
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch)),
    url.yritykset)(id));

const toggleLaatijaYritys = R.curry((method, fetch, laatijaId, yritysId) =>
  R.chain(
    Fetch.rejectWithInvalidResponse,
    Future.attemptP(_ => fetch(url.yritykset(laatijaId) + '/' + yritysId, {method}))));

export const putLaatijaYritys = toggleLaatijaYritys('put');
export const deleteLaatijaYritys = toggleLaatijaYritys('delete');

export const getAllYritykset = yritysApi.getAllYrityksetFuture;

export const patevyydet = R.compose(
  Future.cache,
  Fetch.responseAsJson,
  Future.encaseP(Fetch.getFetch(fetch))
)('api/private/patevyydet/');