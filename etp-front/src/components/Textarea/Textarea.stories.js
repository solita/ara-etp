import Textarea from './Textarea';
import * as validation from '@Utility/validation';
import { _ } from '@Language/i18n';

export default { title: 'Textarea' };

export const emptyTextarea = () => ({
  Component: Textarea,
  props: {
    id: 'id',
    label: 'Textarea',
    validators: [
      validation.minLengthConstraint(2),
      validation.maxLengthConstraint(200)
    ],
    i18n: i => i
  }
});
