import * as GeoUtils from '@/utilities/geo';
import { labelLocale } from '@Localization/localization';

export const calculateLaatijaWeight = (toimintaalueet, laatija) =>
  weightByToimintaalueet(toimintaalueet, laatija.toimintaalue) +
  weightByMuutToimintaalueet(toimintaalueet, laatija.muuttoimintaalueet) +
  weightByJulkisettiedot(laatija.wwwosoite, laatija.puhelin, laatija.email) +
  weightByActivity(laatija.aktiivinen);

export const weightByToimintaalueet = (haetutToimintaalueet, toimintaalue) =>
  haetutToimintaalueet.has(toimintaalue) ? 2 : 0;

export const weightByMuutToimintaalueet = (
  haetutToimintaalueet,
  muuttoimintaalueet
) => (muuttoimintaalueet.some(ta => haetutToimintaalueet.has(ta)) ? 1 : 0);

export const weightByJulkisettiedot = (wwwosoite, puhelin, email) =>
  wwwosoite || puhelin || email ? 2 : 0;

export const weightByActivity = aktiivinen => (aktiivinen ? 1 : 0);

export const findPatevyys = (patevyydet, laatija) =>
  patevyydet.find(patevyys => patevyys.id === laatija.patevyystaso);

export const laatijatByNimihaku = (nimihaku, laatijat) => {
  if (!nimihaku) return new Set(laatijat.map(laatija => laatija.id));
  return new Set(
    laatijat
      .filter(laatija =>
        laatija.nimi.toUpperCase().includes(nimihaku.toUpperCase())
      )
      .map(laatija => laatija.id)
  );
};

export const laatijatByAluehaku = (laatijat, toimintaalueet) => {
  return new Set(
    laatijat
      .filter(
        laatija =>
          toimintaalueet.has(laatija.toimintaalue) ||
          laatija.muuttoimintaalueet.some(toimintaalue =>
            toimintaalueet.has(toimintaalue)
          )
      )
      .map(laatija => laatija.id)
  );
};

export const laatijatByHakukriteerit = (
  aluehaku,
  nimihaku,
  laatijat,
  toimintaalueet,
  patevyydet
) => {
  const laatijatWithGivenPatevyys = laatijat.filter(laatija =>
    patevyydet
      .split(',')
      .map(item => parseInt(item))
      .includes(laatija.patevyystaso)
  );
  const nimet = laatijatByNimihaku(nimihaku, laatijatWithGivenPatevyys);

  if (!aluehaku)
    return laatijatWithGivenPatevyys.filter(laatija => nimet.has(laatija.id));

  const alueet = laatijatByAluehaku(
    laatijatWithGivenPatevyys.filter(laatija => nimet.has(laatija.id)),
    toimintaalueet
  );

  return laatijatWithGivenPatevyys.filter(laatija => alueet.has(laatija.id));
};

export const deserialize = (locale, patevyydet, toimintaalueet) => laatija => ({
  ...laatija,
  nimi: `${laatija.etunimi} ${laatija.sukunimi}`,
  patevyys: labelLocale(locale, findPatevyys(patevyydet, laatija)),
  'toimintaalue-nimi':
    labelLocale(
      locale,
      GeoUtils.findToimintaalue(toimintaalueet, laatija.toimintaalue)
    ) ?? '',
  'muuttoimintaalueet-nimet': laatija['muuttoimintaalueet'].map(
    ta =>
      labelLocale(locale, GeoUtils.findToimintaalue(toimintaalueet, ta)) ?? ''
  ),
  postitoimipaikka: laatija.postitoimipaikka ?? '',
  wwwosoite: laatija.wwwosoite
});

export const sliceLaatijaTable = (page, pagesize, laatijat) =>
  laatijat.slice(page * pagesize, (page + 1) * pagesize);
