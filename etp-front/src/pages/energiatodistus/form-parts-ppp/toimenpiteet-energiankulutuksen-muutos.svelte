<script>
  import * as R from 'ramda';
  import * as inputs from '@Pages/energiatodistus/inputs.js';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as EitherMaybe from '@Utility/either-maybe';
  import { _ } from '@Language/i18n';
  import VuosikulutusUnit from '@Pages/energiatodistus/form-parts/units/annual-energy';
  import HiilidioksidiekvivalenttiTonnitPerVuosiUnit from '@Pages/energiatodistus/form-parts/units/annual-co2-ekv-tons';
  import EurosPerVuosiUnit from '@Pages/energiatodistus/form-parts/units/annual-euros';
  import * as formats from '@Utility/formats.js';

  import H5 from '@Component/H/H5';

  export let energiatodistus;
  export let perusparannuspassi;
  export let vaihe;

  const i18n = $_;
  const vaiheIndex = vaihe['vaihe-nro'] - 1;

  const isFirstVaihe = R.equals(0);

  // TODO: AE-2663: Should this come from database or somewhere else?
  export const paastokertoimet = {
    kaukolampo: 0.059,
    sahko: 0.05,
    biopolttoaineet: 0.027,
    fossiiliset: 0.306,
    kaukojaahdytys: 0.014
  };

  const evaluateForCalculatedValues = R.curry(
    ({ energiatodistus, perusparannuspassi }, { calculate }) =>
      calculate({ energiatodistus, perusparannuspassi })
  );

  const evaluateForExtraCalculatedValues = R.curry(
    (
      { energiatodistus, perusparannuspassi, calculatedValues },
      { calculate }
    ) => calculate({ energiatodistus, perusparannuspassi, calculatedValues })
  );

  const pppTuloksetLensFor = x => R.lensPath(['tulokset', x]);

  const pppVaiheTuloksetLensFor = x => vaiheIndex =>
    R.lensPath(['vaiheet', vaiheIndex, 'tulokset', x]);

  const etTuloksetKaytettavatEnergiamuodotLensFor = x =>
    R.lensPath(['tulokset', 'kaytettavat-energiamuodot', x]);

  const ostoenergianTarveLenses = {
    kaukolampo: {
      pppLens: pppVaiheTuloksetLensFor('ostoenergian-tarve-kaukolampo'),
      etLens: etTuloksetKaytettavatEnergiamuodotLensFor('kaukolampo')
    },
    sahko: {
      pppLens: pppVaiheTuloksetLensFor('ostoenergian-tarve-sahko'),
      etLens: etTuloksetKaytettavatEnergiamuodotLensFor('sahko')
    },
    uusiutuvatPolttoaineet: {
      pppLens: pppVaiheTuloksetLensFor('ostoenergian-tarve-uusiutuvat-pat'),
      etLens: etTuloksetKaytettavatEnergiamuodotLensFor('uusiutuva-polttoaine')
    },
    fossiilisetPolttoaineet: {
      pppLens: pppVaiheTuloksetLensFor('ostoenergian-tarve-fossiiliset-pat'),
      etLens: etTuloksetKaytettavatEnergiamuodotLensFor(
        'fossiilinen-polttoaine'
      )
    },
    kaukojaahdytys: {
      pppLens: pppVaiheTuloksetLensFor('ostoenergian-tarve-kaukojaahdytys'),
      etLens: etTuloksetKaytettavatEnergiamuodotLensFor('kaukojaahdytys')
    },
    uusiutuvanEnergianHyodynnettyOsuus: {
      pppLens: pppVaiheTuloksetLensFor('uusiutuvan-energian-hyodynnetty-osuus')
    }
  };

  const toteutunutOstoenergiaLenses = {
    kaukolampo: {
      pppLens: pppVaiheTuloksetLensFor('toteutunut-ostoenergia-kaukolampo')
    },
    sahko: {
      pppLens: pppVaiheTuloksetLensFor('toteutunut-ostoenergia-sahko')
    },
    uusiutuvatPolttoaineet: {
      pppLens: pppVaiheTuloksetLensFor('toteutunut-ostoenergia-uusiutuvat-pat')
    },
    fossiilisetPolttoaineet: {
      pppLens: pppVaiheTuloksetLensFor('toteutunut-ostoenergia-fossiiliset-pat')
    },
    kaukojaahdytys: {
      pppLens: pppVaiheTuloksetLensFor('toteutunut-ostoenergia-kaukojaahdytys')
    }
  };

  const simpleDiffCalculation =
    ({ etLens, pppLens }) =>
    ({ energiatodistus, perusparannuspassi }) => {
      if (isFirstVaihe(vaiheIndex))
        return R.compose(R.lift, R.lift)(R.subtract)(
          R.view(pppLens(vaiheIndex), perusparannuspassi),
          R.view(etLens, energiatodistus)
        );
      else {
        return R.compose(R.lift, R.lift)(R.subtract)(
          R.view(pppLens(vaiheIndex), perusparannuspassi),
          R.view(pppLens(vaiheIndex - 1), perusparannuspassi)
        );
      }
    };

  const calculatedValuesSpecs = {
    kaukolampo: {
      format: formats.optionalNumber,
      calculate: simpleDiffCalculation(ostoenergianTarveLenses.kaukolampo)
    },
    sahko: {
      format: formats.optionalNumber,
      calculate: simpleDiffCalculation(ostoenergianTarveLenses.sahko)
    },
    uusiutuvatPolttoaineet: {
      format: formats.optionalNumber,
      calculate: simpleDiffCalculation(
        ostoenergianTarveLenses.uusiutuvatPolttoaineet
      )
    },
    fossiilisetPolttoaineet: {
      format: formats.optionalNumber,
      calculate: simpleDiffCalculation(
        ostoenergianTarveLenses.fossiilisetPolttoaineet
      )
    },
    kaukojaahdytys: {
      format: formats.optionalNumber,
      calculate: simpleDiffCalculation(ostoenergianTarveLenses.kaukojaahdytys)
    },
    ostoenergianKokonaistarveVaiheenJalkeen: {
      format: formats.optionalNumber,
      calculate: ({ perusparannuspassi }) => {
        const values = R.map(R.view(R.__, perusparannuspassi))([
          ostoenergianTarveLenses.kaukolampo.pppLens(vaiheIndex),
          ostoenergianTarveLenses.sahko.pppLens(vaiheIndex),
          ostoenergianTarveLenses.uusiutuvatPolttoaineet.pppLens(vaiheIndex),
          ostoenergianTarveLenses.fossiilisetPolttoaineet.pppLens(vaiheIndex),
          ostoenergianTarveLenses.kaukojaahdytys.pppLens(vaiheIndex)
        ]);

        return R.compose(
          R.map(R.map(R.sum)),
          R.map(R.ifElse(R.isEmpty, Maybe.None, R.sequence(Maybe.of))),
          R.map(R.filter(Maybe.isSome)),
          R.sequence(Either.of)
        )(values);
      }
    },
    ostoenergianKokonaistarveVaiheenJalkeenToteutunutKulutus: {
      format: formats.optionalNumber,
      calculate: ({ perusparannuspassi }) => {
        const values = R.map(R.view(R.__, perusparannuspassi))([
          toteutunutOstoenergiaLenses.kaukolampo.pppLens(vaiheIndex),
          toteutunutOstoenergiaLenses.sahko.pppLens(vaiheIndex),
          toteutunutOstoenergiaLenses.uusiutuvatPolttoaineet.pppLens(
            vaiheIndex
          ),
          toteutunutOstoenergiaLenses.fossiilisetPolttoaineet.pppLens(
            vaiheIndex
          ),
          toteutunutOstoenergiaLenses.kaukojaahdytys.pppLens(vaiheIndex)
        ]);

        return R.compose(
          R.map(R.map(R.sum)),
          R.map(R.ifElse(R.isEmpty, Maybe.None, R.sequence(Maybe.of))),
          R.map(R.filter(Maybe.isSome)),
          R.sequence(Either.of)
        )(values);
      }
    },
    toteutuneenOstoenergianVuotuinenEnergiakustannusArvio: {
      format: formats.optionalNumber,
      calculate: ({ perusparannuspassi }) => {
        const values = [
          R.compose(R.lift, R.lift)(R.multiply)(
            R.view(
              ostoenergianTarveLenses.kaukolampo.pppLens(vaiheIndex),
              perusparannuspassi
            ),
            R.view(pppTuloksetLensFor('kaukolampo-hinta'), perusparannuspassi)
          ),
          R.compose(R.lift, R.lift)(R.multiply)(
            R.view(
              ostoenergianTarveLenses.sahko.pppLens(vaiheIndex),
              perusparannuspassi
            ),
            R.view(pppTuloksetLensFor('sahko-hinta'), perusparannuspassi)
          ),
          R.compose(R.lift, R.lift)(R.multiply)(
            R.view(
              ostoenergianTarveLenses.uusiutuvatPolttoaineet.pppLens(
                vaiheIndex
              ),
              perusparannuspassi
            ),
            R.view(
              pppTuloksetLensFor('uusiutuvat-pat-hinta'),
              perusparannuspassi
            )
          ),
          R.compose(R.lift, R.lift)(R.multiply)(
            R.view(
              ostoenergianTarveLenses.fossiilisetPolttoaineet.pppLens(
                vaiheIndex
              ),
              perusparannuspassi
            ),
            R.view(
              pppTuloksetLensFor('fossiiliset-pat-hinta'),
              perusparannuspassi
            )
          ),
          R.compose(R.lift, R.lift)(R.multiply)(
            R.view(
              ostoenergianTarveLenses.kaukojaahdytys.pppLens(vaiheIndex),
              perusparannuspassi
            ),
            R.view(
              pppTuloksetLensFor('kaukojaahdytys-hinta'),
              perusparannuspassi
            )
          )
        ];

        return R.compose(
          R.map(R.map(R.sum)),
          R.map(R.ifElse(R.isEmpty, Maybe.None, R.sequence(Maybe.of))),
          R.map(R.filter(Maybe.isSome)),
          R.sequence(Either.of)
        )(values);
      }
    },
    energiankaytostaAiheutuvatHiilidioksiidipaastot: {
      format: formats.optionalNumber,
      calculate: ({ perusparannuspassi }) => {
        const values = R.map(R.applyTo(R.view(R.__, perusparannuspassi)))([
          R.compose(
            R.map(R.map(R.multiply(paastokertoimet.kaukolampo))),
            R.applyTo(ostoenergianTarveLenses.kaukolampo.pppLens(vaiheIndex))
          ),
          R.compose(
            R.map(R.map(R.multiply(paastokertoimet.sahko))),
            R.applyTo(ostoenergianTarveLenses.sahko.pppLens(vaiheIndex))
          ),
          R.compose(
            R.map(R.map(R.multiply(paastokertoimet.biopolttoaineet))),
            R.applyTo(
              ostoenergianTarveLenses.uusiutuvatPolttoaineet.pppLens(vaiheIndex)
            )
          ),
          R.compose(
            R.map(R.map(R.multiply(paastokertoimet.fossiiliset))),
            R.applyTo(
              ostoenergianTarveLenses.fossiilisetPolttoaineet.pppLens(
                vaiheIndex
              )
            )
          ),
          R.compose(
            R.map(R.map(R.multiply(paastokertoimet.kaukojaahdytys))),
            R.applyTo(
              ostoenergianTarveLenses.kaukojaahdytys.pppLens(vaiheIndex)
            )
          )
        ]);

        return R.compose(
          R.map(R.map(R.divide(R.__, 1000))),
          R.map(R.map(R.sum)),
          R.map(R.ifElse(R.isEmpty, Maybe.None, R.sequence(Maybe.of))),
          R.map(R.filter(Maybe.isSome)),
          R.sequence(Either.of)
        )(values);
      }
    }
  };

  const extraCalculatedValuesSpecs = {
    uusiutuvanEnergianOsuusOstoenergianKokonaistarpeesta: {
      format: Maybe.cata('', formats.percentFormat),
      calculate: ({ calculatedValues, perusparannuspassi }) => {
        const value = R.view(
          ostoenergianTarveLenses.uusiutuvanEnergianHyodynnettyOsuus.pppLens(
            vaiheIndex
          ),
          perusparannuspassi
        );
        const otherValue = R.view(
          R.lensPath(['ostoenergianKokonaistarveVaiheenJalkeen']),
          calculatedValues
        );

        return R.compose(R.lift, R.lift)(R.divide)(value, otherValue);
      }
    }
  };

  $: calculatedValues = R.map(
    evaluateForCalculatedValues({ energiatodistus, perusparannuspassi }),
    calculatedValuesSpecs
  );
  $: extraCalculatedValues = R.map(
    evaluateForExtraCalculatedValues({
      energiatodistus,
      perusparannuspassi,
      calculatedValues
    }),
    extraCalculatedValuesSpecs
  );
