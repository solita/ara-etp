<script>
  import * as R from 'ramda';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Future from '@Utility/future-utils';

  import { locale, _ } from '@Language/i18n';

  import LaatijaDropArea from './laatija-upload-droparea';
  import * as LaatijaUploadUtils from './laatija-upload-utils';
  import Button from '@Component/Button/Button';

  import { flashMessageStore } from '@/stores';

  import * as LocaleUtils from '@Language/locale-utils';
  import * as laatijaApi from '@Component/Laatija/laatija-api';

  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';

  let overlay = false;

  const toggleOverlay = value => (overlay = value);

  let patevyystasot = Maybe.None();
  let laatijat = [];
  let files = [];

  $: Future.fork(
    () => {
      toggleOverlay(false);
      flashMessageStore.add('Laatija', 'error', $_('errors.unexpected'));
    },
    response => {
      toggleOverlay(false);
      patevyystasot = Maybe.Some(response);
    },
    R.tap(() => toggleOverlay(true), laatijaApi.patevyydet)
  );

  const fields = [
    'etunimi',
    'sukunimi',
    'henkilotunnus',
    'jakeluosoite',
    'postinumero',
    'postitoimipaikka',
    'email',
    'puhelin',
    'patevyystaso',
    'toteamispaivamaara',
    'toteaja'
  ];

  $: submit = R.compose(
    Future.fork(
      response => {
        toggleOverlay(false);
        const msg = LocaleUtils.uniqueViolationMessage(
          $_,
          response,
          'laatija.messages.save-error'
        );
        flashMessageStore.add('Laatija', 'error', msg);
      },
      _ => {
        toggleOverlay(false);
        flashMessageStore.add(
          'Laatija',
          'success',
          $_('laatija.messages.save-success')
        );
        laatijat = [];
      }
    ),
    Future.delay(300),
    R.tap(() => toggleOverlay(true)),
    laatijaApi.uploadLaatijat
  );

  $: labelLocale = LocaleUtils.label($locale);

  $: formats = R.curry((key, value) =>
    R.defaultTo(
      R.identity,
      R.prop(key, {
        patevyystaso: patevyys =>
          R.compose(
            Maybe.orSome(patevyys),
            R.map(labelLocale),
            R.chain(Maybe.findById(parseInt(patevyys)))
          )(patevyystasot)
      })
    )(value)
  );

  $: model = R.map(
    R.compose(LaatijaUploadUtils.validate, LaatijaUploadUtils.parse),
    laatijat
  );

  $: errors = R.compose(
    R.filter(R.length),
    R.addIndex(R.map)((item, index) =>
      R.compose(
        Maybe.orSome(''),
        R.map(LaatijaUploadUtils.formatRowError($_, index))
      )(item)
    ),
    LaatijaUploadUtils.errors($_)
  )(model);

  $: if (errors.length) {
    flashMessageStore.add('Laatija', 'error', R.join(' | ', errors));
  }
</script>

<style>
  .breakout {
    @apply relative ml-8 mt-4 overflow-x-auto;
    width: 95vw;
    left: calc(-50vw + 50%);
  }

  .invalid {
    @apply text-error font-bold;
  }
</style>

<!-- purgecss: invalid -->

<Overlay {overlay}>
  <div slot="content">
    <LaatijaDropArea bind:laatijat bind:files />
    {#if laatijat.length}
      <div class="breakout overflow-x-auto">
        <table class="etp-table">
          <thead class="etp-table--thead">
            <tr class="etp-table--tr">
              <th class="etp-table--th">{$_('rivi')}</th>
              {#each fields as field}
                <th class="etp-table--th">{$_(`laatijaupload.${field}`)}</th>
              {/each}
            </tr>
          </thead>
          <tbody class="etp-table--tbody">
            {#each laatijat as laatija, index}
              <tr data-cy="laatija-upload-row" class="etp-table--tr">
                <td class="etp-table--td">
                  {index + 1}
                </td>
                {#each fields as field}
                  <td
                    class="etp-table--td"
                    class:invalid={R.compose(
                      Either.isLeft,
                      R.prop(field),
                      LaatijaUploadUtils.validate,
                      LaatijaUploadUtils.parse
                    )(laatija)}>
                    {formats(field, laatija[field])}
                  </td>
                {/each}
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
      <form
        on:submit|preventDefault={_ => {
          flashMessageStore.flush('Laatija');
          submit(model);
        }}
        on:reset|preventDefault={_ => {
          flashMessageStore.flush('Laatija');
          files = [];
          laatijat = [];
        }}>
        <div class="flex -mx-4 pt-8">
          <div class="px-4">
            <Button
              prefix="laatija-upload"
              disabled={errors.length}
              type={'submit'}
              text={$_('laatija.lisaa-laatijat')} />
          </div>
          <div class="px-4">
            <Button
              prefix="laatija-upload"
              style={'secondary'}
              type={'reset'}
              text={$_('peruuta')} />
          </div>
        </div>
      </form>
    {/if}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
