import { Some } from '../../utils/maybe-utils';
import Login from './Login.svelte';

export default { title: 'Login' };

export const withLoggedOut = () => ({
    Component: Login
});
