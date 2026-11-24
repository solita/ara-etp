export const uploadAttachment = (
  uploadUrl,
  { fileName, contents, mimeType }
) => {
  cy.intercept('POST', uploadUrl).as('uploadAttachment');

  cy.get('[data-cy="droparea"] input[type="file"]').selectFile(
    {
      contents: contents || Cypress.Buffer.from('attachment test'),
      fileName,
      mimeType: mimeType || 'text/plain'
    },
    { force: true }
  );

  return cy.wait('@uploadAttachment');
};

const attachmentErrorMessages = {
  'liite-invalid-filename': 'Liitetiedoston nimi ei ole sallitussa muodossa.',
  'liite-forbidden-extension': 'Liitetiedoston tiedostopääte ei ole sallittu.',
  'liite-executable':
    'Liitetiedosto on suoritettava ohjelma eikä sitä voi lisätä liitteeksi.',
  'liite-content-type-mismatch':
    'Liitetiedoston sisältö ei vastaa ilmoitettua tiedostotyyppiä.',
  'liite-extension-mismatch':
    'Liitetiedoston tiedostopääte ei vastaa tiedoston sisältöä.'
};

export const assertRejectedAttachment = (response, expectedType) => {
  expect(response.statusCode).to.eq(400);
  expect(response.body.type).to.eq(expectedType);
  cy.get('[data-cy="form-alert-text"]').should(
    'contain',
    attachmentErrorMessages[expectedType]
  );
};

export const assertSuccessfulAttachment = (response, expectedMessage) => {
  expect(response.statusCode).to.eq(201);
  cy.get('[data-cy="form-alert-text"]').should('contain', expectedMessage);
};
