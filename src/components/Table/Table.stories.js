import Table from './Table.svelte';
export default { title: 'Table' };

const removeByIndex = index => {};
export const withFieldsAndContent = () => ({
  Component: Table,
  props: {
    fields: [
      { id: 'field1', title: 'Field 1', sort: 'ascend' },
      { id: 'field2', title: 'Field 2', sort: 'descend' },
      { id: 'field3', title: 'Field 3' },
      {
        type: 'action',
        title: 'Remove',
        actions: [{ type: 'remove', update: removeByIndex }]
      }
    ],
    tablecontents: [
      { field1: 11, field2: 12, field3: 13 },
      { field1: 21, field2: 22, field3: 23 },
      { field1: 31, field2: 32, field3: 33 }
    ]
  }
});
