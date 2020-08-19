import * as Maybe from "@Utility/maybe-utils";

const key = {
  LaskutettavaYritysId: 'energiatodistus.laskutettava-yritys-id'
}

export const setDefaultLaskutettavaYritysId = yritysId => {
  yritysId.forEach(id => {
    window.localStorage.setItem(
      key.LaskutettavaYritysId,
      id);
  });
  yritysId.orElseRun(() => {
    window.localStorage.removeItem(key.LaskutettavaYritysId);
  })
}

export const getDefaultLaskutettavaYritysId = () =>
  Maybe.fromNull(window.localStorage.getItem(key.LaskutettavaYritysId))
    .map(parseInt);