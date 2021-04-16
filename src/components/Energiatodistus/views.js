import * as Router from '@Component/Router/router';
import * as Maybe from '@Utility/maybe-utils';
import * as ET from '@Component/Energiatodistus/energiatodistus-utils';
import * as Locales from '@Language/locale-utils';

export const toETView = energiatodistus => {
  Router.push('#/energiatodistus/' + energiatodistus.versio + '/' + energiatodistus.id);
};

export const kayttotarkoitusTitle = locale => (luokittelut, energiatodistus) =>
  Maybe.fold(
    '-',
    ET.selectFormat(
      Locales.label(locale),
      luokittelut[energiatodistus.versio].kayttotarkoitusluokat
    ),
    ET.findKayttotarkoitusluokkaId(
      energiatodistus.perustiedot.kayttotarkoitus,
      luokittelut[energiatodistus.versio].alakayttotarkoitusluokat
    )
  ) +
  ' / ' +
  Maybe.fold(
    '-',
    ET.selectFormat(
      Locales.label(locale),
      luokittelut[energiatodistus.versio].alakayttotarkoitusluokat
    ),
    energiatodistus.perustiedot.kayttotarkoitus
  );