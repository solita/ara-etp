import SigningDialog from './SigningDialog.svelte';
import { energiatodistus2018 } from '@Pages/energiatodistus/empty';

export default { title: 'SigningDialog' };

export const signingWithCardNoConnection = () => ({
  Component: SigningDialog,
  props: {
    energiatodistus: energiatodistus2018(),
    selection: 'card',
    allowSelection: true
  }
});

export const signingWithoutCard = () => ({
  Component: SigningDialog,
  props: {
    energiatodistus: energiatodistus2018(),
    selection: 'system',
    allowSelection: true,
    freshSession: true
  }
});

export const signingWithoutCardStaleSession = () => ({
  Component: SigningDialog,
  props: {
    energiatodistus: energiatodistus2018(),
    selection: 'system',
    allowSelection: true,
    freshSession: false
  }
});
