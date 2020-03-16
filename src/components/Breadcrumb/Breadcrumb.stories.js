import Breadcrumb from './Breadcrumb';

export default { title: 'Breadcrumb' };

export const withText = () => ({
  Component: Breadcrumb,
    props: {
      value: [
        {label: 'Some example 1',
         url: 'https://example.com'},
        {label: 'Some example 2',
         url: 'https://google.com'}
      ]
    }
});
