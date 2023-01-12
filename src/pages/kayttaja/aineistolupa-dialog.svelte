<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import * as Either from '@Utility/either-utils';
  import * as EM from '@Utility/either-maybe';
  import * as Parsers from '@Utility/parsers';
  import * as Formats from '@Utility/formats';
  import * as Schema from './schema';

  import * as Kayttajat from '@Utility/kayttajat';

  import { _ } from '@Language/i18n';

  import Input from '@Component/Input/Input';
  import Button from '@Component/Button/Button';
  import Datepicker from '@Component/Input/Datepicker';

  const i18n = $_;
  const i18nRoot = 'kayttaja';

  export let aineistot;
  export let model;
  export let aineistoIndex;
  export let kayttaja;

  export let reload;

  const aineistoLabel = Maybe.orSome(
    '',
    R.map(R.prop('label-fi'), Maybe.findById(model[aineistoIndex], aineistot))
  );

  const clearModel = R.compose(
    R.set(R.lensPath([aineistoIndex, 'valid-until']), Either.Right(Maybe.None())),
    R.set(R.lensPath([aineistoIndex, 'ip-address']), '')
  );
  const clear = () => model = clearModel(model);

  const schema = Schema.aineistolupa;
</script>

<style type="text/postcss">
  dialog {
    @apply fixed top-0 w-screen left-0 z-50 h-screen bg-hr cursor-default flex justify-center items-center;
  }

  .content {
    @apply relative bg-light w-2/3 py-10 px-10 rounded-md shadow-lg flex flex-col justify-center;
  }

  h1 {
    @apply text-secondary font-bold uppercase text-lg mb-4 pb-2 border-b-1 border-tertiary tracking-xl;
  }

  .buttons {
    @apply flex flex-wrap items-center mt-5 border-t-1 border-tertiary;
  }
</style>

<dialog on:click|stopPropagation>
  <form class="content">
    <h1>{i18n(i18nRoot + '.aineisto-lupa')}</h1>

    <div class="w-full py-4">
      <dl>
        <dt>Käyttäjä:</dt>
        <dd>{Kayttajat.fullName(kayttaja)}</dd>

        <dt>Aineisto:</dt>
        <dd>{aineistoLabel}</dd>
      </dl>
    </div>

    <Datepicker
      id="valid-until"
      name="valid-until"
      label="Voimassa"
      bind:model={model}
      lens={R.lensPath([aineistoIndex, 'valid-until'])}
      format={Maybe.fold('', Formats.formatDateInstant)}
      parse={Parsers.optionalParser(Parsers.parseDate)}
      required={false}
      transform={EM.fromNull} />

    <Input
      id="ip-address"
      name="ip-address"
      label="IP-osoite"
      bind:model={model}
      lens={R.lensPath([aineistoIndex, 'ip-address'])}
      validators={schema['ip-address']}
      {i18n} />

    <div class="buttons">
      <div class="mr-5 mt-5">
        <Button
          text={i18n(i18nRoot + '.back')}
          style={'secondary'}
          on:click={reload} />
      </div>
      <div class="mt-5">
        <Button
          text={i18n(i18nRoot + '.clear')}
          on:click={clear}
          style={'error'}
      />
      </div>
    </div>
  </form>
</dialog>
