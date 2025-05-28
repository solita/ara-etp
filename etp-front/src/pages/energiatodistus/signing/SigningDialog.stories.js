import SigningDialog from './SigningDialog.svelte';
import { energiatodistus2018 } from '@Pages/energiatodistus/empty';

export default { title: 'SigningDialog' };

export const signingWithoutCard = () => ({
  Component: SigningDialog,
  props: {
    energiatodistus: energiatodistus2018(),
    freshSession: true
  }
});

export const signingWithoutCardStaleSession = () => ({
  Component: SigningDialog,
  props: {
    energiatodistus: energiatodistus2018(),
    freshSession: false
  }
});
