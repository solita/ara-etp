import { assert } from 'chai';
import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as dfns from 'date-fns';
import * as Toimenpiteet from './toimenpiteet';
import { deserializeToimenpide } from '@Pages/valvonta-oikeellisuus/valvonta-api';

describe('Toimenpiteet: ', () => {
  describe('deadline', () => {
    it('is by default in one month for types 1 - 4', () => {
      R.range(1, 5).forEach(typeId => {
        assert.isTrue(
          dfns.isSameDay(
            dfns.addMonths(new Date(), 1),
            Maybe.get(Toimenpiteet.defaultDeadline(typeId))
          )
        );
      });
    });

    it('is by default two weeks for type 7', () => {
      assert.isTrue(
        dfns.isSameDay(
          dfns.addWeeks(new Date(), 2),
          Maybe.get(Toimenpiteet.defaultDeadline(7))
        )
      );
    });

    it('is by default 37 days for types 8, 9, 10, 15, 16 and 17', () => {
      [8, 9, 10, 15, 16, 17].forEach(typeId => {
        assert.isTrue(
          dfns.isSameDay(
            dfns.addDays(new Date(), 37),
            Maybe.get(Toimenpiteet.defaultDeadline(typeId))
          )
        );
      });
    });

    it('is by default 30 day for types 11, 18 and 19', () => {
      [11, 18, 19].forEach(typeId => {
        assert.isTrue(
          dfns.isSameDay(
            dfns.addDays(new Date(), 30),
            Maybe.get(Toimenpiteet.defaultDeadline(typeId))
          )
        );
      });
    });

    it('is by default 30 day for type 12', () => {
      assert.isTrue(
        dfns.isSameDay(
          dfns.addDays(new Date(), 30),
          Maybe.get(Toimenpiteet.defaultDeadline(12))
        )
      );
    });

    it('is by default two weeks for type 14', () => {
      assert.isTrue(
        dfns.isSameDay(
          dfns.addWeeks(new Date(), 2),
          Maybe.get(Toimenpiteet.defaultDeadline(14))
        )
      );
    });
  });

  describe('Käskypäätös / Kuulemiskirje', () => {
    it('id is mapped correctly to the type key', () => {
      assert.equal('decision-order-hearing-letter', Toimenpiteet.typeKey(7));
    });

    it('is a type with a deadline', () => {
      assert.isTrue(Toimenpiteet.hasDeadline({ 'type-id': 7 }));
    });
  });

  describe('Käskypäätös / varsinainen päätös', () => {
    it('id is mapped correctly to the type key', () => {
      assert.equal('decision-order-actual-decision', Toimenpiteet.typeKey(8));
    });

    it('is a type with a deadline', () => {
      assert.isTrue(Toimenpiteet.hasDeadline({ 'type-id': 8 }));
    });

    it('is recognized correctly with isActualDecision function', () => {
      assert.isTrue(
        Toimenpiteet.isDecisionOrderActualDecision({ 'type-id': 8 })
      );
      assert.isFalse(
        Toimenpiteet.isDecisionOrderActualDecision({ 'type-id': 7 })
      );
    });
  });

  describe('Toimenpide object is recognized correctly whether it is part of the given types', () => {
    assert.isTrue(Toimenpiteet.isToimenpideOfGivenTypes([7])({ 'type-id': 7 }));
    assert.isFalse(
      Toimenpiteet.isToimenpideOfGivenTypes([7])({ 'type-id': 1 })
    );
  });
});

describe('Käskypäätös / valitusajan odotus ja umpeutuminen', () => {
  it('id is mapped correctly to the type key', () => {
    assert.equal(
      'decision-order-waiting-for-deadline',
      Toimenpiteet.typeKey(12)
    );
  });

  it('is a type with a deadline', () => {
    assert.isTrue(Toimenpiteet.hasDeadline({ 'type-id': 12 }));
  });
});

describe('Sakkopäätös / Kuulemiskirje', () => {
  it('id is mapped correctly to the type key', () => {
    assert.equal('penalty-decision-hearing-letter', Toimenpiteet.typeKey(14));
  });

  it('is a type with a deadline', () => {
    assert.isTrue(Toimenpiteet.hasDeadline({ 'type-id': 14 }));
  });
});

describe('Sakkopäätös / Varsinainen päätös', () => {
  it('id is mapped correctly to the type key', () => {
    assert.equal('penalty-decision-actual-decision', Toimenpiteet.typeKey(15));
  });

  it('is a type with a deadline', () => {
    assert.isTrue(Toimenpiteet.hasDeadline({ 'type-id': 15 }));
  });
});

describe('Sakkopäätös / tiedoksianto (ensimmäinen postitus)', () => {
  it('id is mapped correctly to the type key', () => {
    assert.equal(
      'penalty-decision-notice-first-mailing',
      Toimenpiteet.typeKey(16)
    );
  });

  it('is a type with a deadline', () => {
    assert.isTrue(Toimenpiteet.hasDeadline({ 'type-id': 16 }));
  });
});

