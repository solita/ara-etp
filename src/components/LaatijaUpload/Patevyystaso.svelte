<script>
  import * as R from 'ramda';
  import { locale, _ } from '@Language/i18n';
  import * as Either from '@Utility/either-utils';
  import * as Future from '@Utility/future-utils';
  import * as Fetch from '@Utility/fetch-utils';
  import { patevyystasoStore } from '@/stores';
  import * as LocaleUtils from '@Language/locale-utils';

  export let value;

  const patevyystasoFuture = R.compose(
    Future.coalesce(Either.Left, Either.Right),
    Fetch.responseAsJson,
    Future.encaseP(Fetch.getFetch(fetch))
  )('api/private/patevyydet/');

  // TODO: Get patevyydet only once when store is empty
  Either.isRight($patevyystasoStore) ||
    Future.fork(
      patevyystasoStore.set,
      patevyystasoStore.set,
      patevyystasoFuture
    );

  $: Either.isRight($patevyystasoStore) ||
    R.compose(
      Future.fork(patevyystasoStore.set, patevyystasoStore.set),
      R.chain(Future.after(1000))
    )(patevyystasoFuture);

  $: patevyystaso = Either.foldRight(
    [],
    R.compose(
      LocaleUtils.label($locale),
      R.find(R.propEq('id', value))
    ),
    $patevyystasoStore
  );
</script>

{#if patevyystaso}{patevyystaso}{:else}-{/if}
