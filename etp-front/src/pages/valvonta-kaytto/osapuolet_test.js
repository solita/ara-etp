import { assert } from 'chai';
import {
  toimitustapa,
  toimitustapaErrorKey
} from '@Pages/valvonta-kaytto/osapuolet';
import { Maybe } from '@Utility/maybe-utils';

describe('Toimitustapa', () => {
  const suomifi = {
    'toimitustapa-id': Maybe.Some(0)
  };
  const email = {
    'toimitustapa-id': Maybe.Some(1)
  };
  const other = {
    'toimitustapa-id': Maybe.Some(2)
  };
  it('Check suomifi', () => {
    assert.isTrue(toimitustapa.suomifi(suomifi));
    assert.isFalse(toimitustapa.suomifi(email));
    assert.isFalse(toimitustapa.suomifi(other));
  });
  it('Check email', () => {
    assert.isFalse(toimitustapa.email(suomifi));
    assert.isTrue(toimitustapa.email(email));
    assert.isFalse(toimitustapa.email(other));
  });
  it('Check other', () => {
    assert.isFalse(toimitustapa.other(suomifi));
    assert.isFalse(toimitustapa.other(email));
    assert.isTrue(toimitustapa.other(other));
  });
});

describe('ToimitustapaErrorKey', () => {
  describe('Valid values return None', () => {
    it('always for muu', () => {
      assert.isTrue(
        toimitustapaErrorKey
          .yritys({ 'toimitustapa-id': Maybe.Some(2) })
          .isNone()
      );
      assert.isTrue(
        toimitustapaErrorKey
          .henkilo({ 'toimitustapa-id': Maybe.Some(2) })
          .isNone()
      );
    });
    describe('for yritys', () => {
      it('when using suomifi', () => {
        const suomifi = {
          'toimitustapa-id': Maybe.Some(0),
          ytunnus: Maybe.Some('1234567-8'),
          jakeluosoite: Maybe.Some('Katu 1'),
          maa: Maybe.Some('Suomi')
        };
        assert.isTrue(toimitustapaErrorKey.yritys(suomifi).isNone());
      });
      it('when using email', () => {
        const email = {
          'toimitustapa-id': Maybe.Some(1),
          email: Maybe.Some('example@example.com')
        };
        assert.isTrue(toimitustapaErrorKey.yritys(email).isNone());
      });
    });
    describe('for henkilo', () => {
      it('when using suomifi', () => {
        const suomifi = {
          'toimitustapa-id': Maybe.Some(0),
          henkilotunnus: Maybe.Some('123456-7890'),
          jakeluosoite: Maybe.Some('Katu 1'),
          maa: Maybe.Some('Suomi')
        };
        assert.isTrue(toimitustapaErrorKey.henkilo(suomifi).isNone());
      });
      it('when using email', () => {
        const email = {
          'toimitustapa-id': Maybe.Some(1),
          email: Maybe.Some('example@example.com')
        };
        assert.isTrue(toimitustapaErrorKey.henkilo(email).isNone());
      });
    });
  });
  describe('Invalid values return Some', () => {
    describe('for yritys', () => {
      it('when using suomifi', () => {
        const missingYTunnus = {
          'toimitustapa-id': Maybe.Some(0),
          ytunnus: Maybe.None(),
          jakeluosoite: Maybe.Some('Katu 1'),
          maa: Maybe.Some('Suomi')
        };
        const missingJakeluosoite = {
          'toimitustapa-id': Maybe.Some(0),
          ytunnus: Maybe.Some('1234567-8'),
          jakeluosoite: Maybe.None(),
          maa: Maybe.Some('Suomi')
        };
        const missingMaa = {
          'toimitustapa-id': Maybe.Some(0),
          ytunnus: Maybe.Some('1234567-8'),
          jakeluosoite: Maybe.Some('Katu 1'),
          maa: Maybe.None()
        };
        [missingYTunnus, missingJakeluosoite, missingMaa].forEach(invalid => {
          assert.equal(
            toimitustapaErrorKey.yritys(invalid).some(),
            'suomifi-yritys'
          );
        });
      });
      it('when using email', () => {
        const missingEmail = {
          'toimitustapa-id': Maybe.Some(1),
          email: Maybe.None()
        };
        assert.equal(toimitustapaErrorKey.yritys(missingEmail).some(), 'email');
      });
    });
    describe('for henkilo', () => {
      it('when using suomifi', () => {
        const missingHenkilotunnus = {
          'toimitustapa-id': Maybe.Some(0),
          henkilotunnus: Maybe.None(),
          jakeluosoite: Maybe.Some('Katu 1'),
          maa: Maybe.Some('Suomi')
        };
        const missingJakeluosoite = {
          'toimitustapa-id': Maybe.Some(0),
          henkilotunnus: Maybe.Some('123456-7890'),
          jakeluosoite: Maybe.None(),
          maa: Maybe.Some('Suomi')
        };
        const missingMaa = {
          'toimitustapa-id': Maybe.Some(0),
          henkilotunnus: Maybe.Some('123456-7890'),
          jakeluosoite: Maybe.Some('Katu 1'),
          maa: Maybe.None()
        };
        [missingHenkilotunnus, missingJakeluosoite, missingMaa].forEach(
          invalid => {
            assert.equal(
              toimitustapaErrorKey.henkilo(invalid).some(),
              'suomifi-henkilo'
            );
          }
        );
      });
      it('when using email', () => {
        const missingEmail = {
          'toimitustapa-id': Maybe.Some(1),
          email: Maybe.None()
        };
        assert.equal(
          toimitustapaErrorKey.henkilo(missingEmail).some(),
          'email'
        );
      });
    });
  });
});
