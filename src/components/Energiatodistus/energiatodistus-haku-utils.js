const gt = { label: '>', command: (first, second) => `> ${first} ${second}` };
const gte = {
  label: '>=',
  command: (first, second) => `>= ${first} ${second}`
};
const lt = { label: '<', command: (first, second) => `< ${first} ${second}` };
const lte = {
  label: '<=',
  command: (first, second) => `<= ${first} ${second}`
};
const eq = { label: '=', command: (first, second) => `= ${first} ${second}` };

const contains = { label: 'contains', command: str => `like %${str}%` };

const allComparisons = [eq, gt, gte, lt, lte];

const kriteeri = (key, operators) => ({ key, operators });

const korvattuEnergiatodistusIdKriteeri = kriteeri(
  'korvattu-energiatodistus-id',
  [allComparisons]
);

const idKriteeri = kriteeri('id', allComparisons);

const allekirjoitusaikaKriteeri = kriteeri('allekirjoitusaika', []);

const viimeinenvoimassaoloKriteeri = kriteeri('viimeinenvoimassaolo', []);

export const perustiedot = () => ({
  nimi: kriteeri('perustiedot.nimi', [eq, contains]),
  rakennustunnus: kriteeri('perustiedot.rakennustunnus', [allComparisons]),
  kiinteistotunnus: kriteeri('perustiedot.kiinteistotunnus', [allComparisons]),
  'katuosoite-fi': kriteeri('perustiedot.katuosoite-fi', []),
  'katuosoite-sv': kriteeri('perustiedot.katuosoite-sv', []),
  postinumero: kriteeri('perustiedot.postinumero', [allComparisons]),
  'onko-julkinen-rakennus': kriteeri('perustiedot.onko-julkinen-rakennus', [
    eq
  ]),
  uudisrakennus: kriteeri('perustiedot.uudisrakennus', [eq]),
  laatimisvaihe: kriteeri('perustiedot.laatimisvaihe', []),
  havainnointikaynti: kriteeri('perustiedot.havainnointikaynti', []),
  valmistumisvuosi: kriteeri('perustiedot.valmistumisvuosi', []),
  rakennusluokka: kriteeri('rakennusluokka', []),
  'rakennuksen-kayttotarkoitusluokka': kriteeri(
    'rakennuksen-kayttotarkoitusluokka',
    []
  )
});
