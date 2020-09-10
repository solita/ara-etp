import * as R from 'ramda';
import * as dfns from 'date-fns';

import * as validation from '@Utility/validation';
import * as deep from '@Utility/deep-objects';
import * as Maybe from '@Utility/maybe-utils';
import * as Either from '@Utility/either-utils';
import * as objects from '@Utility/objects';
import * as fxmath from '@Utility/fxmath';
import * as laatimisvaiheet from './laatimisvaiheet';

export const isValidForm = R.compose(
  R.all(Either.isRight),
  R.filter(R.allPass([R.complement(R.isNil), Either.isEither])),
  deep.values(Either.isEither),
  validation.validateModelObject
);

export const breadcrumb1stLevel = i18n => ({
  label: i18n('navigation.energiatodistukset'),
  url: '/#/energiatodistus/all'
});

export const selectFormat = (label, items) =>
  R.compose(Maybe.orSome(''), R.map(label), Maybe.findById(R.__, items));

export const findKayttotarkoitusluokkaId = (
  alakayttotarkoitusluokkaId,
  alakayttotarkoitusluokat
) =>
  R.compose(
    Maybe.map(R.prop('kayttotarkoitusluokka-id')),
    Maybe.chain(Maybe.findById(R.__, alakayttotarkoitusluokat))
  )(alakayttotarkoitusluokkaId);

export const filterAlakayttotarkoitusLuokat = R.curry(
  (kayttotarkoitusluokkaId, alakayttotarkoitusluokat) =>
    R.filter(
      Maybe.map(
        R.propEq('kayttotarkoitusluokka-id'),
        kayttotarkoitusluokkaId
      ).orSome(R.T),
      alakayttotarkoitusluokat
    )
);

export const findAlakayttotarkoitusluokkaId = (
  kayttotarkoitusluokkaId,
  alakayttotarkoitusluokat
) => {
  const alaluokat = filterAlakayttotarkoitusLuokat(
    kayttotarkoitusluokkaId,
    alakayttotarkoitusluokat
  );
  return R.length(alaluokat) === 1 ? Maybe.Some(alaluokat[0].id) : Maybe.None();
};

export const validators = deep.map(
  R.compose(R.complement(R.isNil), R.prop('validators')),
  R.prop('validators')
);

export const unnestValidation = R.compose(
  R.when(Either.isEither, Maybe.None),
  R.when(R.allPass([Either.isEither, Either.isRight]), Either.right)
);

export const energiatodistusPath = R.curry((path, et) =>
  R.compose(unnestValidation, R.path(path))(et)
);

export const calculatePaths = R.curry((calcFn, firstPath, secondPath, et) =>
  R.converge(R.lift(calcFn), [
    energiatodistusPath(firstPath),
    energiatodistusPath(secondPath)
  ])(et)
);

const rakennusvaippa = R.path(['lahtotiedot', 'rakennusvaippa']);

const fieldsWithUA = [
  'ulkoseinat',
  'ylapohja',
  'alapohja',
  'ikkunat',
  'ulkoovet'
];

export const rakennusvaippaUA = R.compose(
  R.converge(R.merge, [
    R.compose(R.map(unnestValidation), R.pick(['kylmasillat-UA'])),
    R.compose(
      R.map(
        R.compose(
          R.apply(R.lift(R.multiply)),
          R.map(unnestValidation),
          R.props(['ala', 'U'])
        )
      ),
      R.pick(fieldsWithUA)
    )
  ]),
  rakennusvaippa
);

const teknisetJarjestelmat = R.path(['tulokset', 'tekniset-jarjestelmat']);

const fieldsWithSahko = [
  'jaahdytys',
  'kayttoveden-valmistus',
  'tilojen-lammitys',
  'tuloilman-lammitys'
];

const fieldsWithLampo = [
  'jaahdytys',
  'kayttoveden-valmistus',
  'tilojen-lammitys',
  'tuloilman-lammitys'
];

