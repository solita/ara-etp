<script>
    import * as R from 'ramda';
    import * as Maybe from '@Utility/maybe-utils';
    import * as Future from '@Utility/future-utils';
    import * as Response from '@Utility/response';
    import * as Schema from './schema';
  
    import * as KayttajaApi from './kayttaja-api';
    import * as Kayttajat from '@Utility/kayttajat';
  
    import { _ } from '@Language/i18n';
  
    import Button from '@Component/Button/Button';
    import Datepicker from '@Component/Input/Datepicker';
    import Textarea from '@Component/Textarea/Textarea';
    import { flashMessageStore } from '@/stores';
    import * as Validation from '@Utility/validation';
  
    const i18n = $_;
    const i18nRoot = 'kayttaja';
  
    export let aineistot;
    export let model;
    export let aineistoId;
    export let kayttaja;

    export let reload;
    export let save = () => console.log("Should save aineistot", model, "for kayttaja", kayttaja);
    
    let error = Maybe.None();

    const defaultPermit = {
      'valid-until': Maybe.None(),
      'ip-address': '',
    };

    const existingPermit = (aineistoId, model) => R.compose(Maybe.orSome(defaultPermit), R.map(R.props(['ip-address', 'valid-until'])),Maybe.find(R.propEq('id', aineistoId)));

    let permit = aine {
      'valid-until': Maybe.None(),
      'ip-address': '',
    };

    const schema = Schema.aineistolupa;
    $: isValidForm = Validation.isValidForm(schema);
  
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
  
    .error {
      @apply flex py-2 px-2 bg-error text-light;
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
          <dd>{aineistot[aineistoId]['label-fi']}</dd>
        </dl>
      </div>

      <Datepicker />

      {#each error.toArray() as txt}
        <div class="my-2 error">
          <span class="font-icon mr-2">error_outline</span>
          <div>{txt}</div>
        </div>
      {/each}
  
      <div class="buttons">
        <div class="mr-5 mt-5">
          <Button text={i18n('tallenna')} on:click={save} />
        </div>
  
        <div class="mt-5">
          <Button
            text={i18n(i18nRoot + '.cancel')}
            style={'secondary'}
            on:click={reload} />
        </div>
      </div>
    </form>
  </dialog>
  