import Breadcrumb from './Breadcrumb';

export default { title: 'Breadcrumb' };

export const withText = () => ({
  Component: Breadcrumb,
    props: {
        value: [
            ['Some example 1', 'https://example.com'],
            ['Some example 2', 'https://google.com']
        ]
    }
});
