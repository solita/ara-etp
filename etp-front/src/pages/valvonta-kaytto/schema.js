import * as R from 'ramda';

import * as Validation from '@Utility/validation';

import * as Toimenpiteet from './toimenpiteet';
import * as Maybe from '@Utility/maybe-utils';
import { isDecisionOrderActualDecision } from './toimenpiteet';

const OptionalLimitedString = (min, max) =>
  R.map(Validation.liftValidator, Validation.LimitedString(2, 200));

export const kohde = {
  rakennustunnus: [
    Validation.liftValidator(Validation.rakennustunnusValidator)
  ],
  postinumero: [Validation.isSome],
  katuosoite: Validation.RequiredString(2, 200),
  ilmoituspaikka_description: OptionalLimitedString(2, 200),
  ilmoitustunnus: OptionalLimitedString(2, 200)
};

const osapuoli = {
  email: [Validation.liftValidator(Validation.emailValidator)],
  puhelin: OptionalLimitedString(2, 200),
  'vastaanottajan-tarkenne': OptionalLimitedString(2, 200),
  jakeluosoite: OptionalLimitedString(2, 200),
  postinumero: [],
  postitoimipaikka: OptionalLimitedString(2, 200),
  'rooli-description': OptionalLimitedString(2, 200),
  'toimitustapa-description': OptionalLimitedString(2, 200)
};

export const henkilo = R.mergeLeft(osapuoli, {
  henkilotunnus: [Validation.liftValidator(Validation.henkilotunnusValidator)],
  etunimi: Validation.RequiredString(2, 200),
  sukunimi: Validation.RequiredString(2, 200)
});

export const yritys = R.mergeLeft(osapuoli, {
  ytunnus: [Validation.liftValidator(Validation.ytunnusValidator)],
  nimi: Validation.RequiredString(2, 200)
});

export const appendPostinumeroValidatorForCountry = (osapuoli, schema) =>
  R.over(
    R.lensProp('postinumero'),
    Maybe.exists(R.equals('FI'), osapuoli.maa)
      ? R.append(Validation.liftValidator(Validation.postinumeroFIValidator))
      : R.concat(
          R.__,
          R.map(Validation.liftValidator, Validation.LimitedString(2, 20))
        ),
    schema
  );

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
  'severity-id': [],
  'type-specific-data': {
    fine: Validation.MaybeInterval(0, Number.MAX_VALUE),
    'osapuoli-specific-data': [
      {
        osapuoli: { id: [], type: [] },
        'hallinto-oikeus-id': Validation.MaybeInterval(0, 5),
        'karajaoikeus-id': Validation.MaybeInterval(0, 19),
        'haastemies-email': [
          Validation.liftValidator(Validation.emailValidator)
        ],
        'recipient-answered': [Validation.isBoolean],
        'answer-commentary-fi': description,
        'answer-commentary-sv': description,
        'statement-fi': description,
        'statement-sv': description
      }
    ],
    'department-head-title-fi': description,
    'department-head-title-sv': description,
    'department-head-name': description
  }
};

const addRequiredValidatorToFieldsWhen = (when, fields) => osapuoliData =>
  R.reduce(
    (acc, field) => {
      return R.over(R.lensProp(field), addRequiredValidator(when), acc);
    },
    osapuoliData,
    fields
  );

export const toimenpidePublish = (templates, toimenpide) =>
  R.evolve(
    {
      publish: R.T,
      'deadline-date': addRequiredValidator(
        Toimenpiteet.hasDeadline(toimenpide)
      ),
      'template-id': addRequiredValidator(!R.isEmpty(templates)),
      'type-specific-data': {
        fine: addRequiredValidator(Toimenpiteet.hasFine(toimenpide)),
        'osapuoli-specific-data': osapuoliSpecificSchema =>
          R.addIndex(R.map)(
            (item, index) => {
              const hasDocument = R.path(
                [
                  'type-specific-data',
                  'osapuoli-specific-data',
                  index,
                  'document'
                ],
                toimenpide
              );

              const recipientAnswered = R.path(
                [
                  'type-specific-data',
                  'osapuoli-specific-data',
                  index,
                  'recipient-answered'
                ],
                toimenpide
              );
              return R.compose(
                addRequiredValidatorToFieldsWhen(
                  (Toimenpiteet.isDecisionOrderActualDecision(toimenpide) ||
                    Toimenpiteet.isPenaltyDecisionActualDecision(toimenpide)) &&
                    recipientAnswered,
                  [
                    'answer-commentary-sv',
                    'answer-commentary-fi',
                    'statement-sv',
                    'statement-fi'
                  ]
                ),
                addRequiredValidatorToFieldsWhen(
                  (Toimenpiteet.isDecisionOrderActualDecision(toimenpide) ||
                    Toimenpiteet.isPenaltyDecisionActualDecision(toimenpide)) &&
                    hasDocument,
                  ['hallinto-oikeus-id']
                ),
                addRequiredValidatorToFieldsWhen(
                  Toimenpiteet.isNoticeBailiff(toimenpide) && hasDocument,
                  ['karajaoikeus-id', 'haastemies-email']
                )
              )(item);
            },
            R.map(
              R.always(osapuoliSpecificSchema[0]),
              R.range(
                0,
                R.length(
                  R.path(
                    ['type-specific-data', 'osapuoli-specific-data'],
                    toimenpide
                  )
                )
              )
            )
          ),
        'department-head-title-fi': addRequiredValidator(
          Toimenpiteet.isDecisionOrderActualDecision(toimenpide)
        ),
        'department-head-title-sv': addRequiredValidator(
          Toimenpiteet.isDecisionOrderActualDecision(toimenpide)
        ),
        'department-head-name': addRequiredValidator(
          Toimenpiteet.isDecisionOrderActualDecision(toimenpide)
        )
      }
    },
    toimenpideSave
  );

export const note = {
  description: Validation.LimitedString(2, 4000)
};
