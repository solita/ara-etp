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
  });

  describe('Käskypäätös / Kuulemiskirje', () => {
    it('id is mapped correctly to the type key', () => {
      assert.equal('decision-order-hearing-letter', Toimenpiteet.typeKey(7));
    });

    it('is a type with a deadline', () => {
      assert.isTrue(Toimenpiteet.hasDeadline({ 'type-id': 7 }));
    });
  });

  describe('Toimenpidetypes with manually-deliverable are recognized correctly', () => {
    assert.isTrue(
      Toimenpiteet.isToimenpideDeliveredManually([7])({ 'type-id': 7 })
    );
    assert.isFalse(
      Toimenpiteet.isToimenpideDeliveredManually([7])({ 'type-id': 1 })
    );
  });
});

describe('Given toimenpidetypes, find the ids of manually deliverable types', () => {
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
