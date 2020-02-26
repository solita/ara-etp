// TODO this should also come from config.json similar to client id.
export const loginPageBaseUrl = "https://kehitys-energiatodistuspalvelu-com.auth.eu-central-1.amazoncognito.com/login";

export const redirect = url => window.location.href = url;

export const redirectAfterTimeout = (url, timeout) => setTimeout(() =>
  redirect(url),
  timeout
);

// TODO should include state query parameter to cognito and verify it upon returning.
export const loginPageUrl = () => `${loginPageBaseUrl}?client_id=${CONFIG.COGNITOCLIENTID}&redirect_uri=${encodeURIComponent(document.location.href)}&response_type=code`;
