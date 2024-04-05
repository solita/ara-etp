<script>
  import * as R from 'ramda';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';

  import { locale, _ } from '@Language/i18n';

  import LaatijaDropArea from './laatija-upload-droparea';
  import * as LaatijaUploadUtils from './laatija-upload-utils';
  import Button from '@Component/Button/Button';

  import { announcementsForModule } from '@Utility/announce';

  import * as LocaleUtils from '@Language/locale-utils';
  import * as laatijaApi from '@Pages/laatija/laatija-api';

  import Overlay from '@Component/Overlay/Overlay';
  import Spinner from '@Component/Spinner/Spinner';
  import * as Locales from '@Language/locale-utils';

  const i18n = $_;
  const i18nRoot = 'laatija.upload';
  const { announceError, announceSuccess, clearAnnouncements } =
    announcementsForModule('Laatija');

  let overlay = false;

  const toggleOverlay = value => (overlay = value);

  let patevyystasot = Maybe.None();
  let laatijat = [];
  let files = [];

  $: Future.fork(
    response => {
      toggleOverlay(false);
      announceError(i18n(Response.errorKey(i18nRoot, 'load', response)));
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
        const msg = Locales.uniqueViolationMessage(
          i18n,
          response,
          Response.errorKey(i18nRoot, 'save', response)
        );
        announceError(msg);
      },
      _ => {
        toggleOverlay(false);
        announceSuccess(i18n(i18nRoot + '.messages.save-success'));
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
        R.map(LaatijaUploadUtils.formatRowError(i18n, index))
      )(item)
    ),
    LaatijaUploadUtils.errors(i18n)
  )(model);

  $: if (errors.length) {
    announceError(R.join(' | ', errors));
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

<Overlay {overlay}>
  <div slot="content">
    <LaatijaDropArea bind:laatijat bind:files />
    {#if laatijat.length}
      <div class="breakout overflow-x-auto">
        <table class="etp-table">
          <thead class="etp-table--thead">
            <tr class="etp-table--tr">
              <th class="etp-table--th">{i18n(i18nRoot + '.rivi')}</th>
              {#each fields as field}
                <th class="etp-table--th">{i18n(i18nRoot + '.' + field)}</th>
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
          clearAnnouncements();
          submit(model);
        }}
        on:reset|preventDefault={_ => {
          clearAnnouncements();
          files = [];
          laatijat = [];
        }}>
        <div class="flex -mx-4 pt-8">
          <div class="px-4">
            <Button
              prefix="laatija-upload"
              disabled={errors.length}
              type={'submit'}
              text={i18n(i18nRoot + '.save')} />
          </div>
          <div class="px-4">
            <Button
              prefix="laatija-upload"
              style={'secondary'}
              type={'reset'}
              text={i18n(i18nRoot + '.cancel')} />
          </div>
        </div>
      </form>
    {/if}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
