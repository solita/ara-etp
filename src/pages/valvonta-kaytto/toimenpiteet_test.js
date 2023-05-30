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
});
