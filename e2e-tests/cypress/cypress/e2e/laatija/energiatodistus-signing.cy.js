import { FIXTURES } from '../../fixtures/laatija';

describe('Signing energiatodistus', () => {
  beforeEach(() => {
    cy.intercept(/\/api\/private/, req => {
      req.headers = { ...req.headers, ...FIXTURES.headers };
    });
    cy.resetDb();
  });

  it('succeeds when using system signing', () => {
    cy.visit('/#/energiatodistus/2018/2');

    // Laskututiedot needs to be set.
    cy.selectInSelect('laskutusosoite-id', 'Henkilökohtaiset tiedot');

    cy.get('[data-cy="allekirjoita-button"]').click();

    cy.get('[data-cy="System"]').click();

    cy.intercept({
      method: 'POST',
      pathname:
        /\/api\/private\/energiatodistukset\/2018\/2\/signature\/system-sign/
    }).as('system-sign');

    cy.get('[data-cy="signing-submit-button"]').click();

    cy.wait('@system-sign');

    // After signing energiatodistus a link to the signed energiatodistus is visible
    // and the signed pdf can be downloaded.
    cy.get('[data-cy="energiatodistus-2-fi.pdf"]')
      .should('be.visible')
      .should('have.attr', 'href')
      .then(href => cy.request({ url: href, headers: FIXTURES.headers }))
      .then(res => {
        expect(res.status).to.eq(200);
      });
  });

  it('succeeds after switching energiatodistus draft to be bilingual and signing it using system signing', () => {
    cy.visit('/#/energiatodistus/2018/2');

    // Laskututiedot needs to be set.
    cy.selectInSelect('laskutusosoite-id', 'Henkilökohtaiset tiedot');

    // Set language to be bilingual
    cy.selectInSelect('perustiedot.kieli', 'Kaksikielinen');

    // Select edit language to Swedish
    cy.get('[data-cy="languageselect-sv"]').click();

    // Fill in missing Swedish description
    cy.get('[data-cy="lahtotiedot.lammitys.lammitysmuoto-2.kuvaus"]')
      .type('Testi')
      .blur();

    cy.get('[data-cy="allekirjoita-button"]').click();

    cy.get('[data-cy="System"]').click();

    cy.intercept({
      method: 'POST',
      pathname:
        /\/api\/private\/energiatodistukset\/2018\/2\/signature\/system-sign/
    }).as('system-sign');

    cy.get('[data-cy="signing-submit-button"]').click();

    cy.wait('@system-sign');

    // After signing energiatodistus links to the Finnish and Swedish energiatodistus document are present
    // and the signed PDFs can be downloaded.
    cy.get('[data-cy="energiatodistus-2-fi.pdf"]')
      .should('be.visible')
      .should('have.attr', 'href')
      .then(href => cy.request({ url: href, headers: FIXTURES.headers }))
      .then(res => {
        expect(res.status).to.eq(200);
      });

    cy.get('[data-cy="energiatodistus-2-sv.pdf"]')
      .should('be.visible')
      .should('have.attr', 'href')
      .then(href => cy.request({ url: href, headers: FIXTURES.headers }))
      .then(res => {
        expect(res.status).to.eq(200);
      });
  });
});
