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

export const getAllYritykset = yritysApi.getAllYrityksetFuture;