</script>

<div>
  <H5
    text={i18n(
      'perusparannuspassi.vaiheet.0.tulokset.energiankulutuksen-muutos.header'
    )} />

  <dl class="ppp-description-list">
    <dt>
      {i18n(
        'perusparannuspassi.vaiheet.0.tulokset.energiankulutuksen-muutos.ostoenergian-tarve-kaukolampo-muutos'
      )}
    </dt>
    <dd>
      {inputs.viewValueFormatted({
        model: calculatedValues,
        schema: calculatedValuesSpecs,
        valueOnEmpty: '-',
        path: ['kaukolampo']
      })}
      {#each EitherMaybe.toArray(calculatedValues.kaukolampo) as _}
        <VuosikulutusUnit />
      {/each}
    </dd>
    <dt>
      {i18n(
        'perusparannuspassi.vaiheet.0.tulokset.energiankulutuksen-muutos.ostoenergian-tarve-sahko-muutos'
      )}
    </dt>
    <dd>
      {inputs.viewValueFormatted({
        model: calculatedValues,
        schema: calculatedValuesSpecs,
        valueOnEmpty: '-',
        path: ['sahko']
      })}
      {#each EitherMaybe.toArray(calculatedValues.sahko) as _}
        <VuosikulutusUnit />
      {/each}
    </dd>
    <dt>
      {i18n(
        'perusparannuspassi.vaiheet.0.tulokset.energiankulutuksen-muutos.ostoenergian-tarve-uusiutuvat-pat-muutos'
      )}
    </dt>
    <dd>
      {inputs.viewValueFormatted({
        model: calculatedValues,
        schema: calculatedValuesSpecs,
        valueOnEmpty: '-',
        path: ['uusiutuvatPolttoaineet']
      })}
      {#each EitherMaybe.toArray(calculatedValues.uusiutuvatPolttoaineet) as _}
        <VuosikulutusUnit />
      {/each}
    </dd>
    <dt>
      {i18n(
        'perusparannuspassi.vaiheet.0.tulokset.energiankulutuksen-muutos.ostoenergian-tarve-fossiiliset-pat-muutos'
      )}
    </dt>
    <dd>
      {inputs.viewValueFormatted({
        model: calculatedValues,
        schema: calculatedValuesSpecs,
        valueOnEmpty: '-',
        path: ['fossiilisetPolttoaineet']
      })}
      {#each EitherMaybe.toArray(calculatedValues.fossiilisetPolttoaineet) as _}
        <VuosikulutusUnit />
      {/each}
    </dd>
    <dt>
      {i18n(
        'perusparannuspassi.vaiheet.0.tulokset.energiankulutuksen-muutos.ostoenergian-tarve-kaukojaahdytys-muutos'
      )}
    </dt>
    <dd>
      {inputs.viewValueFormatted({
        model: calculatedValues,
        schema: calculatedValuesSpecs,
        valueOnEmpty: '-',
        path: ['kaukojaahdytys']
      })}
      {#each EitherMaybe.toArray(calculatedValues.kaukojaahdytys) as _}
        <VuosikulutusUnit />
      {/each}
    </dd>
    <dt>
      {i18n(
        'perusparannuspassi.vaiheet.0.tulokset.ostoenergian-kokonaistarve-vaiheen-jalkeen'
      )}
    </dt>
    <dd>
      {inputs.viewValueFormatted({
        model: calculatedValues,
        schema: calculatedValuesSpecs,
        valueOnEmpty: '-',
        path: ['ostoenergianKokonaistarveVaiheenJalkeen']
      })}
      {#each EitherMaybe.toArray(calculatedValues.ostoenergianKokonaistarveVaiheenJalkeen) as _}
        <VuosikulutusUnit />
      {/each}
    </dd>
    <dt>
      {i18n(
        'perusparannuspassi.vaiheet.0.tulokset.uusiutuvan-energian-osuus-ostoenergian-kokonaistarpeesta'
      )}
    </dt>
    <dd>
      {inputs.viewValueFormatted({
        model: extraCalculatedValues,
        schema: extraCalculatedValuesSpecs,
        valueOnEmpty: '-',
        path: ['uusiutuvanEnergianOsuusOstoenergianKokonaistarpeesta']
      })}
    </dd>
    <dt>
      {i18n(
        'perusparannuspassi.vaiheet.0.tulokset.ostoenergian-kokonaistarve-vaiheen-jalkeen-toteutunut-kulutus'
      )}
    </dt>
    <dd>
      {inputs.viewValueFormatted({
        model: calculatedValues,
        schema: calculatedValuesSpecs,
        valueOnEmpty: '-',
        path: ['ostoenergianKokonaistarveVaiheenJalkeenToteutunutKulutus']
      })}
      {#each EitherMaybe.toArray(calculatedValues.ostoenergianKokonaistarveVaiheenJalkeenToteutunutKulutus) as _}
        <VuosikulutusUnit />
      {/each}
    </dd>
    <dt>
      {i18n(
        'perusparannuspassi.vaiheet.0.tulokset.toteutuneen-ostoenergian-vuotuinen-energiakustannus-arvio'
      )}
    </dt>
    <dd>
      {inputs.viewValueFormatted({
        model: calculatedValues,
        schema: calculatedValuesSpecs,
        valueOnEmpty: '-',
        path: ['toteutuneenOstoenergianVuotuinenEnergiakustannusArvio']
      })}
      {#each EitherMaybe.toArray(calculatedValues.toteutuneenOstoenergianVuotuinenEnergiakustannusArvio) as _}
        <EurosPerVuosiUnit />
      {/each}
    </dd>
    <dt>
      {i18n(
        'perusparannuspassi.vaiheet.0.tulokset.energiankaytosta-aiheutuvat-hiilidioksidipaastot'
      )}
    </dt>
    <dd>
      {inputs.viewValueFormatted({
        model: calculatedValues,
        schema: calculatedValuesSpecs,
        valueOnEmpty: '-',
        path: ['energiankaytostaAiheutuvatHiilidioksiidipaastot']
      })}
      {#each EitherMaybe.toArray(calculatedValues.energiankaytostaAiheutuvatHiilidioksiidipaastot) as _}
        <HiilidioksidiekvivalenttiTonnitPerVuosiUnit />
      {/each}
    </dd>
  </dl>
</div>
