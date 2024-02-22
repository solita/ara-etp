/**
 * Select value from Select component
 * @memberof cy
 * @method selectInSelect
 * @param {string} selectElement value of the data-cy attribute of the Select component you want to select
 * @param {string} optionToSelect The visible value in the Select component dropdown you want to select
 * @returns Chainable
 */
Cypress.Commands.add('selectInSelect', (selectElement, optionToSelect) => {
  cy.get('[data-cy="' + selectElement + '"]')
    .click()
    .parent()
    .within(() => {
      cy.contains(optionToSelect).click();
    });
});
