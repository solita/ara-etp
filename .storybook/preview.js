import './tailwind.css';
import { makeDecorator } from '@storybook/addons';
import { addParameters, addDecorator } from '@storybook/svelte';
import { withA11y } from '@storybook/addon-a11y';
import { setupI18n } from '@Language/i18n';

addDecorator(withA11y);
addParameters({ viewport: { defaultViewport: 'responsive' } });

const localizationDecorator = makeDecorator({
  wrapper: (storyFn, context) => {
    setupI18n();
    return storyFn(context);
  }
});

addDecorator(localizationDecorator);
