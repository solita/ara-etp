import * as Future from '@Utility/future-utils';
import * as EnergiatodistusApi from '@Pages/energiatodistus/energiatodistus-api';
import * as Maybe from '@Utility/maybe-utils';
import * as R from 'ramda';

export const fetchEnergiatodistusWithKorvaavat = (whoami, versio, id) => {
  const energiatodistus = Future.cache(
    EnergiatodistusApi.getEnergiatodistusById(versio, id)
  );

  // [path, et] -> Future path
  const korvaavuusChain = ([path, et]) =>
    Maybe.fold(
      // Future path
      Future.resolve(path),
      // Compose produces a korvaavaId => Future path function
      R.compose(
        R.chain(korvaavuusChain),
        R.map(korvaavaEt => [R.concat(path, [korvaavaEt]), korvaavaEt]), // Future [path, ET]
        EnergiatodistusApi.getEnergiatodistusById('all') // Future ET
      ),
      et['korvaava-energiatodistus-id']
    );

  const korvaavatEnergiatodistukset = R.chain(
    R.compose(korvaavuusChain, et => [[], et]),
    energiatodistus
  );

  return { energiatodistus, korvaavatEnergiatodistukset };
};
