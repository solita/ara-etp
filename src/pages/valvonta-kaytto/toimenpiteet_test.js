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

    it('is by default 37 days for type 8', () => {
      assert.isTrue(
        dfns.isSameDay(
          dfns.addDays(new Date(), 37),
          Maybe.get(Toimenpiteet.defaultDeadline(8))
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
      assert.isTrue(Toimenpiteet.isActualDecision({ 'type-id': 8 }));
      assert.isFalse(Toimenpiteet.isActualDecision({ 'type-id': 7 }));
    });
  });

  describe('Toimenpide object is recognized correctly whether it is part of the given types', () => {
    assert.isTrue(Toimenpiteet.isToimenpideOfGivenTypes([7])({ 'type-id': 7 }));
    assert.isFalse(
      Toimenpiteet.isToimenpideOfGivenTypes([7])({ 'type-id': 1 })
    );
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
      osapuoliIds: [1, 7]
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
      'recipient-answered',
      'answer-commentary-fi',
      'answer-commentary-sv',
      'statement-fi',
      'statement-sv',
      'courts',
      'department-head-title-fi',
      'department-head-title-sv',
      'department-head-name'
    ]);

    // courts contains a list of objects with osapuoli-id and associated hallinto-oikeus-id
    assert.deepEqual(
      R.path(['type-specific-data', 'courts'], emptyToimenpide),
      [
        { 'osapuoli-id': 1, 'hallinto-oikeus-id': Maybe.None() },
        {
          'osapuoli-id': 7,
          'hallinto-oikeus-id': Maybe.None()
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
      fine: Toimenpiteet.findFineFromToimenpiteet(toimenpiteet)
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

    assert.equal(Toimenpiteet.findFineFromToimenpiteet(toimenpiteet), 1000);
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

    assert.equal(Toimenpiteet.findFineFromToimenpiteet(toimenpiteet), 2000);
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

    assert.equal(Toimenpiteet.findFineFromToimenpiteet(toimenpiteet), 3000);
  });
});

describe('deleteOsaPuoliCourtdata takes toimenpide object', () => {
  it('and deletes the court data of the given osapuoli from it', () => {
    const toimenpide = Toimenpiteet.emptyToimenpide(8, [], {
      osapuoliIds: [1, 3, 7]
    });

    const toimenpideWithoutCourtDataForOsapuoli3 =
      Toimenpiteet.deleteOsapuoliCourtData(toimenpide, 3);

    assert.deepEqual(
      R.path(
        ['type-specific-data', 'courts'],
        toimenpideWithoutCourtDataForOsapuoli3
      ),
      [
        { 'osapuoli-id': 1, 'hallinto-oikeus-id': Maybe.None() },
        {
          'osapuoli-id': 7,
          'hallinto-oikeus-id': Maybe.None()
        }
      ]
    );
  });
});