describe('Sakkopäätös / tiedoksianto (toinen postitus)', () => {
  it('id is mapped correctly to the type key', () => {
    assert.equal(
      'penalty-decision-notice-second-mailing',
      Toimenpiteet.typeKey(17)
    );
  });

  it('is a type with a deadline', () => {
    assert.isTrue(Toimenpiteet.hasDeadline({ 'type-id': 17 }));
  });
});

describe('Sakkopäätös / Valitusajan odotus ja umpeutuminen', () => {
  it('id is mapped correctly to the type key', () => {
    assert.equal(
      'penalty-decision-waiting-for-deadline',
      Toimenpiteet.typeKey(19)
    );
  });

  it('is a type with a deadline', () => {
    assert.isTrue(Toimenpiteet.hasDeadline({ 'type-id': 19 }));
  });
});

describe('Sakkopäätös / Tiedoksianto (Haastemies)', () => {
  it('id is mapped correctly to the type key', () => {
    assert.equal('penalty-decision-notice-bailiff', Toimenpiteet.typeKey(18));
  });

  it('is a type with a deadline', () => {
    assert.isTrue(Toimenpiteet.hasDeadline({ 'type-id': 18 }));
  });
});

describe('Sakkoluettelon lähetys menossa', () => {
  it('id is mapped correctly to the type key', () => {
    assert.equal('penalty-list-delivery-in-progress', Toimenpiteet.typeKey(21));
  });
});

describe('Given toimenpidetypes', () => {
  it('find the ids of manually deliverable types', () => {
    assert.deepEqual(
      Toimenpiteet.manuallyDeliverableToimenpideTypes([
        { id: 2, 'manually-deliverable': false },
        { id: 7, 'manually-deliverable': true },
        { id: 1, 'manually-deliverable': false },
        { id: 8, 'manually-deliverable': true }
      ]),
      [7, 8]
    );
  });

  it('find the ids of toimenpidetypes that allow comments', () => {
    assert.deepEqual(
      Toimenpiteet.toimenpideTypesThatAllowComments([
        { id: 2, 'allow-comments': false },
        { id: 7, 'allow-comments': true },
        { id: 1, 'allow-comments': false },
        { id: 8, 'allow-comments': true }
      ]),
      [7, 8]
    );
  });
});

