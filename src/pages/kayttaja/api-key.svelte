<script>
  import * as R from 'ramda';
  import * as Maybe from '@Utility/maybe-utils';
  import { generatePassword } from '@Utility/password';
  import * as Schema from '@Pages/kayttaja/schema';

  import { locale, _ } from '@Language/i18n';

  import Button from '@Component/Button/Button';
  import Input from '@Component/Input/Input';

  export let dirty;
  export let kayttaja;

  const i18n = $_;
  const i18nRoot = 'kayttaja';
  const schema = Schema.Kayttaja;

  const btoa = str => new Buffer.from(str, 'binary').toString('base64');
</script>

<div class="flex">
  <div class="w-full">
    <Input
      id={'api-key'}
      name={'api-key'}
      label={i18n('kayttaja.api-key.label')}
      required={false}
      bind:model={kayttaja}
      lens={R.lensProp('api-key')}
      format={R.compose(
        Maybe.orSome(i18n('kayttaja.api-key.placeholder')),
        R.map(R.compose(btoa, key => kayttaja.email + ':' + key))
      )}
      parse={R.compose(Maybe.fromEmpty, R.trim)}
      validators={schema['api-key']}
      disabled={true}
      {i18n} />
  </div>
  <Button
    style={'secondary'}
    on:click={() => {
      kayttaja = R.assoc('api-key', Maybe.Some(generatePassword()), kayttaja);
      dirty = true;
    }}
    text={i18n('kayttaja.api-key.luo-uusi')}
    disabled={kayttaja.email === ''} />
</div>
