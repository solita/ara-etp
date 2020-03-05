import Pagination from './Pagination.svelte';

export default { title: 'Pagination' };

export const withDefaults = () => ({
  Component: Pagination,
  props: {}
});

export const with10Pages5Active = () => ({
  Component: Pagination,
  props: { pageCount: 10, pageNum: 5 }
});

export const with10Pages1Active = () => ({
  Component: Pagination,
  props: { pageCount: 10, pageNum: 1 }
});

export const with10Pages10Active = () => ({
  Component: Pagination,
  props: { pageCount: 10, pageNum: 10 }
});

export const with1Pages1Active = () => ({
  Component: Pagination,
  props: { pageCount: 1, pageNum: 1 }
});

export const with3Pages2Active = () => ({
  Component: Pagination,
  props: { pageCount: 3, pageNum: 2 }
});

export const with3Pages1Active = () => ({
  Component: Pagination,
  props: { pageCount: 3, pageNum: 1 }
});

export const with3Pages3Active = () => ({
  Component: Pagination,
  props: { pageCount: 3, pageNum: 3 }
});

export const with2Pages1Active = () => ({
  Component: Pagination,
  props: { pageCount: 2, pageNum: 1 }
});

export const with2Pages2Active = () => ({
  Component: Pagination,
  props: { pageCount: 2, pageNum: 2 }
});