describe('Empty toimenpide', () => {
  it('Contains correct keys for toimenpidetype 1', () => {
    const emptyToimenpide = Toimenpiteet.emptyToimenpide(1, [{}]);
    assert.deepEqual(Object.keys(emptyToimenpide), [
      'type-id',
      'publish-time',
      'deadline-date',
      'template-id',
      'description'
    ]);
  });

  it('Contains correct keys for toimenpidetype 7 which includes fine under type-specific-data', () => {
    const emptyToimenpide = Toimenpiteet.emptyToimenpide(7, [{}]);
    assert.deepEqual(Object.keys(emptyToimenpide), [
      'type-id',
      'publish-time',
      'deadline-date',
      'template-id',
      'description',
      'type-specific-data'
    ]);

    assert.deepEqual(Object.keys(emptyToimenpide['type-specific-data']), [
      'fine'
    ]);
  });

  it('Contains correct keys for toimenpidetype 8', () => {
    const emptyToimenpide = Toimenpiteet.emptyToimenpide(8, [{}], {
      osapuolis: [
        { id: 1, nimi: 'Emme hanki Energiatodistuksia Oyj' },
        {
          id: 7,
          etunimi: 'Marjo',
          sukunimi: 'Koivukuja'
        }
      ]
    });
    assert.deepEqual(Object.keys(emptyToimenpide), [
      'type-id',
      'publish-time',
      'deadline-date',
      'template-id',
      'description',
      'type-specific-data'
    ]);

    assert.deepEqual(Object.keys(emptyToimenpide['type-specific-data']), [
      'fine',
      'osapuoli-specific-data',
      'department-head-title-fi',
      'department-head-title-sv',
      'department-head-name'
    ]);

    // osapuoli-specific-data contains a list of objects with osapuoli,
    // associated hallinto-oikeus-id, whether the osapuoli answered the kuulemiskirje
    // and the answer-commentary and statement fields
    assert.deepEqual(
      R.path(['type-specific-data', 'osapuoli-specific-data'], emptyToimenpide),
      [
        {
          osapuoli: {
            id: 1,
            type: 'yritys'
          },
          'hallinto-oikeus-id': Maybe.None(),
          document: true,
          'recipient-answered': false,
          'answer-commentary-fi': Maybe.None(),
          'answer-commentary-sv': Maybe.None(),
          'statement-fi': Maybe.None(),
          'statement-sv': Maybe.None()
        },
        {
          osapuoli: {
            id: 7,
            type: 'henkilo'
          },
          'hallinto-oikeus-id': Maybe.None(),
          document: true,
          'recipient-answered': false,
          'answer-commentary-fi': Maybe.None(),
          'answer-commentary-sv': Maybe.None(),
          'statement-fi': Maybe.None(),
          'statement-sv': Maybe.None()
        }
      ]
    );
  });

  it('Contains correct keys for toimenpidetype 11', () => {
    const emptyToimenpide = Toimenpiteet.emptyToimenpide(11, [{}], {
      osapuolis: [{ id: 1, nimi: 'Emme hanki Energiatodistuksia Oyj' }]
    });
    assert.deepEqual(Object.keys(emptyToimenpide), [
      'type-id',
      'publish-time',
      'deadline-date',
      'template-id',
      'description',
      'type-specific-data'
    ]);

    assert.deepEqual(Object.keys(emptyToimenpide['type-specific-data']), [
      'osapuoli-specific-data'
    ]);

    // osapuoli-specific-data contains a list of objects with osapuoli,
    // associated hallinto-oikeus-id, whether the osapuoli answered the kuulemiskirje
    // and the answer-commentary and statement fields
    assert.deepEqual(
      R.path(['type-specific-data', 'osapuoli-specific-data'], emptyToimenpide),
      [
        {
          osapuoli: {
            id: 1,
            type: 'yritys'
          },
          'karajaoikeus-id': Maybe.None(),
          document: true,
          'haastemies-email': Maybe.None()
        }
      ]
    );
  });

  it('Contains correct keys for toimenpidetype 18', () => {
    const emptyToimenpide = Toimenpiteet.emptyToimenpide(18, [{}], {
      osapuolis: [{ id: 1, nimi: 'Emme hanki Energiatodistuksia Oyj' }]
    });
    assert.deepEqual(Object.keys(emptyToimenpide), [
      'type-id',
      'publish-time',
      'deadline-date',
      'template-id',
      'description',
      'type-specific-data'
    ]);

    assert.deepEqual(Object.keys(emptyToimenpide['type-specific-data']), [
      'osapuoli-specific-data'
    ]);

    // osapuoli-specific-data contains a list of objects with osapuoli,
    // associated hallinto-oikeus-id, whether the osapuoli answered the kuulemiskirje
    // and the answer-commentary and statement fields
    assert.deepEqual(
      R.path(['type-specific-data', 'osapuoli-specific-data'], emptyToimenpide),
      [
        {
          osapuoli: {
            id: 1,
            type: 'yritys'
          },
          'hallinto-oikeus-id': Maybe.None(),
          'karajaoikeus-id': Maybe.None(),
          document: true,
          'haastemies-email': Maybe.None()
        }
      ]
    );
  });

  it('Contains correct keys for toimenpidetype 14 which includes fine under type-specific-data', () => {
    const emptyToimenpide = Toimenpiteet.emptyToimenpide(14, [{}], {
      osapuolis: [
        {
          id: 6,
          etunimi: 'Janina',
          sukunimi: 'Mäkiaho'
        }
      ]
    });
    assert.deepEqual(Object.keys(emptyToimenpide), [
      'type-id',
      'publish-time',
      'deadline-date',
      'template-id',
      'description',
      'type-specific-data'
    ]);

    assert.deepEqual(Object.keys(emptyToimenpide['type-specific-data']), [
      'fine',
      'osapuoli-specific-data'
    ]);

    assert.deepEqual(
      R.path(['type-specific-data', 'osapuoli-specific-data'], emptyToimenpide),
      [
        {
          osapuoli: {
            id: 6,
            type: 'henkilo'
          },
          document: true
        }
      ]
    );
  });

  it('with a fine is recognized as having a fine', () => {
    const emptyToimenpide = Toimenpiteet.emptyToimenpide(7, [{}]);
    assert.isTrue(Toimenpiteet.hasFine(emptyToimenpide));
  });

  it('of type 7 has a default fine of 800', () => {
    const emptyToimenpide = Toimenpiteet.emptyToimenpide(7, [{}]);

    assert.equal(
      800,
      Maybe.get(R.path(['type-specific-data', 'fine'], emptyToimenpide))
    );
  });

  it('of type 8 has a default fine of 800 and no department head fields filled by default', () => {
    const emptyToimenpide = Toimenpiteet.emptyToimenpide(8, [{}]);
    assert.equal(
      800,
      Maybe.get(R.path(['type-specific-data', 'fine'], emptyToimenpide))
    );

    assert.isTrue(
      Maybe.isNone(
        R.path(
          ['type-specific-data', 'department-head-title-fi'],
          emptyToimenpide
        )
      )
    );

    assert.isTrue(
      Maybe.isNone(
        R.path(
          ['type-specific-data', 'department-head-title-sv'],
          emptyToimenpide
        )
      )
    );

    assert.isTrue(
      Maybe.isNone(
        R.path(['type-specific-data', 'department-head-name'], emptyToimenpide)
      )
    );
  });

  it('of type 8 can have its default values overridden', () => {
    const emptyToimenpide = Toimenpiteet.emptyToimenpide(8, [], {
      fine: 1000,
      departmentHeadTitleFi: 'Prefilled title',
      departmentHeadTitleSv: 'Prefilled title på Svenska',
      departmentHeadName: 'Prefilled Name'
    });

    assert.equal(
      1000,
      Maybe.get(R.path(['type-specific-data', 'fine'], emptyToimenpide))
    );

    assert.equal(
      'Prefilled title',
      Maybe.get(
        R.path(
          ['type-specific-data', 'department-head-title-fi'],
          emptyToimenpide
        )
      )
    );

    assert.equal(
      'Prefilled title på Svenska',
      Maybe.get(
        R.path(
          ['type-specific-data', 'department-head-title-sv'],
          emptyToimenpide
        )
      )
    );

    assert.equal(
      'Prefilled Name',
      Maybe.get(
        R.path(['type-specific-data', 'department-head-name'], emptyToimenpide)
      )
    );
  });

  it('of type 8 has a default fine of 800 when toimenpiteet does not contain a previous fine', () => {
    const toimenpiteet = [];
    const emptyToimenpide = Toimenpiteet.emptyToimenpide(8, [], {
      fine: Toimenpiteet.findFineFromToimenpiteet(
        Toimenpiteet.isDecisionOrderHearingLetter,
        toimenpiteet
      )
    });
    assert.equal(
      800,
      Maybe.get(R.path(['type-specific-data', 'fine'], emptyToimenpide))
    );
  });

  it('with a fine key but no value is recognized as having a fine', () => {
    let emptyToimenpide = Toimenpiteet.emptyToimenpide(7, [{}]);
    emptyToimenpide.fine = Maybe.fromNull(null);
    assert.isTrue(Toimenpiteet.hasFine(emptyToimenpide));
  });

  it('without a fine is recognized as not having a fine', () => {
    const emptyToimenpide = Toimenpiteet.emptyToimenpide(1, [{}]);
    assert.isFalse(Toimenpiteet.hasFine(emptyToimenpide));
  });

  it('Contains correct keys and default values for toimenpidetype 15', () => {
    const emptyToimenpide = Toimenpiteet.emptyToimenpide(15, [{}], {
      osapuolis: [
        { id: 1, etunimi: 'Jake', sukunimi: 'Mallinen' },
        { id: 7, nimi: 'Yritys' }
      ],
      defaultStatementFi: 'Statementin default-arvo voidaan antaa parametrina',
      defaultStatementSv:
        'Ruotsinkieliselle statementille voi antaa %s käyttäen kohdan mihin täydennetään osapuolen sukunimi tai yrityksen nimi'
    });
    assert.deepEqual(Object.keys(emptyToimenpide), [
      'type-id',
      'publish-time',
      'deadline-date',
      'template-id',
      'description',
      'type-specific-data'
    ]);

    assert.deepEqual(Object.keys(emptyToimenpide['type-specific-data']), [
      'fine',
      'osapuoli-specific-data',
      'department-head-title-fi',
      'department-head-title-sv',
      'department-head-name'
    ]);

    // osapuoli-specific-data contains a list of objects with osapuoli,
    // associated hallinto-oikeus-id, whether the osapuoli answered the kuulemiskirje
    // and the answer-commentary and statement fields
    assert.deepEqual(
      R.path(['type-specific-data', 'osapuoli-specific-data'], emptyToimenpide),
      [
        {
          osapuoli: {
            id: 1,
            type: 'henkilo'
          },
          'hallinto-oikeus-id': Maybe.None(),
          document: true,
          'recipient-answered': false,
          'answer-commentary-fi': Maybe.None(),
          'answer-commentary-sv': Maybe.None(),
          'statement-fi': Maybe.Some(
            'Statementin default-arvo voidaan antaa parametrina'
          ),
          'statement-sv': Maybe.Some(
            'Ruotsinkieliselle statementille voi antaa Mallinen käyttäen kohdan mihin täydennetään osapuolen sukunimi tai yrityksen nimi'
          )
        },
        {
          osapuoli: {
            id: 7,
            type: 'yritys'
          },
          'hallinto-oikeus-id': Maybe.None(),
          document: true,
          'recipient-answered': false,
          'answer-commentary-fi': Maybe.None(),
          'answer-commentary-sv': Maybe.None(),
          'statement-fi': Maybe.Some(
            'Statementin default-arvo voidaan antaa parametrina'
          ),
          'statement-sv': Maybe.Some(
            'Ruotsinkieliselle statementille voi antaa Yritys käyttäen kohdan mihin täydennetään osapuolen sukunimi tai yrityksen nimi'
          )
        }
      ]
    );
  });
});

