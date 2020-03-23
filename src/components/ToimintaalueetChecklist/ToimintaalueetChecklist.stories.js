import ToimintaalueetChecklist from './ToimintaalueetChecklist';

const toimintaalueet = [
  {
    id: 0,
    'label-fi': 'Etelä-Karjala',
    'label-sv': 'Södra Karelen'
  },
  {
    id: 1,
    'label-fi': 'Etelä-Pohjanmaa',
    'label-sv': 'Södra Österbotten'
  },
  {
    id: 2,
    'label-fi': 'Etelä-Savo',
    'label-sv': 'Södra Savolax'
  },
  {
    id: 3,
    'label-fi': 'Kainuu',
    'label-sv': 'Kajanaland'
  },
  {
    id: 4,
    'label-fi': 'Kanta-Häme',
    'label-sv': 'Egentliga Tavastland'
  },
  {
    id: 5,
    'label-fi': 'Keski-Pohjanmaa',
    'label-sv': 'Mellersta Österbotten'
  },
  {
    id: 6,
    'label-fi': 'Keski-Suomi',
    'label-sv': 'Mellersta Finland'
  },
  {
    id: 7,
    'label-fi': 'Kymenlaakso',
    'label-sv': 'Kymmenedalen'
  },
  {
    id: 8,
    'label-fi': 'Lappi',
    'label-sv': 'Lappland'
  },
  {
    id: 9,
    'label-fi': 'Pirkanmaa',
    'label-sv': 'Birkaland'
  },
  {
    id: 10,
    'label-fi': 'Pohjanmaa',
    'label-sv': 'Österbotten'
  },
  {
    id: 11,
    'label-fi': 'Pohjois-Karjala',
    'label-sv': 'Norra Karelen'
  },
  {
    id: 12,
    'label-fi': 'Pohjois-Pohjanmaa',
    'label-sv': 'Norra Österbotten'
  },
  {
    id: 13,
    'label-fi': 'Pohjois-Savo',
    'label-sv': 'Norra Savolax'
  },
  {
    id: 14,
    'label-fi': 'Päijät-Häme',
    'label-sv': 'Päijänne-Tavastland'
  },
  {
    id: 15,
    'label-fi': 'Satakunta',
    'label-sv': 'Satakunta'
  },
  {
    id: 16,
    'label-fi': 'Uusimaa',
    'label-sv': 'Nyland'
  },
  {
    id: 17,
    'label-fi': 'Varsinais-Suomi',
    'label-sv': 'Egentliga Finland'
  }
];

export default { title: 'ToimintaalueetChecklist' };

export const withToimintaalueet = () => ({
  Component: ToimintaalueetChecklist,
  props: { toimintaalueet }
});

export const withSelected = () => ({
  Component: ToimintaalueetChecklist,
  props: { toimintaalueet, selected: [1, 2, 3] }
});
