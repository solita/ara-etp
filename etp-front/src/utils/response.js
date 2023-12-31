import * as Maybe from '@Utility/maybe-utils';

export const status = {
  ok: 200,
  notFound: 404,
  unauthorized: 401,
  unavailable: 503,
  error: 500
};

export const isUnauthorized = response =>
  response.status === status.unauthorized;

export const isUnavailable = response => response.status === status.unavailable;

export const isSystemError = response => response.status === status.error;

export const notFound = response => response.status === status.notFound;

const localizationKeys = {
  401: 'errors.unauthorized',
  403: 'errors.forbidden',
  503: 'errors.unavailable'
};
export const localizationKey = response =>
  Maybe.fromNull(localizationKeys[response.status]);

export const errorKey = (i18nRoot, action, response) =>
  Maybe.orSome(
    `${i18nRoot}.messages.${action}-error`,
    localizationKey(response)
  );

export const errorKey404 = (i18nRoot, action, response) =>
  notFound(response)
    ? `${i18nRoot}.messages.not-found`
    : errorKey(i18nRoot, action, response);

export const openBlob = blob => {
  const pdfUrl = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = pdfUrl;
  link.target = '_blank';
  document.body.appendChild(link);
  link.click();

  setTimeout(() => {
    window.URL.revokeObjectURL(pdfUrl);
    document.body.removeChild(link);
  }, 1000);
};