describe('findFineFromToimenpiteet returns the fine present in the newest toimenpide of type 7', () => {
  it('when there is only one toimenpide of type 7', () => {
    const toimenpiteet = R.map(deserializeToimenpide, [
      {
        'type-id': 7,
        'create-time': '2023-08-17T06:56:31.747903Z',
        'type-specific-data': {
          fine: 1000
        }
      }
    ]);

    assert.equal(
      Toimenpiteet.findFineFromToimenpiteet(
        Toimenpiteet.isDecisionOrderHearingLetter,
        toimenpiteet
      ),
      1000
    );
  });

  it('when there are other toimenpiteet also present', () => {
    const toimenpiteet = R.map(deserializeToimenpide, [
      {
        'type-id': 1
      },
      {
        'type-id': 7,
        'create-time': '2023-08-18T06:56:31.747903Z',
        'type-specific-data': {
          fine: 2000
        }
      }
    ]);

    assert.equal(
      Toimenpiteet.findFineFromToimenpiteet(
        Toimenpiteet.isDecisionOrderHearingLetter,
        toimenpiteet
      ),
      2000
    );
  });

  it('when there are multiple toimenpiteet of type 7', () => {
    const toimenpiteet = R.map(deserializeToimenpide, [
      {
        'type-id': 1
      },
      {
        'type-id': 7,
        'create-time': '2023-08-17T06:56:31.747903Z',
        'type-specific-data': {
          fine: 1000
        }
      },
      {
        'type-id': 7,
        'create-time': '2023-08-18T06:56:31.747903Z',
        'type-specific-data': {
          fine: 3000
        }
      },
      {
        'type-id': 8,
        'create-time': '2023-08-17T06:56:31.747903Z',
        'type-specific-data': {
          fine: 666
        }
      },
      {
        'type-id': 7,
        'create-time': '2023-08-14T06:56:31.747903Z',
        'type-specific-data': {
          fine: 120
        }
      }
    ]);

    assert.equal(
      Toimenpiteet.findFineFromToimenpiteet(
        Toimenpiteet.isDecisionOrderHearingLetter,
        toimenpiteet
      ),
      3000
    );
  });
});

