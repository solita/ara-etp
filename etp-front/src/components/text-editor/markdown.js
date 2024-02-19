import * as R from 'ramda';
import DOMPurify from 'dompurify';
import { marked } from 'marked';

marked.use({
  tokenizer: {
    url: _ => null
  }
});

export const toHtml = R.compose(DOMPurify.sanitize, marked);

export const toPlainText = R.compose(
  R.join(''),
  R.filter(R.test(/(\w|[ÅÄÖ])/i)),
  R.replace(/<\/?[^>]+(>|$)/g, '')
);

export const plainTextValidator = R.over(R.lensProp('predicate'), predicate =>
  R.compose(predicate, toPlainText)
);
