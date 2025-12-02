import * as EM from '@/utils/either-maybe.js';
import * as R from 'ramda';

const pppRequiredValidation = (perusparannuspassi, pppvalidation, bypassValidationLimits) =>
    bypassValidationLimits
    ? pppvalidation.requiredBypass
    : pppvalidation.requiredAll;

const pppRequiredVaihe = (perusparannuspassi, pppvalidation, bypassValidationLimits) =>
    bypassValidationLimits
    ? pppvalidation.vaiheBypass
    : pppvalidation.vaiheAll;

export const pppRequired = (perusparannuspassi, pppvalidation, bypassValidationLimits) => {
  const pppRequiredFields = pppRequiredValidation(
    perusparannuspassi,
    pppvalidation,
    bypassValidationLimits
  );
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
    return R.concat(pppRequiredFields, ['vaiheet.0.tulokset.vaiheen-alku-pvm']);
  } else {
    const vaiheRequireds = R.compose(
      R.flatten,
      R.map(vaiheNro =>
        R.map(requiredField =>
          R.concat('vaiheet.' + (vaiheNro - 1) + '.', requiredField)
        )(vaiheRequiredFields)
      )
    )(validVaiheet);

    return R.concat(pppRequiredFields, vaiheRequireds);
  }
};
