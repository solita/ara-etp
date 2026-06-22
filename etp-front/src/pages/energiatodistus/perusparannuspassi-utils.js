import * as EM from '@/utils/either-maybe.js';
import * as R from 'ramda';

const pppRequiredValidation = (
  perusparannuspassi,
  pppvalidation,
  bypassValidationLimits
) =>
  bypassValidationLimits
    ? pppvalidation.requiredBypass
    : pppvalidation.requiredAll;

const pppRequiredVaihe = (
  perusparannuspassi,
  pppvalidation,
  bypassValidationLimits
) =>
  bypassValidationLimits ? pppvalidation.vaiheBypass : pppvalidation.vaiheAll;

export const pppRequired = (
  perusparannuspassi,
  pppvalidation,
  bypassValidationLimits,
  energiatodistus
) => {
  const pppRequiredFields = pppRequiredValidation(
    perusparannuspassi,
    pppvalidation,
    bypassValidationLimits
  );

  const filteredPppRequiredFields = energiatodistus
    ? R.reject(field => {
        const alaMapping = {
          'rakennuksen-perustiedot.ylapohja-ehdotettu-taso': [
            'lahtotiedot',
            'rakennusvaippa',
            'ylapohja',
            'ala'
          ],
          'rakennuksen-perustiedot.alapohja-ehdotettu-taso': [
            'lahtotiedot',
            'rakennusvaippa',
            'alapohja',
            'ala'
          ],
          'rakennuksen-perustiedot.ikkunat-ehdotettu-taso': [
            'lahtotiedot',
            'rakennusvaippa',
            'ikkunat',
            'ala'
          ],
          'rakennuksen-perustiedot.ulkoovet-ehdotettu-taso': [
            'lahtotiedot',
            'rakennusvaippa',
            'ulkoovet',
            'ala'
          ]
        };
        const alaPath = alaMapping[field];
        if (!alaPath) return false;
        return EM.fold(false, v => v === 0, R.path(alaPath, energiatodistus));
      }, pppRequiredFields)
    : pppRequiredFields;

  const vaiheRequiredFields = pppRequiredVaihe(
    perusparannuspassi,
    pppvalidation,
    bypassValidationLimits
  );
  const validVaiheet = R.compose(
    R.map(R.prop('vaihe-nro')),
    R.filter(vaihe =>
      R.compose(R.isNotEmpty, EM.toArray)(vaihe.tulokset['vaiheen-alku-pvm'])
    )
  )(perusparannuspassi.vaiheet);
  if (R.isEmpty(validVaiheet)) {
    return R.concat(filteredPppRequiredFields, [
      'vaiheet.0.tulokset.vaiheen-alku-pvm'
    ]);
  } else {
    const vaiheRequireds = R.compose(
      R.flatten,
      R.map(vaiheNro =>
        R.map(requiredField =>
          R.concat('vaiheet.' + (vaiheNro - 1) + '.', requiredField)
        )(vaiheRequiredFields)
      )
    )(validVaiheet);

    return R.concat(filteredPppRequiredFields, vaiheRequireds);
  }
};

/**
 * Checks that filled vaiheen-alku-pvm fields are consecutive with no gaps.
 * Returns the field paths of the gap fields that need to be filled.
 */
export const pppVaiheGaps = perusparannuspassi => {
  const vaiheet = perusparannuspassi.vaiheet;
  const filled = R.map(
    vaihe =>
      R.compose(R.isNotEmpty, EM.toArray)(vaihe.tulokset['vaiheen-alku-pvm']),
    vaiheet
  );

  const lastFilledIndex = R.findLastIndex(R.identity, filled);
  if (lastFilledIndex <= 0) return [];

  return R.compose(
    R.map(i => 'vaiheet.' + i + '.tulokset.vaiheen-alku-pvm'),
    R.filter(i => !filled[i]),
    R.range(0)
  )(lastFilledIndex);
};
