import { FIXTURES } from '../../fixtures/laatija';
import paakayttajaHeaders from '../../fixtures/users/paakayttaja.json';

const publicUrl = Cypress.config('publicUrl');
const backendUrl = Cypress.config('backendUrl');

const expiredEtWithValvontaId = 7;
const expiredEtWithoutValvontaId = 6;
const signedEtThatWillNotBeTouched1 = 1;
const signedEtThatWillNotBeTouchedId2 = 4;

const runExpirationOfTodistukset = () => {
  cy.request(
    'POST',
    `${backendUrl}/api/internal/energiatodistukset/anonymize-and-delete-expired`
  ).then(response => {
    expect(response.status).to.eq(200);
  });
  // There is no way to know when running the expiration is finished.
  cy.wait(1000);
};

context('Laatija', () => {
  beforeEach(() => {
    cy.resetDb();
    cy.intercept(/\/api\/private/, req => {
      req.headers = { ...req.headers, ...FIXTURES.headers };
    });
  });

  describe('when destroying expired energiatodistukset', () => {
    it('should show energiatodistukset before the expiration is run', () => {
      cy.visit('/#/energiatodistus/all');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains(signedEtThatWillNotBeTouched1)
        .siblings('[data-cy="energiatodistus-tila"]')
        .should('have.text', 'Voimassa');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains(signedEtThatWillNotBeTouchedId2)
        .siblings('[data-cy="energiatodistus-tila"]')
        .should('have.text', 'Voimassa');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains(expiredEtWithoutValvontaId)
        .siblings('[data-cy="energiatodistus-tila"]')
        .should('have.text', 'Voimassa');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains(expiredEtWithValvontaId)
        .siblings('[data-cy="energiatodistus-tila"]')
        .should('have.text', 'Voimassa');
    });

    it('Should only see non-expired and expired with ongoing valvonta energiatodistukset after running expiration', () => {
      cy.request(
        'POST',
        `${backendUrl}/api/internal/energiatodistukset/anonymize-and-delete-expired`
      ).then(response => {
        expect(response.status).to.eq(200);
      });

      // There is no way to know when running the expiration is finished.
      cy.wait(1000);

      cy.visit('/#/energiatodistus/all');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains(signedEtThatWillNotBeTouchedId2)
        .siblings('[data-cy="energiatodistus-tila"]')
        .should('have.text', 'Voimassa');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains(signedEtThatWillNotBeTouched1)
        .should('exist');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains(expiredEtWithoutValvontaId)
        .should('not.exist');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains(expiredEtWithValvontaId)
        .siblings('[data-cy="energiatodistus-tila"]')
        .should('have.text', 'Vanhentunut');

      cy.visit(`/#/energiatodistus/2018/${expiredEtWithValvontaId}`);
      cy.contains('Energiatodistus 2018/7 - Vanhentunut').should('exist');

      cy.visit(`/#/energiatodistus/2018/${expiredEtWithoutValvontaId}`);
      cy.contains('Puutteelliset käyttöoikeudet').should('exist');
    });
  });
});

