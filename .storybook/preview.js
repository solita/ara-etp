import './tailwind.css';
import { makeDecorator } from '@storybook/addons';
import { addParameters, addDecorator } from '@storybook/svelte';
import { withA11y } from '@storybook/addon-a11y';
import Localized from './Localized.svelte'

addDecorator(withA11y);
addParameters({ viewport: { defaultViewport: 'responsive' } });

const localizationDecorator = makeDecorator({
  wrapper: (storyFn, context) => {
    const story = storyFn(context);
    story.Wrapper = Localized;
    return story;
  },
});

addDecorator(localizationDecorator);
