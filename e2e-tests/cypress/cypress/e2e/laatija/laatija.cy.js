import { FIXTURES } from '../../fixtures/laatija';

const baseUrl = Cypress.config('baseUrl');

context('Laatija', () => {
  beforeEach(() => {
    cy.intercept(/\/api\/private/, req => {
      req.headers = { ...req.headers, ...FIXTURES.headers };
    });
    cy.resetDb();
  });

  it('should redirect to energiatodistus', () => {
    cy.visit('/');

    cy.location().should(loc =>
      assert.equal(loc.toString(), `${baseUrl}/#/energiatodistus/all`)
    );
  });

  describe('energiatodistukset', () => {
    it('should navigate to energiatodistus', () => {
      cy.visit('/#/energiatodistus/all');

      cy.get('[data-cy="energiatodistus-id"]').contains('5').click();

      cy.location().should(loc =>
        assert.equal(loc.toString(), `${baseUrl}/#/energiatodistus/2018/5`)
      );
    });

    it.skip('should change laskutettava yritys', () => {
      cy.intercept(
        /\/api\/private\/laatijat\/2\/yritykset$/,
        FIXTURES.laatija.yritykset
      ).as('yritykset');

      cy.intercept(/\/api\/private\/yritykset\/1$/, FIXTURES.yritys[1]).as(
        'yritys1'
      );

      cy.visit('/#/energiatodistus/2018/5');

      cy.wait('@yritykset');
      cy.wait('@yritys1');

      cy.get('[data-cy="laskutettava-yritys-id"]').click();
      cy.contains('nimi').click();
      cy.get('[data-cy="energiatodistus.laskutus.postiosoite"]').should(
        'have.value',
        'jakeluosoite, 12345 postitoimipaikka'
      );
    });

    it('should delete energiatodistus from energiatodistus page', () => {
      cy.visit('/#/energiatodistus/all');

      cy.get('[data-cy="energiatodistus-id"]').contains('5').click();

      cy.contains('Poista').click();

      cy.get('[data-cy="confirm-submit-button"]').click();

      cy.get('[data-cy="energiatodistus-id"]')
        .contains('5')
        .should('not.exist');
    });

    it('should delete energiatodistus from energiatodistukset page', () => {
      cy.visit('/#/energiatodistus/all');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains('5')
        .siblings('[data-cy="energiatodistus-delete"]')
        .click();

      cy.get('[data-cy="confirm-submit-button"]').click();

      cy.get('[data-cy="energiatodistus-id"]')
        .contains('5')
        .should('not.exist');
    });
  });

  describe('yritykset', () => {
    it('should navigate to yritykset', () => {
      cy.visit('/#/energiatodistus/all');

      cy.contains('Yritykset').click();

      cy.location().should(loc =>
        assert.equal(loc.toString(), `${baseUrl}/#/laatija/2/yritykset`)
      );
    });

    // Gives 'Puuttelliset käyttöoikeudet'
    // This works if following steps are performed manually before running this test:
    // - Be Liisa Specimen-Potex
    // - Create some yritys
    // Does not work if the manually created yritys is deleted.
    it.skip('should create new yritys', () => {
      const yritys = FIXTURES.yritys[1];

      cy.visit('/#/laatija/2/yritykset');
      cy.contains('Lisää uusi yritys').click();

      cy.location().should(loc =>
        assert.equal(loc.toString(), `${baseUrl}/#/yritys/new`)
      );

      cy.get('[data-cy="ytunnus"]').type(yritys.ytunnus);
      cy.get('[data-cy="nimi"]').type(yritys.nimi);
      cy.get('[data-cy="jakeluosoite"]').type(yritys.jakeluosoite);
      cy.get('[data-cy="postinumero"]').type(yritys.postinumero);
      cy.get('[data-cy="postitoimipaikka"]').type(yritys.postitoimipaikka);
      cy.get('[data-cy="maa"]').type('{selectall}{backspace}suom{enter}');

      cy.intercept(
        {
          method: 'POST',
          pathname: /\/api\/private\/yritykset$/
        },
        {
          statusCode: 201,
          body: { id: 1 }
        }
      ).as('postYritys');

      cy.intercept(
        {
          method: 'GET',
          pathname: /\/api\/private\/yritykset\/1$/
        },
        yritys
      ).as('yritys');

      cy.contains('Tallenna').click();

      cy.wait('@postYritys');
      cy.wait('@yritys');

      cy.get('[data-cy="ytunnus"]').should('have.value', '1111112-8');
      cy.get('#breadcrumbcontainer a').last().should('contain.text', 'nimi');
      cy.get('#navigationcontainer a').first().should('contain.text', 'nimi');
    });

    it('should join yritys', () => {
      function* laatijaYrityksetResponseGenerator() {
        yield FIXTURES.laatija.yritykset;
        yield [
          ...FIXTURES.laatija.yritykset,
          {
            id: 2,
            'modifiedby-name': 'Toinen, Laatija',
            modifytime: '2021-03-11T10:32:36.778400Z',
            'tila-id': 0
          }
        ];
      }

      const laatijaYrityksetResponse = laatijaYrityksetResponseGenerator();

      cy.intercept(
        {
          method: 'GET',
          pathname: /\/api\/private\/yritykset$/
        },
        FIXTURES.yritykset
      ).as('yritykset');
      cy.intercept(
        {
          method: 'GET',
          pathname: /\api\/private\/laatijat\/2\/yritykset$/
        },
        req => req.reply(laatijaYrityksetResponse.next().value)
      ).as('laatijanYritykset');

      cy.visit('/#/laatija/2/yritykset');

      cy.wait('@laatijanYritykset');
      cy.wait('@yritykset');

      cy.intercept(
        {
          method: 'PUT',
          pathname: /\/api\/private\/laatijat\/2\/yritykset\/2$/
        },
        { statusCode: 200 }
      ).as('putLaatija');

      cy.get('[data-cy="yritys"]').type('yri{enter}');

      cy.contains('Liitä yritykseen').click();

      cy.wait('@putLaatija');
      cy.wait('@laatijanYritykset');

      cy.contains('Odottaa hyväksymistä').should('exist');
      cy.get('[data-cy="yritys-row"]').should('have.length', 2);
    });

    it('should leave yritys', () => {
      function* laatijaYrityksetResponseGenerator() {
        yield [
          ...FIXTURES.laatija.yritykset,
          {
            id: 2,
            'modifiedby-name': 'Toinen, Laatija',
            modifytime: '2021-03-11T10:32:36.778400Z',
            'tila-id': 0
          }
        ];
        yield FIXTURES.laatija.yritykset;
      }

      const laatijaYrityksetResponse = laatijaYrityksetResponseGenerator();

      cy.intercept(
        {
          method: 'GET',
          pathname: /\/api\/private\/yritykset$/
        },
        FIXTURES.yritykset
      ).as('yritykset');
      cy.intercept(
        {
          method: 'GET',
          pathname: /\api\/private\/laatijat\/2\/yritykset$/
        },
        req => req.reply(laatijaYrityksetResponse.next().value)
      ).as('laatijanYritykset');

      cy.visit('/#/laatija/2/yritykset');

      cy.wait('@yritykset');
      cy.wait('@laatijanYritykset');

      cy.intercept(
        {
          method: 'DELETE',
          pathname: /\/api\/private\/laatijat\/2\/yritykset\/2$/
        },
        { statusCode: 200 }
      ).as('deleteLaatija');

      cy.get('[data-cy="yritys-row"] span').last().click();

      cy.get('[data-cy="confirm-submit-button"]').click();

      cy.wait('@deleteLaatija');
      cy.wait('@yritykset');
      cy.wait('@laatijanYritykset');

      cy.get('[data-cy="yritys-row"]').should('have.length', 1);
    });
  });

  describe('viestiketjut', () => {
    function* viestiResponseGenerator() {
      yield FIXTURES.viesti;
      yield FIXTURES.respondedViesti;
    }

    it('should navigate to viestit', () => {
      cy.visit('/#/energiatodistus/all');

      cy.contains('Viestit').click();

      cy.location().should(loc =>
        assert.equal(loc.toString(), `${baseUrl}/#/viesti/all`)
      );
    });

    it.skip('should make new ketju', () => {
      const viestiResponse = viestiResponseGenerator();
      cy.visit('/#/viesti/all');
      cy.contains('Uusi viesti').click();
      cy.location().should(loc =>
        assert.equal(loc.toString(), `${baseUrl}/#/viesti/new`)
      );

      cy.intercept(
        {
          method: 'POST',
          pathname: /\/api\/private\/viestit$/
        },
        {
          statusCode: 201
        }
      ).as('post');

      cy.intercept(
        {
          method: 'GET',
          pathname: /\/api\/private\/viestit\/count$/
        },
        {
          statusCode: 200,
          body: { count: 1 }
        }
      ).as('count');

      cy.intercept(
        {
          method: 'GET',
          pathname: /\/api\/private\/viestit$/
        },
        {
          statusCode: 200,
          body: [
            {
              id: 5000,
              vastaanottajat: [],
              'vastaanottajaryhma-id': 0,
              'energiatodistus-id': null,
              subject: 'Otsikko',
              viestit: [
                {
                  from: {
                    id: 2,
                    'rooli-id': 0,
                    sukunimi: 'Specimen-Potex',
                    etunimi: 'Liisa'
                  },
                  'sent-time': '2021-03-18T08:25:16.735110Z',
                  body: 'Viesti'
                }
              ]
            }
          ]
        }
      ).as('viestit');

      cy.get('[data-cy="ketju.subject"]').type('Otsikk');
      cy.get('.ql-editor').type('Viesti');
      // Typing the last 'o' so that the focus from '.ql-editor' has been moved to achieve 'Lähetä' working.
      cy.get('[data-cy="ketju.subject"]').type('o');
      cy.contains('Lähetä').click();
      cy.wait('@post');
      cy.wait('@count');
      cy.wait('@viestit');

      cy.location().should(loc =>
        assert.equal(loc.toString(), `${baseUrl}/#/viesti/all`)
      );

      cy.intercept(
        {
          method: 'GET',
          pathname: /\/api\/private\/viestit\/5000$/
        },
        req => req.reply(viestiResponse.next().value)
      ).as('viesti');

      cy.contains('Otsikko').click();

      cy.location().should(loc =>
        assert.equal(loc.toString(), `${baseUrl}/#/viesti/5000`)
      );

      cy.wait('@viesti');

      cy.get('[data-cy="message"]').should(msg => assert.equal(msg.length, 1));
    });

    it.skip('should respond to viestiketju', () => {
      const viestiResponse = viestiResponseGenerator();
      cy.intercept(
        {
          method: 'GET',
          pathname: /\/api\/private\/viestit\/5000$/
        },
        req => req.reply(viestiResponse.next().value)
      ).as('viesti');

      cy.visit('/#/viesti/5000');

      cy.wait('@viesti');

      cy.get('[data-cy="uusiviesti-button"]').type('Vastaus').blur();
      cy.intercept(
        {
          method: 'POST',
          pathname: /\/api\/private\/viestit\/5000\/viestit$/
        },
        {
          statusCode: 200
        }
      ).as('vastaus');

      cy.get('[data-cy="viesti-submit"]').click();

      cy.wait('@vastaus');
      cy.wait('@viesti');

      cy.get('[data-cy="message"]').should(msg => assert.equal(msg.length, 2));
    });
  });

  describe('Omat tiedot', () => {
    it('should navigate to omat tiedot', () => {
      cy.visit('/');
      cy.contains('Liisa Specimen-Potex').click();
      cy.contains('Omat tiedot').click();

      cy.location().should(loc =>
        assert.equal(loc.toString(), `${baseUrl}/#/kayttaja/2`)
      );
      cy.get('#breadcrumbcontainer span')
        .last()
        .should('contain.text', 'Omat tiedot');
      cy.get('#navigationcontainer a')
        .first()
        .should('contain.text', 'Liisa Specimen-Potex');
    });

    it('should disable checkbox with päätoiminta-alue selection', () => {
      cy.visit('/#/kayttaja/2');
      cy.contains('Ei valintaa').click();
      cy.contains('Etelä-Karjala').click();
      cy.get('#muuttoimintaalueet ol li input').first().should('be.disabled');
    });

    it('should disable all with many toimintaalueet selected', () => {
      cy.visit('/#/kayttaja/2');
      cy.contains('Ei valintaa').click();
      cy.contains('Etelä-Karjala').click();
      cy.get('#muuttoimintaalueet').as('root');

      const toimintaalueet = [
        'Kainuu',
        'Keski-Suomi',
        'Pirkanmaa',
        'Pohjois-Pohjanmaa',
        'Satakunta'
      ];

      toimintaalueet.forEach(item => cy.get('@root').contains(item).click());

      cy.get('#muuttoimintaalueet [type="checkbox"]:disabled').should(
        'have.length',
        13
      );
    });

    it.skip('should undisable julkinen www when valid url is given', () => {
      cy.visit('/#/kayttaja/2');

      cy.get('[type="checkbox"]:disabled').as('input');

      cy.get('@input').should('be.disabled');

      cy.get('[data-cy="wwwosoite"]').type('example.com').blur();

      cy.get('@input').should('not.be.disabled');
    });
  });
});
