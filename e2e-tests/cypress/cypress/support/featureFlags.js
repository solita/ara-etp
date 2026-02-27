export function isEtp2026Enabled() {
  return Cypress.env('ETP_2026') === true;
}
