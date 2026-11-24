import { expect, describe, it } from '@jest/globals';
import { errorKey404 } from './attachment-error';

describe('AttachmentError', () => {
  const pageRoots = [
    'attachments',
    'energiatodistus.liitteet',
    'valvonta.kaytto.kohde',
    'viesti.ketju.liitteet',
    'valvonta.oikeellisuus.toimenpide'
  ];

  it.each(pageRoots)(
    'maps attachment errors from %s to shared messages',
    root => {
      [
        ['liite-invalid-filename', 'add-files-invalid-filename'],
        ['liite-forbidden-extension', 'add-files-forbidden-extension'],
        ['liite-executable', 'add-files-executable'],
        ['liite-content-type-mismatch', 'add-files-content-type-mismatch'],
        ['liite-extension-mismatch', 'add-files-extension-mismatch']
      ].forEach(([type, messageKey]) => {
        expect(
          errorKey404(root, 'add-files', { status: 400, body: { type } })
        ).toBe(`attachments.messages.${messageKey}`);
      });
    }
  );

  it('keeps 404 responses on the not-found message', () => {
    expect(
      errorKey404('viesti.ketju.liitteet', 'add-files', {
        status: 404,
        body: { type: 'liite-executable' }
      })
    ).toBe('viesti.ketju.liitteet.messages.not-found');
  });

  it('falls back to the action error for unknown responses', () => {
    expect(
      errorKey404('energiatodistus.liitteet', 'add-files', {
        status: 400,
        body: { type: 'unknown-error' }
      })
    ).toBe('energiatodistus.liitteet.messages.add-files-error');
  });
});
