import { configure, addParameters, addDecorator } from '@storybook/svelte';
import { withA11y } from '@storybook/addon-a11y';

import './tailwind.css';

configure(require.context('../src', true, /\.stories\.js$/), module);

addDecorator(withA11y);
addParameters({ viewport: { viewports: newViewports, defaultViewport: 'responsive' } });
