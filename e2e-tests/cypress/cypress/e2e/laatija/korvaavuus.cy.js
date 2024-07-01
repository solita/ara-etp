import { FIXTURES } from '../../fixtures/laatija';

describe('Handling of korvaavuusehdotus', () => {
  beforeEach(() => {
    cy.intercept(/\/api\/private/, req => {
      req.headers = { ...req.headers, ...FIXTURES.headers };
    });
    cy.resetDb();
  });

  // A quick check for the regression reported as AE-2257
  it('Check that selecting an ET to be replaced causes the replacement checkbox to be checked', () => {
    cy.visit('/#/energiatodistus/2018/5');
    cy.get('[data-cy="korvaavuus-checkbox"]').should('not.be.checked');
    cy.get('[data-cy="energiatodistus-1-id-cell"]').click();
    cy.get('[data-cy="korvaavuus-checkbox"]').should('be.checked');
  });
});
