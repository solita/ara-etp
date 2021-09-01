import * as R from 'ramda';
import DOMPurify from 'dompurify';
import Marked from 'marked';

Marked.use({
  tokenizer: {
    url: _ => null
  }
});

export const toHtml = R.compose(
  DOMPurify.sanitize,
  Marked
);

export const toPlainText = R.compose(
  R.join(''),
  R.filter(R.test(/(\w|[ÅÄÖ])/i)),
  R.replace(/<\/?[^>]+(>|$)/g, '')
);

export const plainTextValidator = R.over(R.lensProp('predicate'), predicate =>
  R.compose(predicate, toPlainText)
);
