import { FIXTURES } from '../../fixtures/laatija';
import paakayttajaHeaders from '../../fixtures/users/paakayttaja.json';

const baseUrl = Cypress.config('baseUrl');
const backendUrl = Cypress.config('backendUrl');

const expiredEtWithValvontaId = 7;
const expiredEtWithoutValvontaId = 6;
const etToBeMadeExpiredId = 1;
const signedEtThatWillNotBeTouchedId = 4;

// Only thing about applicationName that matters in this case is that it is parseable by the audit system.
// Now just put as <expiration-system-user-id>@cypress.
const applicationName = '-6@cypress';

context('Laatija', () => {
  // Using just `before` here as we do not want to reset the database in
  // between tests.
  before(() => {
    cy.resetDb();
  });

  beforeEach(() => {
    cy.intercept(/\/api\/private/, req => {
      req.headers = { ...req.headers, ...FIXTURES.headers };
    });
  });

  describe('when destroying expired energiatodistukset', () => {
    it('should show energiatodistukset before the expiration is run', () => {
      cy.visit('/#/energiatodistus/all');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains(etToBeMadeExpiredId)
        .siblings('[data-cy="energiatodistus-tila"]')
        .should('have.text', 'Voimassa');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains(signedEtThatWillNotBeTouchedId)
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

    it('set the et 1 as expired and run expiration', () => {
      const query =
        "update etp.energiatodistus set voimassaolo_paattymisaika = now() - interval '2 days' where id = 1;";

      cy.task('executeQuery', { query, applicationName });
      // There is no way to know when running the expiration is finished but this seems to be fast enough.
      cy.request(
        'POST',
        `${backendUrl}/api/internal/energiatodistukset/anonymize-and-delete-expired`
      ).then(response => {
        expect(response.status).to.eq(200);
      });
    });

    it('energiatodistus that was not expired shows up still correctly', () => {
      cy.visit('/#/energiatodistus/all');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains(signedEtThatWillNotBeTouchedId)
        .siblings('[data-cy="energiatodistus-tila"]')
        .should('have.text', 'Voimassa');
    });

    it('energiatodistus that was made expired is not shown', () => {
      cy.visit('/#/energiatodistus/all');
      cy.get('[data-cy="energiatodistus-id"]')
        .contains(etToBeMadeExpiredId)
        .should('not.exist');
    });

    it('energiatodistus without valvonta is not shown', () => {
      cy.visit('/#/energiatodistus/all');
      cy.get('[data-cy="energiatodistus-id"]')
        .contains(expiredEtWithoutValvontaId)
        .should('not.exist');
    });

    it('energiatodistus with valvonta is still shown', () => {
      cy.visit('/#/energiatodistus/all');
      cy.get('[data-cy="energiatodistus-id"]')
        .contains(expiredEtWithValvontaId)
        .siblings('[data-cy="energiatodistus-tila"]')
        .should('have.text', 'Vanhentunut');
    });

    it('energiatodistus with valvonta can be still accessed', () => {
      cy.visit(`/#/energiatodistus/2018/${expiredEtWithValvontaId}`);
      cy.contains('Energiatodistus 2018/7 - Vanhentunut').should('exist');
    });

    it('energiatodistus without valvonta can not be accessed', () => {
      cy.visit(`/#/energiatodistus/2018/${expiredEtWithoutValvontaId}`);
      cy.contains('Puutteelliset käyttöoikeudet').should('exist');
    });
  });
});

context('Paakayttaja', () => {
  before(() => {
    cy.resetDb();
  });

  beforeEach(() => {
    cy.intercept(/\/api\/private/, req => {
      req.headers = { ...req.headers, ...paakayttajaHeaders };
    });
  });

  describe('when destroying expired energiatodistukset', () => {
    it('should see all the Voimassa energiatodistukset before running the expiration', () => {
      cy.visit('/#/energiatodistus/all');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains(etToBeMadeExpiredId)
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
    it('running the expiration should succeed', () => {
      const query =
        "update etp.energiatodistus set voimassaolo_paattymisaika = now() - interval '2 days' where id = 1;";
      cy.task('executeQuery', { query, applicationName });
      // There is no way to know when running the expiration is finished but this seems to be fast enough.
      cy.request(
        'POST',
        `${backendUrl}/api/internal/energiatodistukset/anonymize-and-delete-expired`
      ).then(response => {
        expect(response.status).to.eq(200);
      });
    });
    it('should see 1 and 6 as tuhottu, 4 as voimassa and 7 as vahentunut', () => {
      cy.visit('/#/energiatodistus/all');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains(etToBeMadeExpiredId)
        .siblings('[data-cy="energiatodistus-tila"]')
        .should('have.text', 'Tuhottu');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains(signedEtThatWillNotBeTouchedId)
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
    });
    it('energiatodistus without valvonta can not be accessed', () => {
      cy.visit('/#/energiatodistus/all');
      cy.get('[data-cy="energiatodistus-id"]')
        .contains(expiredEtWithoutValvontaId)
        .click();
      cy.contains('Puutteelliset käyttöoikeudet').should('exist');
    });

    it('energiatodistus with valvonta can still be accessed', () => {
      cy.visit('/#/energiatodistus/all');
      cy.get('[data-cy="energiatodistus-id"]')
        .contains(expiredEtWithValvontaId)
        .click();
      cy.contains('Energiatodistus 2018/7 - Vanhentunut').should('exist');
    });
  });
});