export const teknistenJarjestelmienSahkot = R.compose(
  R.map(unnestValidation),
  R.converge(R.merge, [
    R.pick(['iv-sahko', 'kuluttajalaitteet-ja-valaistus-sahko']),
    R.compose(R.map(R.prop('sahko')), R.pick(fieldsWithSahko))
  ]),
  teknisetJarjestelmat
);

export const teknistenJarjestelmienLammot = R.compose(
  R.map(R.compose(unnestValidation, R.prop('lampo'))),
  R.pick(fieldsWithLampo),
  teknisetJarjestelmat
);

export const teknistenJarjestelmienKaukojaahdytys = R.compose(
  R.map(R.compose(unnestValidation, R.prop('kaukojaahdytys'))),
  R.pick(['jaahdytys']),
  teknisetJarjestelmat
);

export const sumEtValues = R.compose(
  R.reduce(R.lift(R.add), Maybe.of(0)),
  R.filter(Maybe.isSome),
  R.values
);

export const partOfSum = R.curry((sum, value) => R.lift(R.divide)(value, sum));

export const energiamuotokertoimet = () => ({
  2018: {
    'fossiilinen-polttoaine': Maybe.Some(1),
    kaukojaahdytys: Maybe.Some(0.28),
    kaukolampo: Maybe.Some(0.5),
    sahko: Maybe.Some(1.2),
    'uusiutuva-polttoaine': Maybe.Some(0.5)
  },
  2013: {
    'fossiilinen-polttoaine': Maybe.Some(1),
    kaukojaahdytys: Maybe.Some(0.4),
    kaukolampo: Maybe.Some(0.7),
    sahko: Maybe.Some(1.7),
    'uusiutuva-polttoaine': Maybe.Some(0.5)
  }
});

const fieldsWithErittelyOstoenergia = [
  'kaukolampo',
  'sahko',
  'uusiutuva-polttoaine',
  'fossiilinen-polttoaine',
  'kaukojaahdytys'
];

const kaytettavatEnergiamuodot = R.path([
  'tulokset',
  'kaytettavat-energiamuodot'
]);

export const ostoenergiat = R.compose(
  R.map(unnestValidation),
  R.pick(fieldsWithErittelyOstoenergia),
  kaytettavatEnergiamuodot
);

export const muutOstoenergiat = R.compose(
  objects.mapKeys(key => 'muu-' + key),
  R.map(unnestValidation),
  R.map(R.prop('ostoenergia')),
  R.defaultTo([]),
  R.path(['tulokset', 'kaytettavat-energiamuodot', 'muu'])
);

export const muutEnergiamuotokertoimet = R.compose(
  objects.mapKeys(key => 'muu-' + key),
  R.map(unnestValidation),
  R.map(R.prop('muotokerroin')),
  R.defaultTo([]),
  R.path(['tulokset', 'kaytettavat-energiamuodot', 'muu'])
);

export const multiplyWithKerroin = R.curry((kerroin, ostoenergiamaara) =>
  R.lift(R.multiply)(kerroin, ostoenergiamaara)
);

export const perLammitettyNettoala = R.curry((energiatodistus, values) =>
  R.compose(
    R.map(R.__, values),
    R.lift(R.flip(R.divide)),
    energiatodistusPath(['lahtotiedot', 'lammitetty-nettoala'])
  )(energiatodistus)
);

export const energiaPerLammitettyNettoala = energiaPath =>
  R.compose(
    R.map(fxmath.round(1)),
    R.converge(R.lift(R.divide), [
      energiatodistusPath(energiaPath),
      energiatodistusPath(['lahtotiedot', 'lammitetty-nettoala'])
    ])
  );

const fieldsWithUusiutuvaOmavaraisenergia = [
  'aurinkosahko',
  'tuulisahko',
  'aurinkolampo',
  'muulampo',
  'muusahko',
  'lampopumppu'
];

const uusiutuvatOmavaraisenergiat = R.path([
  'tulokset',
  'uusiutuvat-omavaraisenergiat'
]);

