<script>
  import * as R from 'ramda';
  import { querystring } from 'svelte-spa-router';
  import qs from 'qs';

  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Future from '@Utility/future-utils';
  import * as Response from '@Utility/response';
  import * as Validation from '@Utility/validation';
  import * as Kayttajat from '@Utility/kayttajat';
  import * as Parsers from '@Utility/parsers';
  import * as Locales from '@Language/locale-utils';

  import * as api from './viesti-api';
  import * as kayttajaApi from '@Pages/kayttaja/kayttaja-api';
  import * as laatijaApi from '@Pages/laatija/laatija-api';
  import * as Viestit from './viesti-util';
  import * as Schema from './schema';

  import { flashMessageStore } from '@/stores';
  import { _, locale } from '@Language/i18n';
  import * as Router from '@Component/Router/router';

  import Overlay from '@Component/Overlay/Overlay.svelte';
  import H1 from '@Component/H/H1.svelte';
  import Button from '@Component/Button/Button.svelte';
  import Textarea from '@Component/Textarea/Textarea.svelte';
  import Spinner from '@Component/Spinner/Spinner.svelte';
  import Input from '@Component/Input/Input.svelte';
  import DirtyConfirmation from '@Component/Confirm/dirty.svelte';
  import Autocomplete from '@Component/Autocomplete/Autocomplete.svelte';
  import Select from '@Component/Select/Select.svelte';

  const i18nRoot = 'viesti.ketju.new';
  const i18n = $_;

  export let etFuture = Future.resolve(Maybe.None());

  let resources = Maybe.None();
  let dirty = false;
  let overlay = true;
  let enableOverlay = _ => {
    overlay = true;
  };

  let ketju = Viestit.emptyKetju();
  let defaultKetju = Viestit.emptyKetju();

  const formatVastaanottaja = kayttaja =>
    `${kayttaja.etunimi} ${kayttaja.sukunimi} | ${kayttaja.email}`;

  // List[Kayttaja] -> Str -> Either[Int]
  const parseVastaanottaja = kayttajat =>
    R.compose(
      Maybe.toEither(R.applyTo(`${i18nRoot}.messages.vastaanottaja-not-found`)),
      R.map(R.prop('id')),
      predicate => Maybe.find(predicate, kayttajat),
      R.propEq('email'),
      R.compose(R.unless(R.isNil, R.trim), R.nth(1), R.split('|'))
    );

  const arrayHeadLens = R.lens(
    R.compose(Either.Right, Maybe.head),
    R.compose(Maybe.toArray, EM.toMaybe)
  );

  R.compose(
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            `${i18nRoot}.messages.load-error`,
            Response.localizationKey(response)
          )
        );

        flashMessageStore.add('viesti', 'error', msg);
        overlay = false;
      },
      response => {
        resources = Maybe.Some(response);
        defaultKetju = Viestit.defaultKetju(
          response.energiatodistus,
          response.whoami
        );
        ketju = Viestit.defaultKetju(response.energiatodistus, response.whoami);
        overlay = false;
      }
    ),
    R.chain(whoami =>
      Future.parallelObject(4, {
        vastaanottajaryhmat: api.vastaanottajaryhmat,
        whoami: Future.resolve(whoami),
        energiatodistus: etFuture,
        laatijat: Viestit.isAllowedToSendToEveryone(whoami)
          ? laatijaApi.laatijat
          : Future.resolve([])
      })
    )
  )(kayttajaApi.whoami);

  const addKetju = R.compose(
    Future.fork(
      response => {
        const msg = i18n(
          Maybe.orSome(
            `${i18nRoot}.messages.error`,
            Response.localizationKey(response)
          )
        );
        flashMessageStore.add('viesti', 'error', msg);
        overlay = false;
      },
      _ => {
        flashMessageStore.add(
          'viesti',
          'success',
          i18n(`${i18nRoot}.messages.success`)
        );
        dirty = false;
        Router.pop();
      }
    ),
    api.postKetju(fetch)
  );

  const isValidForm = Validation.isValidForm(Schema.ketju);

  const submitNewKetju = event => {
    if (isValidForm(ketju)) {
      addKetju(ketju);
    } else {
      flashMessageStore.add(
        'viesti',
        'error',
        i18n(`${i18nRoot}.messages.validation-error`)
      );
      Validation.blurForm(event.target);
    }
  };

  const cancel = _ => {
    ketju = defaultKetju;
    dirty = false;
  };

  $: {
    const query = qs.parse($querystring);
    if (R.has('subject', query)) {
      ketju = R.assoc('subject', `Re: [${query.subject}]`, ketju);
    }
  }

  $: isVastaanottajaRequired = Maybe.isNone(
    R.prop('vastaanottajaryhma-id', ketju)
  );

  $: isVastaanottajaRyhmaRequired = R.isEmpty(R.prop('vastaanottajat', ketju));
