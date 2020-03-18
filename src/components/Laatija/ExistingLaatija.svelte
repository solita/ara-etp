<script>
  import * as R from 'ramda';
  import * as LaatijaUtils from './laatija-utils';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Future from '@Utility/future-utils';

  import LaatijaForm from './LaatijaForm';
  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';

  export let params;

  let laatija = Maybe.None();
  let overlay = false;

  const submit = () => {};
  const disabled = false;

  $: R.compose(
    Future.fork(console.error, console.log),
    Maybe.getOrElse(Future.resolve(LaatijaUtils.emptyLaatija())),
    R.map(LaatijaUtils.laatijaFuture(fetch)),
    Maybe.fromNull,
    R.prop('id')
  )(params);
</script>

<Overlay {overlay}>
  <div slot="content">
    <LaatijaForm />
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