export const omavaraisenergiat = R.compose(
  R.map(unnestValidation),
  R.pick(fieldsWithUusiutuvaOmavaraisenergia),
  uusiutuvatOmavaraisenergiat
);

const fieldsWithNettotarve = [
  'tilojen-lammitys-vuosikulutus',
  'ilmanvaihdon-lammitys-vuosikulutus',
  'kayttoveden-valmistus-vuosikulutus',
  'jaahdytys-vuosikulutus'
];

const nettotarve = R.path(['tulokset', 'nettotarve']);

export const nettotarpeet = R.compose(
  R.map(unnestValidation),
  R.pick(fieldsWithNettotarve),
  nettotarve
);

const fieldsWithLampokuorma = [
  'aurinko',
  'ihmiset',
  'kuluttajalaitteet',
  'valaistus',
  'kvesi'
];

const lampokuormat = R.path(['tulokset', 'lampokuormat']);

export const kuormat = R.compose(
  R.map(unnestValidation),
  R.pick(fieldsWithLampokuorma),
  lampokuormat
);

const fieldsWithOstoenergia = [
  'kaukolampo-vuosikulutus',
  'kokonaissahko-vuosikulutus',
  'kiinteistosahko-vuosikulutus',
  'kayttajasahko-vuosikulutus',
  'kaukojaahdytys-vuosikulutus'
];

const ostettuEnergia = R.path([
  'toteutunut-ostoenergiankulutus',
  'ostettu-energia'
]);

export const ostetutEnergiamuodot = R.compose(
  R.map(unnestValidation),
  R.pick(fieldsWithOstoenergia),
  ostettuEnergia
);

const fieldsWithToteutunutOstoenergia = [
  'sahko-vuosikulutus-yhteensa',
  'kaukolampo-vuosikulutus-yhteensa',
  'polttoaineet-vuosikulutus-yhteensa',
  'kaukojaahdytys-vuosikulutus-yhteensa'
];

const toteutunutOstoenergia = R.path(['toteutunut-ostoenergiankulutus']);

export const toteutuneetOstoenergiat = R.compose(
  R.map(unnestValidation),
  R.pick(fieldsWithToteutunutOstoenergia),
  toteutunutOstoenergia
);

const fieldsWithOstettuPolttoaine = [
  'kevyt-polttooljy',
  'pilkkeet-havu-sekapuu',
  'pilkkeet-koivu',
  'puupelletit'
];

const ostetutPolttoaineet = R.path([
  'toteutunut-ostoenergiankulutus',
  'ostetut-polttoaineet'
]);

export const polttoaineet = R.compose(
  R.map(unnestValidation),
  R.pick(fieldsWithOstettuPolttoaine),
  ostetutPolttoaineet
);

const vapaaPolttoaine = R.path([
  'toteutunut-ostoenergiankulutus',
  'ostetut-polttoaineet',
  'muu'
]);

export const vapaatPolttoaineet = R.compose(
  R.map(unnestValidation),
  R.map(R.prop('maara-vuodessa')),
  vapaaPolttoaine
);

export const vapaatKertoimet = R.compose(
  R.map(unnestValidation),
  R.map(R.prop('muunnoskerroin')),
  vapaaPolttoaine
);

const tilat = [
  'draft',
  'in-signing',
  'signed',
  'discarded',
  'replaced',
  'deleted'
];

export const tila = R.compose(R.map(parseInt), R.invertObj)(tilat);

export const tilaKey = id => tilat[id];

const kielisyydet = ['fi', 'sv', 'bilingual'];

export const kielisyys = R.compose(R.map(parseInt), R.invertObj)(kielisyydet);

export const kielisyysKey = id => kielisyydet[id];

export const viimeinenVoimassaolo = R.compose(
  R.map(paattymisaika => dfns.subDays(dfns.parseISO(paattymisaika), 1)),
  R.prop('voimassaolo-paattymisaika')
);
