import * as Router from 'svelte-spa-router';

import { flashMessageStore } from '@/stores';

export const replace = Router.replace;

export const pop = Router.pop;

export const push = Router.push;

export const replaceFlushFlashMessages = route => {
  flashMessageStore.flush();
  Router.replace(route);
};

export const popFlushFlashMessages = route => {
  flashMessageStore.flush();
  Router.pop(route);
};

export const pushFlushFlashMessages = route => {
  flashMessageStore.flush();
  Router.push(route);
};