describe('documentExistsForOsapuoli', () => {
  it('returns false for for the henkilo osapuoli who has their document set to false, true for others', () => {
    const toimenpide = R.set(
      R.lensPath(['type-specific-data', 'osapuoli-specific-data']),
      [
        {
          osapuoli: {
            id: 1,
            type: 'yritys'
          },
          document: false
        },
        {
          osapuoli: {
            id: 1,
            type: 'henkilo'
          },
          'hallinto-oikeus-id': Maybe.Some(5),
          document: true
        },

        {
          osapuoli: {
            id: 3,
            type: 'henkilo'
          },
          'hallinto-oikeus-id': Maybe.Some(2),
          document: false
        },
        {
          osapuoli: {
            id: 7,
            type: 'henkilo'
          },
          'hallinto-oikeus-id': Maybe.Some(1),
          document: true
        }
      ],
      Toimenpiteet.emptyToimenpide(8, [])
    );

    assert.isTrue(
      Toimenpiteet.documentExistsForOsapuoli(toimenpide, 1, 'henkilo')
    );
    assert.isFalse(
      Toimenpiteet.documentExistsForOsapuoli(toimenpide, 1, 'yritys')
    );
    assert.isFalse(
      Toimenpiteet.documentExistsForOsapuoli(toimenpide, 3, 'henkilo')
    );
    assert.isTrue(
      Toimenpiteet.documentExistsForOsapuoli(toimenpide, 7, 'henkilo')
    );
  });
});

describe('toimenpideForOsapuoli', () => {
  it('returns the original toimenpide object but with the osapuoli-specific-data for other osapuolis removed', () => {
    const toimenpide = Toimenpiteet.emptyToimenpide(8, [], {
      osapuolis: [
        { id: 1, etunimi: 'Heimo', sukunimi: 'Järvenperä' },
        { id: 3, nimi: 'Asunnonomistajat Oy' },
        { id: 3, etunimi: 'Väärä', sukunimi: 'Osapuoli' },
        {
          id: 7,
          etunimi: 'Maaret',
          sukunimi: 'Laidanoja'
        }
      ]
    });

    const result = Toimenpiteet.toimenpideForOsapuoli(toimenpide, 3, 'yritys');
    assert.deepEqual(R.dissoc('deadline-date', result), {
      'type-id': 8,
      'publish-time': Maybe.None(),
      'template-id': Maybe.None(),
      description: Maybe.None(),
      'type-specific-data': {
        fine: Maybe.Some(800),
        'osapuoli-specific-data': [
          {
            osapuoli: {
              id: 3,
              type: 'yritys'
            },
            'recipient-answered': false,
            'answer-commentary-fi': Maybe.None(),
            'answer-commentary-sv': Maybe.None(),
            'statement-fi': Maybe.None(),
            'statement-sv': Maybe.None(),
            'hallinto-oikeus-id': Maybe.None(),
            document: true
          }
        ],
        'department-head-title-fi': Maybe.None(),
        'department-head-title-sv': Maybe.None(),
        'department-head-name': Maybe.None()
      }
    });
  });
});

