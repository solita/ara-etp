import { FIXTURES } from '../../fixtures/laatija';

describe('Signing energiatodistus', () => {
  beforeEach(() => {
    cy.intercept(/\/api\/private/, req => {
      req.headers = { ...req.headers, ...FIXTURES.headers };
    });
    cy.resetDb();
  });

  it('succeeds when signing using mpollux', () => {
    cy.intercept(
      'https://localhost:53952/version',
      FIXTURES.mpollux.version
    ).as('mpolluxVersion');
    cy.intercept(
      {
        method: 'PUT',
        pathname: /\/api\/private\/energiatodistukset\/2018\/5$/
      },
      { statusCode: 200 }
    ).as('signStart');
    cy.visit('/#/energiatodistus/2018/5');
    cy.get('[data-cy="laskutusosoite-id"]').click();
    cy.contains('Henkilökohtaiset tiedot').click();
    cy.get('[data-cy="allekirjoita-button"]').click();

    cy.wait('@signStart');
    cy.wait('@mpolluxVersion');

    cy.intercept(
      {
        method: 'POST',
        pathname:
          /\/api\/private\/energiatodistukset\/2018\/5\/signature\/start/
      },
      { statusCode: 200, body: 'Ok' }
    ).as('start');

    cy.intercept(
      {
        method: 'GET',
        pathname:
          /\/api\/private\/energiatodistukset\/2018\/5\/signature\/digest\/fi/
      },
      {
        digest: FIXTURES.mpollux.digest
      }
    ).as('digest');

    cy.intercept(
      'POST',
      'https://localhost:53952/sign',
      FIXTURES.mpollux.sign
    ).as('sign');

    cy.intercept(
      {
        method: 'PUT',
        pathname:
          /\/api\/private\/energiatodistukset\/2018\/5\/signature\/pdf\/fi/
      },
      { statusCode: 200, body: FIXTURES.mpollux.pdf(5) }
    ).as('pdf');

    cy.intercept(
      {
        method: 'POST',
        pathname:
          /\/api\/private\/energiatodistukset\/2018\/5\/signature\/finish/
      },
      { statusCode: 200, body: 'Ok' }
    ).as('finish');

    // Signing method selection should exist before starting the signing.
    cy.get('[name="Card"]').should('exist');
    cy.get('[name="System"]').should('exist');

    cy.get('[data-cy="signing-submit-button"]').click();

    // Signing method selection should not exist when the signing is in progress.
    cy.get('[name="Card"]').should('not.exist');
    cy.get('[name="System"]').should('not.exist');

    cy.wait('@start');
    cy.wait('@digest');
    cy.wait('@sign');
    cy.wait('@pdf');
    cy.wait('@finish');

    cy.contains(
      'Suomenkielinen energiatodistus on allekirjoitettu onnistuneesti.'
    ).should('exist');

    // Signing method selection should not exist after the signing has finished.
    cy.get('[name="Card"]').should('not.exist');
    cy.get('[name="System"]').should('not.exist');
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

  it.skip('can be aborted and starting signing afterwards again succeeds when using system signing', () => {
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

    cy.intercept({
      method: 'POST',
      pathname: /\/api\/private\/energiatodistukset\/2018\/2\/signature\/cancel/
    }).as('sign-cancel');

    cy.get('[data-cy="signing-submit-button"]').click();

    // Abort the signing process after one second
    cy.wait(1000);
    cy.get('[data-cy="signing-reject-button"]').click();
    cy.wait('@sign-cancel');

    // Start signing again
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
});