context('Paakayttaja', () => {
  beforeEach(() => {
    cy.resetDb();
    cy.intercept(/\/api\/private/, req => {
      req.headers = { ...req.headers, ...paakayttajaHeaders };
    });
  });

  describe('when destroying expired energiatodistukset', () => {
    it('should see all the Voimassa energiatodistukset before running the expiration', () => {
      cy.visit('/#/energiatodistus/all');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains(signedEtThatWillNotBeTouched1)
        .siblings('[data-cy="energiatodistus-tila"]')
        .should('have.text', 'Voimassa');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains('4')
        .siblings('[data-cy="energiatodistus-tila"]')
        .should('have.text', 'Voimassa');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains(expiredEtWithoutValvontaId)
        .siblings('[data-cy="energiatodistus-tila"]')
        .should('have.text', 'Voimassa');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains(expiredEtWithValvontaId)
        .siblings('[data-cy="energiatodistus-tila"]')
        .should('have.text', 'Voimassa');
    });
    it('should see 1 and 6 as tuhottu, 4 as voimassa and 7 as vahentunut after running the expiration', () => {
      runExpirationOfTodistukset();
      cy.visit('/#/energiatodistus/all');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains(signedEtThatWillNotBeTouched1)
        .siblings('[data-cy="energiatodistus-tila"]')
        .should('have.text', 'Voimassa');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains(signedEtThatWillNotBeTouchedId2)
        .siblings('[data-cy="energiatodistus-tila"]')
        .should('have.text', 'Voimassa');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains(expiredEtWithoutValvontaId)
        .siblings('[data-cy="energiatodistus-tila"]')
        .should('have.text', 'Tuhottu');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains(expiredEtWithValvontaId)
        .siblings('[data-cy="energiatodistus-tila"]')
        .should('have.text', 'Vanhentunut');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains(expiredEtWithoutValvontaId)
        .click();
      cy.contains('Puutteelliset käyttöoikeudet').should('exist');

      cy.visit('/#/energiatodistus/all');
      cy.get('[data-cy="energiatodistus-id"]')
        .contains(expiredEtWithValvontaId)
        .click();
      cy.contains('Energiatodistus 2018/7 - Vanhentunut').should('exist');
    });
  });
});

context('Public', () => {
  beforeEach(() => {
    cy.resetDb();
  });

  describe('when destroying expired energiatodistukset', () => {
    it('should see all the Voimassa energiatodistukset before running the expiration', () => {
      cy.visit(`${publicUrl}/ethaku`);

      cy.get('[data-cy="ethaku-hae"]').click();

      // Notice that these are the type that are seen by the public
      cy.get('[data-cy="ethaku-tunnus"]')
        .contains(expiredEtWithValvontaId)
        .should('exist');

      cy.get('[data-cy="ethaku-tunnus"]')
        .contains(expiredEtWithoutValvontaId)
        .should('exist');

      cy.get('[data-cy="ethaku-tunnus"]')
        .contains(signedEtThatWillNotBeTouchedId2)
        .should('exist');

      cy.get('[data-cy="ethaku-tunnus"]')
        .contains(signedEtThatWillNotBeTouched1)
        .should('exist');
    });
    it('should only see the non-expired todistukset after running the expiration.', () => {
      runExpirationOfTodistukset();
      cy.visit(`${publicUrl}/ethaku`);

      cy.get('[data-cy="ethaku-hae"]').click();

      // Notice that these are the type that are seen by the public
      cy.get('[data-cy="ethaku-tunnus"]')
        .contains(expiredEtWithValvontaId)
        .should('not.exist');

      cy.get('[data-cy="ethaku-tunnus"]')
        .contains(expiredEtWithoutValvontaId)
        .should('not.exist');

      cy.get('[data-cy="ethaku-tunnus"]')
        .contains(signedEtThatWillNotBeTouchedId2)
        .should('exist');

      cy.get('[data-cy="ethaku-tunnus"]')
        .contains(signedEtThatWillNotBeTouched1)
        .should('exist');

      cy.visit(
        `${publicUrl}/energiatodistus?id=${expiredEtWithValvontaId}&versio=2018`
      );
      cy.contains('Energiatodistusta ei löytynyt').should('exist');

      cy.visit(
        `${publicUrl}/energiatodistus?id=${expiredEtWithoutValvontaId}&versio=2018`
      );
      cy.contains('Energiatodistusta ei löytynyt').should('exist');

      cy.visit(
        `${publicUrl}/energiatodistus?id=${signedEtThatWillNotBeTouched1}&versio=2013`
      );
      // This is a piece of the todistus' notes.
      cy.contains('Seuraavia toimenpiteitä voisi tehdä:').should('exist');
    });
  });
});
