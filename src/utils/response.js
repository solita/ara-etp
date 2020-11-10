import * as Maybe from '@Utility/maybe-utils';

export const status = {
  ok: 200,
  unauthorized: 401,
  unavailable: 503,
  error: 500
}

export const isUnauthorized = response => response.status === status.unauthorized;

export const isUnavailable = response => response.status === status.unavailable;

export const isSystemError = response => response.status === status.error;


const localizationKeys = {
  401: 'errors.unauthorized',
  503: 'errors.unavailable',
};
export const localizationKey = response =>
  Maybe.fromNull(localizationKeys[response.status]);