describe('isNoticeBailiff', () => {
  it('returns true for typeId 11', () => {
    const kaskypaatosNoticeBailiffToimenpide = Toimenpiteet.emptyToimenpide(
      11,
      [{}]
    );
    assert.isTrue(
      Toimenpiteet.isNoticeBailiff(kaskypaatosNoticeBailiffToimenpide)
    );
  });
  it('returns true for typeId 18', () => {
    const sakkopaatosNoticeBailiffToimenpide = Toimenpiteet.emptyToimenpide(
      18,
      [{}]
    );
    assert.isTrue(
      Toimenpiteet.isNoticeBailiff(sakkopaatosNoticeBailiffToimenpide)
    );
  });
  it('returns false for some other typeId than 11 or 18', () => {
    const someOtherToimenpide = Toimenpiteet.emptyToimenpide(7, [{}]);
    assert.isFalse(Toimenpiteet.isNoticeBailiff(someOtherToimenpide));
  });
});

describe('isCorrectOsapuoli', () => {
  it('returns true when both osapuoli-id and osapuoli-type match', () => {
    assert.isTrue(
      Toimenpiteet.findOsapuoli(1, 'henkilo')({ id: 1, type: 'henkilo' })
    );
  });

  it('returns false when neither osapuoli-id or osapuoli-type match', () => {
    assert.isFalse(
      Toimenpiteet.findOsapuoli(1, 'henkilo')({ id: 2, type: 'yritys' })
    );
  });

  it('returns false when only osapuoli-id matches', () => {
    assert.isFalse(
      Toimenpiteet.findOsapuoli(1, 'henkilo')({ id: 1, type: 'yritys' })
    );
  });

  it('returns false when only osapuoli-type matches', () => {
    assert.isFalse(
      Toimenpiteet.findOsapuoli(1, 'henkilo')({ id: 2, type: 'henkilo' })
    );
  });
});

describe('osapuoliSpecificDataIndexForOsapuoli', () => {
  it('finds correct index when there are multiple osapuolis', () => {
    const toimenpide = Toimenpiteet.emptyToimenpide(8, [], {
      osapuolis: [
        { id: 1, etunimi: 'Janice', sukunimi: 'Korvensuo' },
        { id: 3, nimi: 'AB Energiatodistuksettomat Oy' },
        { id: 7, etunimi: 'Karri', sukunimi: 'Poromäki' }
      ]
    });
    assert.equal(
      Toimenpiteet.osapuoliSpecificDataIndexForOsapuoli(
        toimenpide,
        1,
        'henkilo'
      ),
      0
    );

    assert.equal(
      Toimenpiteet.osapuoliSpecificDataIndexForOsapuoli(
        toimenpide,
        3,
        'yritys'
      ),
      1
    );

    assert.equal(
      Toimenpiteet.osapuoliSpecificDataIndexForOsapuoli(
        toimenpide,
        7,
        'henkilo'
      ),
      2
    );
  });
});

describe('osapuoliHasHallintoOikeus', () => {
  const toimenpide = {
    'type-specific-data': {
      'osapuoli-specific-data': [
        {
          osapuoli: {
            id: 1,
            type: 'henkilo'
          },
          'karajaoikeus-id': 1,
          'hallinto-oikeus-id': 1,
          'haastemies-email': 'juho.leinonen@solita.fi',
          document: true
        },
        {
          osapuoli: {
            id: 2,
            type: 'henkilo'
          },
          'karajaoikeus-id': 0,
          'haastemies-email': 'juho.leinonen@solita.fi',
          document: true
        },
        {
          osapuoli: {
            id: 3,
            type: 'yritys'
          },
          document: false
        },
        {
          osapuoli: {
            id: 1,
            type: 'yritys'
          },
          'hallinto-oikeus-id': 4,
          document: true
        }
      ]
    }
  };

  it('returns true when henkilö-osapuoli has hallinto-oikeus-id', () => {
    assert.isTrue(
      Toimenpiteet.osapuoliHasHallintoOikeus(toimenpide, {
        etunimi: 'Samuel',
        sukunimi: 'Laatikainen',
        id: 1
      })
    );
  });

  it('returns false when henkilö-osapuoli does not have hallinto-oikeus-id', () => {
    assert.isFalse(
      Toimenpiteet.osapuoliHasHallintoOikeus(toimenpide, {
        etunimi: 'Peter',
        sukunimi: 'Mannninen',
        id: 2
      })
    );
  });

  it('returns true when yritys-osapuoli has hallinto-oikeus-id', () => {
    assert.isTrue(
      Toimenpiteet.osapuoliHasHallintoOikeus(toimenpide, {
        nimi: 'Firma Oy',
        id: 1
      })
    );
  });

  it('returns false when yritys-osapuoli does not have hallinto-oikeus-id', () => {
    assert.isFalse(
      Toimenpiteet.osapuoliHasHallintoOikeus(toimenpide, {
        nimi: 'Yhtiö Ky',
        id: 3
      })
    );
  });
});

