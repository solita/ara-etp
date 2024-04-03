import './tailwind.css';
import { makeDecorator } from "@storybook/preview-api";
import { setupI18n } from '@Language/i18n';

export const parameters = { viewport: { defaultViewport: 'responsive' } };

const localizationDecorator = makeDecorator({
  wrapper: (storyFn, context) => {
    setupI18n();
    return storyFn(context);
  }
});

export const decorators = [localizationDecorator];
