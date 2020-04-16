import Textarea from './Textarea';

export default { title: 'Textarea' };

export const emptyTextarea = () => ({
  Component: Textarea,
  props: {
    id: 'id',
    label: 'Textarea'
  }
});
