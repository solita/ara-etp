import * as R from 'ramda';
import * as Response from './response';

const attachmentErrorKeys = {
  'liite-invalid-filename': 'add-files-invalid-filename',
  'liite-forbidden-extension': 'add-files-forbidden-extension',
  'liite-executable': 'add-files-executable',
  'liite-content-type-mismatch': 'add-files-content-type-mismatch',
  'liite-extension-mismatch': 'add-files-extension-mismatch'
};
const attachmentI18nRoot = 'attachments';

export const attachmentErrorKey = response =>
  attachmentErrorKeys[R.path(['body', 'type'], response)];

export const errorKey404 = (i18nRoot, action, response) =>
  R.cond([
    [
      response => Response.notFound(response),
      R.always(`${i18nRoot}.messages.not-found`)
    ],
    [
      R.pipe(attachmentErrorKey, Boolean),
      response =>
        `${attachmentI18nRoot}.messages.${attachmentErrorKey(response)}`
    ],
    [R.T, response => Response.errorKey404(i18nRoot, action, response)]
  ])(response);
