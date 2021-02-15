export const redirect = url => (window.location.href = url);

export const redirectAfterTimeout = (url, timeout) =>
  setTimeout(() => redirect(url), timeout);
