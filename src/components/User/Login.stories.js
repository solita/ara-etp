import { Some } from '../../utils/maybe-utils';
import Login from './Login.svelte';

export default { title: 'Login' };

export const withLoggedIn = () => ({
    Component: Login,
    props: {storyBookLoggedIn: Some(true)}
});

export const withLoggedOut = () => ({
    Component: Login,
    props: {storyBookLoggedIn: Some(false)}
});
