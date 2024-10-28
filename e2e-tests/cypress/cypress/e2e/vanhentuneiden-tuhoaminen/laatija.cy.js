import { FIXTURES } from '../../fixtures/laatija';

const baseUrl = Cypress.config('baseUrl');
const backendUrl = Cypress.config('backendUrl');

context('Laatija', () => {
  beforeEach(() => {
    cy.intercept(/\/api\/private/, req => {
      req.headers = { ...req.headers, ...FIXTURES.headers };
    });
    cy.resetDb();
  });

  describe('energiatodistukset', () => {
    it('should see energiatodistus before it is expired', () => {
      cy.visit('/#/energiatodistus/all');

      cy.get('[data-cy="energiatodistus-id"]')
        .contains('1')
        .siblings('[data-cy="energiatodistus-tila"]')
        .should('have.text', 'Voimassa');
      cy.get('[data-cy="energiatodistus-id"]')
        .contains('6')
        .siblings('[data-cy="energiatodistus-tila"]')
        .should('have.text', 'Voimassa');
      cy.get('[data-cy="energiatodistus-id"]')
        .contains('7')
        .siblings('[data-cy="energiatodistus-tila"]')
        .should('have.text', 'Voimassa');

      const query =
        "update etp.energiatodistus set voimassaolo_paattymisaika = now() - interval '2 days' where id = 1;";
      // This does not matter, but it needs to be parseable by our database audit system.
      const applicationName = '-6@cypress';

      // Call the Cypress task to execute the query
      cy.task('executeQuery', { query, applicationName });
      cy.request(
        'POST',
        `${backendUrl}/api/internal/energiatodistukset/anonymize-and-delete-expired`
      ).then(response => {
        expect(response.status).to.eq(200);
      });

      // There is no way to know when running the expiration is finished but this seems to be fast enough.
      cy.reload();

      cy.get('[data-cy="energiatodistus-id"]')
        .contains('1')
        .should('not.exist');
      cy.get('[data-cy="energiatodistus-id"]')
        .contains('6')
        .should('not.exist');
      cy.get('[data-cy="energiatodistus-id"]')
        .contains('7')
        .siblings('[data-cy="energiatodistus-tila"]')
        .should('have.text', 'Vanhentunut');
    });
  });
});
