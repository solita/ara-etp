import SigningDialog from './SigningDialog.svelte';
import { energiatodistus2018 } from '@Pages/energiatodistus/empty';

export default { title: 'SigningDialog' };

export const signingWithCardNoConnection = () => ({
  Component: SigningDialog,
  props: {
    energiatodistus: energiatodistus2018(),
    selection: 'card'
  }
});
