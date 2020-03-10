import Login from './Login';

export default { title: 'Login' };

export const withFiveSecondRedirectTimeout = () => ({
  Component: Login,
  props: {
    redirectTimeout: 5000
  }
});
