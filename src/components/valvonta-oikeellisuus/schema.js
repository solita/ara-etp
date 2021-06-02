import * as R from 'ramda';

import * as Validation from '@Utility/validation';

import * as Toimenpiteet from './toimenpiteet';

const isDescriptionRequired = R.anyPass([
  Toimenpiteet.isType(Toimenpiteet.type.anomaly),
  Toimenpiteet.isResponse
]);

const description = R.map(
  Validation.liftValidator,
  Validation.LimitedString(2, 4000)
);

const addRequiredValidator = when =>
  when ? R.prepend(Validation.isSome) : R.identity;

export const toimenpideSave = {
  publish: false,
  'deadline-date': [],
  'template-id': [],
  description: description,
  'severity-id': []
};

export const toimenpidePublish = (templates, toimenpide) =>
  R.evolve(
    {
      publish: R.T,
      'deadline-date': addRequiredValidator(
        Toimenpiteet.hasDeadline(toimenpide)
      ),
      'template-id': addRequiredValidator(!R.isEmpty(templates)),
      description: addRequiredValidator(isDescriptionRequired(toimenpide)),
      'severity-id': addRequiredValidator(
        Toimenpiteet.isAuditReport(toimenpide)
      )
    },
    toimenpideSave
  );
