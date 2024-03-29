<script>
  import * as R from 'ramda';
  import * as Future from '@Utility/future-utils';
  import { location } from 'svelte-spa-router';
  import { idTranslateStore } from '@/stores';

  import * as yritysApi from '@Pages/yritys/yritys-api';
  import * as kayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import * as viestiApi from '@Pages/viesti/viesti-api';

  let cancel = () => {};

  const yritysFuture = ([id]) =>
    R.includes(id, ['new', 'all'])
      ? Future.reject()
      : yritysApi.getYritysById(id);

  const kayttajaFuture = ([id]) =>
    R.includes(id, ['all', 'laatijoidentuonti', 'new'])
      ? Future.reject()
      : kayttajaApi.getKayttajaById(id);

  const mapRoot = R.when(R.equals('laatija'), R.always('kayttaja'));

  $: [root, id] = R.compose(R.split('/'), R.tail)($location);

  $: cancel = R.compose(
    Future.fork(
      () => {},
      R.cond([
        [
          R.propEq('yritys', 'type'),
          R.compose(idTranslateStore.updateYritys, R.prop('payload'))
        ],
        [
          R.propEq('kayttaja', 'type'),
          R.compose(idTranslateStore.updateKayttaja, R.prop('payload'))
        ]
      ])
    ),
    R.chain(Future.parallelObject(2)),
    R.chain(Future.after(500)),
    R.map(
      R.cond([
        [
          R.compose(R.equals('yritys'), R.head),
          R.compose(
            R.assoc('payload', R.__, { type: Future.resolve('yritys') }),
            yritysFuture,
            R.tail
          )
        ],
        [
          R.compose(R.equals('kayttaja'), R.head),
          R.compose(
            R.assoc('payload', R.__, { type: Future.resolve('kayttaja') }),
            kayttajaFuture,
            R.tail
          )
        ],
        [R.T, R.always({ payload: Future.reject() })]
      ])
    ),
    R.ifElse(
      R.hasPath([mapRoot(root), parseInt(id, 10)]),
      R.always(Future.reject()),
      R.always(Future.resolve([mapRoot(root), id]))
    ),
    R.tap(cancel)
  )($idTranslateStore);
</script>

<slot idTranslate={$idTranslateStore} />
