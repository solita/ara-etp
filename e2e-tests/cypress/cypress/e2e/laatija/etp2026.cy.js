import { FIXTURES } from '../../fixtures/laatija';
import { isEtp2026Enabled } from '../../support/featureFlags';

const baseUrl = Cypress.config('baseUrl');

context('Laatija', () => {
    beforeEach(() => {
        cy.intercept(/\/api\/private/, req => {
            req.headers = { ...req.headers, ...FIXTURES.headers };
        });
        cy.resetDb();
    });
    describe('ETP 2026 feature', () => {
        it('should show ET26 creation button only when the feature is enabled', () => {
            cy.visit('/#/energiatodistus/all');

            if (isEtp2026Enabled())
                cy.get('[data-cy="create-new-et26"]').should('exist');
            else cy.get('[data-cy="create-new-et26"]').should('not.exist');
        });
    });
    describe(
        'Energiatodistus 2026 form',
        () => {
            it('The whole process', () => {
                cy.intercept({
                    method: 'PUT',
                    pathname: /\/api\/private\/energiatodistukset\/2026\/*/
                }).as('save');

                cy.visit('/#/energiatodistus/all');

                cy.get('[data-cy="create-new-et26"]').click();

                cy.get('[data-cy="save-button"]').click();

                cy.selectInSelect(
                    'perustiedot.laatimisvaihe',
                    'Olemassa oleva rakennus'
                );
                cy.selectInSelect(
                    'perustiedot.havainnointikayntityyppi-id',
                    'Virtuaalikäynti'
                );

                cy.get(
                    '[data-cy="tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko"]'
                )
                    .type('10000000000')
                    .blur();
                cy.get(
                    '[data-cy="validation-label-for=tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko"]'
                )
                    .should('be.visible')
                    .and('contain.text', 'Suurin sallittu arvo on 9999999999.');

                cy.get('[data-cy="save-button"]').click();
                cy.get('[data-cy="form-alert-text"]').should(
                    'contain.text',
                    'energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.label-context / energiatodistus.tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko'
                );

                cy.get(
                    '[data-cy="tulokset.uusiutuvat-omavaraisenergiat-kokonaistuotanto.aurinkosahko"]'
                )
                    .clear()
                    .type('10000')
                    .blur();

                cy.get('[data-cy="save-button"]').click();
                cy.wait('@save');
                cy.get('[data-cy="form-alert-text"]').should('not.exist');

                cy.get('[data-cy="lahtotiedot.rakennusvaippa.ulkoseinat.U"]')
                    .type('10')
                    .blur();
                cy.get(
                    '[data-cy="validation-label-for=lahtotiedot.rakennusvaippa.ulkoseinat.U"]'
                )
                    .should('be.visible')
                    .and('contain.text', 'Suurin sallittu arvo on 2.');
                cy.get('[data-cy="lahtotiedot.rakennusvaippa.ulkoseinat.U"]')
                    .clear()
                    .type('2')
                    .blur();
                cy.get(
                    '[data-cy="validation-label-for=lahtotiedot.rakennusvaippa.ulkoseinat.U"]'
                )
                    .should('be.visible')
                    .and('contain.text', 'Tyypillisesti pienempi kuin 0.81');

                cy.get('[data-cy="save-button"]').click();
                cy.wait('@save');
            });
        }
    );

    describe.only(
        'Copy energiatodistus with perusparannuspassi',
        () => {
            it('should copy perusparannuspassi data when copying energiatodistus', () => {
                cy.intercept({
                    method: 'POST',
                    pathname: /\/api\/private\/energiatodistukset\/2026$/
                }).as('post');

                cy.intercept({
                    method: 'PUT',
                    pathname: /\/api\/private\/energiatodistukset\/2026\/*/
                }).as('save');

                cy.intercept({
                    method: 'POST',
                    pathname: /\/api\/private\/perusparannuspassit\/2026$/
                }).as('postPpp');

                // Create a new ET 2026
                cy.visit('/#/energiatodistus/all');
                cy.get('[data-cy="create-new-et26"]').click();

                // Set required fields for basic save
                cy.selectInSelect(
                    'perustiedot.laatimisvaihe',
                    'Olemassa oleva rakennus'
                );
                cy.selectInSelect(
                    'perustiedot.havainnointikayntityyppi-id',
                    'Virtuaalikäynti'
                );

                // Save to create the energiatodistus
                cy.get('[data-cy="save-button"]').click();
                cy.wait(['@post']);

                // Wait for navigation to the created energiatodistus
                cy.location('hash').should('match', /\/energiatodistus\/2026\/\d+/);

                // Scroll to PPP section and add PPP
                cy.get('#perusparannuspassi').scrollIntoView();
                cy.get('[data-cy="add-ppp-button"]').click();

                // Fill in havainnointikäynnin päivämäärä
                const testDate = '15.06.2025';
                cy.get('[data-cy="passin-perustiedot.havainnointikaynti"]').type(
                    testDate
                );

                // Save the ET + PPP combo
                cy.get('[data-cy="save-button"]').click();
                cy.wait(['@save', '@postPpp']);

                // Copy the energiatodistus using the toolbar
                cy.get('[data-cy="copy-button"]')
                    .should('be.visible')
                    .and('not.be.disabled')
                    .click()

                // Should navigate to new ET with copy-from-id
                cy.location('hash').should(
                    'match',
                    /\/energiatodistus\/2026\/new\?copy-from-id=\d+/
                );

                // Verify PPP section exists and is valid (should be added automatically)
                cy.get('#perusparannuspassi').scrollIntoView();
                cy.get('[data-cy="delete-ppp-button"]').should('exist');

                // Verify the havainnointikäynnin päivämäärä is copied
                cy.get('[data-cy="passin-perustiedot.havainnointikaynti"]').should(
                    'have.value',
                    testDate
                );
            });
        }
    );
});
