import Login from './Login';

export default { title: 'Login' };

export const withTwoSecondRedirectTimeout = () => ({
  Component: Login,
  props: {
    redirectTimeout: 2000
  }
});