describe('Allowed toimenpiteet are filtered based on the type of the current toimenpide', () => {
  const toimenpidetyypit = R.range(0, 23).map(id => ({ id }));

  it('Valvonnan aloitus allows Kehotus and Valvonnan lopetus', () => {
    assert.deepEqual(
      Toimenpiteet.filterAvailableToimenpidetypes(
        'decision-order',
        0,
        toimenpidetyypit
      ),
      [{ id: 2 }, { id: 5 }]
    );
  });

  it('Kehotus allows Kehotus, Varoitus and Valvonnan lopetus', () => {
    assert.deepEqual(
      Toimenpiteet.filterAvailableToimenpidetypes(
        'decision-order',
        2,
        toimenpidetyypit
      ),
      [{ id: 2 }, { id: 3 }, { id: 5 }]
    );
  });

  it('Varoitus allows Kehotus, Varoitus, käskypäätös / kuulemiskirje  and Valvonnan lopetus', () => {
    assert.deepEqual(
      Toimenpiteet.filterAvailableToimenpidetypes(
        'decision-order',
        3,
        toimenpidetyypit
      ),
      [{ id: 2 }, { id: 3 }, { id: 5 }, { id: 7 }]
    );
  });

  it('Käskypäätös / kuulemiskirje allows Käskypäätös / kuulemiskirje, Käskypäätös / varsinainen päätös and Valvonnan lopetus', () => {
    assert.deepEqual(
      Toimenpiteet.filterAvailableToimenpidetypes(
        'decision-order',
        7,
        toimenpidetyypit
      ),
      [{ id: 5 }, { id: 7 }, { id: 8 }]
    );
  });

  it('Käskypäätös / varsinainen päätös allows Käskypäätös / varsinainen päätös, Käskypäätös / tiedoksianto (ensimmäinen postitus) and Valvonnan lopetus', () => {
    assert.deepEqual(
      Toimenpiteet.filterAvailableToimenpidetypes(
        'decision-order',
        8,
        toimenpidetyypit
      ),
      [{ id: 5 }, { id: 8 }, { id: 9 }]
    );
  });

  it('Käskypäätös / tiedoksianto (ensimmäinen postitus) allows Käskypäätös / tiedoksianto (toinen postitus), Käskypäätös / valitusajan odotus ja umpeutuminen and Valvonnan lopetus', () => {
    assert.deepEqual(
      Toimenpiteet.filterAvailableToimenpidetypes(
        'decision-order',
        9,
        toimenpidetyypit
      ),
      [{ id: 5 }, { id: 10 }, { id: 12 }]
    );
  });

  it('Käskypäätös / tiedoksianto (toinen postitus) allows Käskypäätös / tiedoksianto (Haastemies), Käskypäätös / valitusajan odotus ja umpeutuminen and Valvonnan lopetus', () => {
    assert.deepEqual(
      Toimenpiteet.filterAvailableToimenpidetypes(
        'decision-order',
        10,
        toimenpidetyypit
      ),
      [{ id: 5 }, { id: 11 }, { id: 12 }]
    );
  });

  it('Käskypäätös / tiedoksianto (Haastemies) allows Käskypäätös / valitusajan odotus ja umpeutuminen and Valvonnan lopetus', () => {
    assert.deepEqual(
      Toimenpiteet.filterAvailableToimenpidetypes(
        'decision-order',
        11,
        toimenpidetyypit
      ),
      [{ id: 5 }, { id: 12 }]
    );
  });

  it('Käskypäätös / valitusajan odotus ja umpeutuminen allows HaO-käsittely, Sakkopäätös / kuulemiskirje and Valvonnan lopetus', () => {
    assert.deepEqual(
      Toimenpiteet.filterAvailableToimenpidetypes(
        'decision-order',
        12,
        toimenpidetyypit
      ),
      [{ id: 5 }, { id: 6 }, { id: 14 }]
    );
  });

  it('HaO-käsittely allows Sakkopäätös / kuulemiskirje and Valvonnan lopetus when in käskypäätös phase of the process', () => {
    assert.deepEqual(
      Toimenpiteet.filterAvailableToimenpidetypes(
        'decision-order',
        6,
        toimenpidetyypit
      ),
      [{ id: 5 }, { id: 14 }]
    );
  });

  it('Sakkopäätös / kuulemiskirje allows Sakkopäätös / kuulemiskirje,Sakkopäätös / varsinainen päätös and Valvonnan lopetus', () => {
    assert.deepEqual(
      Toimenpiteet.filterAvailableToimenpidetypes(
        'penalty-decision',
        14,
        toimenpidetyypit
      ),
      [{ id: 5 }, { id: 14 }, { id: 15 }]
    );
  });

  it('Sakkopäätös / varsinainen päätös allows Sakkopäätös / varsinainen päätös, Sakkopäätös / tiedoksianto (ensimmäinen postitus) and Valvonnan lopetus', () => {
    assert.deepEqual(
      Toimenpiteet.filterAvailableToimenpidetypes(
        'penalty-decision',
        15,
        toimenpidetyypit
      ),
      [{ id: 5 }, { id: 15 }, { id: 16 }]
    );
  });

  it('Sakkopäätös / tiedoksianto (ensimmäinen postitus) allows Sakkopäätös / tiedoksianto (toinen postitus), Sakkopäätös / valitusajan odotus ja umpeutuminen and Valvonnan lopetus', () => {
    assert.deepEqual(
      Toimenpiteet.filterAvailableToimenpidetypes(
        'penalty-decision',
        16,
        toimenpidetyypit
      ),
      [{ id: 5 }, { id: 17 }, { id: 19 }]
    );
  });

  it('Sakkopäätös / tiedoksianto (toinen postitus) allows Sakkopäätös / tiedoksianto (Haastemies), Sakkopäätös / valitusajan odotus ja umpeutuminen and Valvonnan lopetus', () => {
    assert.deepEqual(
      Toimenpiteet.filterAvailableToimenpidetypes(
        'penalty-decision',
        17,
        toimenpidetyypit
      ),
      [{ id: 5 }, { id: 18 }, { id: 19 }]
    );
  });

  it('Sakkopäätös / tiedoksianto (Haastemies) allows Sakkopäätös / valitusajan odotus ja umpeutuminen and Valvonnan lopetus', () => {
    assert.deepEqual(
      Toimenpiteet.filterAvailableToimenpidetypes(
        'penalty-decision',
        18,
        toimenpidetyypit
      ),
      [{ id: 5 }, { id: 19 }]
    );
  });

  it('Sakkopäätös / valitusajan odotus ja umpeutuminen allows HaO-käsittely, Sakkoluettelon lähetys menossa and Valvonnan lopetus', () => {
    assert.deepEqual(
      Toimenpiteet.filterAvailableToimenpidetypes(
        'penalty-decision',
        19,
        toimenpidetyypit
      ),
      [{ id: 5 }, { id: 6 }, { id: 21 }]
    );
  });

  it('HaO-käsittely allows Sakkoluettelon lähetys menossa and Valvonnan lopetus when in sakkopäätös phase of the process', () => {
    assert.deepEqual(
      Toimenpiteet.filterAvailableToimenpidetypes(
        'penalty-decision',
        6,
        toimenpidetyypit
      ),
      [{ id: 5 }, { id: 21 }]
    );
  });

  it('Sakkoluettelon lähetys menossa allows Valvonnan lopetus', () => {
    assert.deepEqual(
      Toimenpiteet.filterAvailableToimenpidetypes(
        'penalty-decision',
        21,
        toimenpidetyypit
      ),
      [{ id: 5 }]
    );
  });

  it('Old käskypäätös allows Valvonnan lopetus', () => {
    assert.deepEqual(
      Toimenpiteet.filterAvailableToimenpidetypes(
        'decision-order',
        4,
        toimenpidetyypit
      ),
      [{ id: 5 }]
    );
  });
});

describe('determineProcessPhaseFromToimenpiteet', () => {
  it('returns decision-order when no toimenpide exists', () => {
    assert.equal(
      Toimenpiteet.determineProcessPhaseFromToimenpiteet([]),
      'decision-order'
    );
  });

  it('returns decision-order when a decision-order toimenpide exists', () => {
    assert.equal(
      Toimenpiteet.determineProcessPhaseFromToimenpiteet([
        Toimenpiteet.emptyToimenpide(7, [])
      ]),
      'decision-order'
    );
  });

  it('returns penalty-decision when a decision-order and penalty-decision toimenpide exists', () => {
    assert.equal(
      Toimenpiteet.determineProcessPhaseFromToimenpiteet([
        Toimenpiteet.emptyToimenpide(7, []),
        Toimenpiteet.emptyToimenpide(14, [])
      ]),
      'penalty-decision'
    );
  });
});
