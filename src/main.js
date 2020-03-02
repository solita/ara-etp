import 'promise-polyfill/src/polyfill';
import 'whatwg-fetch';

import App from './App';

const app = new App({
  target: document.body,
  props: {
    name: 'world'
  }
});

export default app;
