<script>
  import * as LaatijaUtils from './laatija-utils';
  import * as Maybe from '@Utility/maybe-utils';

  import LaatijaForm from './LaatijaForm';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';

  export let params;

  let laatija = Maybe.None();
  let overlay = false;

  const submit = () => {};
  const disabled = false;
</script>

<Overlay {overlay}>
  <div slot="content">
    <LaatijaForm />
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
