import { assert } from 'chai';
import * as R from 'ramda';
import * as Maybe from '@Utility/maybe-utils';
import * as dfns from 'date-fns';
import * as Toimenpiteet from './toimenpiteet';

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

  it('Contains correct keys for toimenpidetype 7 which includes fine', () => {
    const emptyToimenpide = Toimenpiteet.emptyToimenpide(7, [{}]);
    assert.deepEqual(Object.keys(emptyToimenpide), [
      'type-id',
      'publish-time',
      'deadline-date',
      'template-id',
      'description',
      'fine'
    ]);
  });

  it('with a fine is recognized as having a fine', () => {
    const emptyToimenpide = Toimenpiteet.emptyToimenpide(7, [{}]);
    assert.isTrue(Toimenpiteet.hasFine(emptyToimenpide));
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
