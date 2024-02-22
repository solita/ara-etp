Cypress.Commands.add('selectInSelect', (selectElement, optionToSelect) => {
  cy.get('[data-cy="' + selectElement + '"]')
    .click()
    .parent()
    .within(() => {
      cy.contains(optionToSelect).click();
    });
});