</script>

<style>
</style>

<Overlay {overlay}>
  <div slot="content" class="w-full mt-3">
    <DirtyConfirmation {dirty} />
    {#each resources.toArray() as { whoami, laatijat, vastaanottajaryhmat, energiatodistus }}
      <H1 text={i18n(`${i18nRoot}.title`)} />
      <form
        id="ketju"
        on:submit|preventDefault={submitNewKetju}
        on:input={_ => {
          dirty = true;
        }}
        on:change={_ => {
          dirty = true;
        }}>
        {#if Viestit.isAllowedToSendToEveryone(whoami)}
          <div class="lg:w-1/2 w-full py-4">
            <Autocomplete
              items={R.map(formatVastaanottaja, laatijat)}
              size="10">
              <Input
                id={'ketju.vastaanottaja'}
                name={'ketju.vastaanottaja'}
                label={i18n('viesti.ketju.vastaanottaja')}
                required={isVastaanottajaRequired}
                disabled={Maybe.isSome(energiatodistus)}
                bind:model={ketju}
                lens={R.compose(R.lensProp('vastaanottajat'), arrayHeadLens)}
                parse={Parsers.optionalParser(parseVastaanottaja(laatijat))}
                format={R.compose(
                  Maybe.orSome(''),
                  R.map(formatVastaanottaja),
                  R.chain(id => Maybe.findById(id, laatijat))
                )}
                {i18n} />
            </Autocomplete>
          </div>
        {/if}

        {#if !Viestit.isAllowedToSendToEveryone(whoami) || Maybe.isNone(energiatodistus)}
          <div class="lg:w-1/2 w-full py-4">
            <Select
              id={'ketju.vastaanottajaryhma'}
              label={i18n('viesti.ketju.vastaanottajaryhma')}
              required={isVastaanottajaRyhmaRequired}
              disabled={!Viestit.isAllowedToSendToEveryone(whoami)}
              allowNone={true}
              bind:model={ketju}
              parse={Maybe.fromNull}
              lens={R.lensProp('vastaanottajaryhma-id')}
              format={Locales.labelForId($locale, vastaanottajaryhmat)}
              items={R.pluck('id', vastaanottajaryhmat)} />
          </div>
        {/if}

        <div class="w-full py-4">
          <Input
            id={'ketju.subject'}
            name={'ketju.subject'}
            label={i18n('viesti.ketju.subject')}
            required={true}
            bind:model={ketju}
            lens={R.lensProp('subject')}
            parse={R.trim}
            validators={Schema.ketju.subject}
            {i18n} />
        </div>
        <div class="w-full py-4">
          <Textarea
            id={'ketju.body'}
            name={'ketju.body'}
            label={i18n('viesti.ketju.body')}
            bind:model={ketju}
            lens={R.lensProp('body')}
            required={true}
            parse={R.trim}
            validators={Schema.ketju.body}
            {i18n} />
        </div>
        {#each Maybe.toArray(energiatodistus) as et}
          <p>Liittyy energiatodistukseen {et.id}</p>
        {/each}
        <div class="flex space-x-4 pt-8">
          <Button
            disabled={!dirty}
            type={'submit'}
            text={i18n(`${i18nRoot}.submit`)} />
          <Button
            disabled={!dirty}
            on:click={cancel}
            text={i18n(`${i18nRoot}.reset`)}
            type={'reset'}
            style={'secondary'} />
        </div>
      </form>
    {/each}
  </div>
  <div slot="overlay-content">
    <Spinner />
  </div>
</Overlay>
