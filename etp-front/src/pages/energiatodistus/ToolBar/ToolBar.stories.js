import ToolBar from './toolbar';
import { energiatodistus2018 } from '@Pages/energiatodistus/empty';

export default { title: 'ToolBar' };

export const withDefaults = () => ({
  Component: ToolBar,
  props: { energiatodistus: energiatodistus2018() }
});
