import { configure, addParameters, addDecorator } from '@storybook/svelte';
import { withA11y } from '@storybook/addon-a11y';
import './tailwind.css';

addDecorator(withA11y);
addParameters({ viewport: { defaultViewport: 'responsive' } });
