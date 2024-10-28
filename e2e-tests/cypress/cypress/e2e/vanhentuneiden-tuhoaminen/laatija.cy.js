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
    it('should see energiatodistus before it is expired', async () => {
      cy.visit('/#/energiatodistus/all');

      cy.get('[data-cy="energiatodistus-id"]').contains('1').should('exist');
      cy.get('[data-cy="energiatodistus-id"]')
        .contains('1')
        .siblings('[data-cy="energiatodistus-tila"]')
        .should('have.text', 'Voimassa');

      const query =
        "update etp.energiatodistus set voimassaolo_paattymisaika = now() - interval '2 days' where id = 1;";
      // This does not matter but it needs to be parseable by our database audit system.
      const applicationName = '-6@cypress';

      // Call the Cypress task to execute the query
      cy.task('executeQuery', { query, applicationName });
      cy.request(
        'POST',
        `${backendUrl}/api/internal/energiatodistukset/anonymize-and-delete-expired`
      ).then(response => {
        expect(response.status).to.eq(200);
      });

      //cy.get('[data-cy="energiatodistus-id"]').contains('1').should('exist');

      // This is kind of flaky as there is no way to know when the expiration is finished...

      cy.wait(3000);

      cy.reload();

      cy.get('[data-cy="energiatodistus-id"]').contains('1').should('exist');
      cy.get('[data-cy="energiatodistus-id"]')
        .contains('1')
        .siblings('[data-cy="energiatodistus-tila"]')
        .should('have.text', 'energiatodistus.tila.undefined');
      //TODO:
      //cy.get('[data-cy="energiatodistus-id"]').contains('1').siblings('[data-cy="energiatodistus-tila"]').should('not.exist');
    });
  });
});
