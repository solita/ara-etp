<script>
  import * as R from 'ramda';

  import { _ } from '@Language/i18n';

  import H1 from '@Component/H1/H1';
  import Input from '@Component/Input/Input';
  import Checkbox from '@Component/Checkbox/Checkbox';
</script>

<style type="text/postcss">

</style>

<form>
  <div class="w-full mt-3">
    <H1 text="Perustiedot" />
    <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'henkilotunnus'}
          name={'henkilotunnus'}
          label={$_('laatija.henkilotunnus')}
          required={true}
          disabled={true}
          i18n={$_} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'etunimi'}
          name={'etunimi'}
          label={$_('laatija.etunimi')}
          required={true}
          i18n={$_} />
      </div>

      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'sukunimi'}
          name={'sukunimi'}
          label={$_('laatija.sukunimi')}
          required={true}
          i18n={$_} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'sahkoposti'}
          name={'sahkoposti'}
          label={`${$_('laatija.sahkoposti')}(${R.toLower($_('laatija.kayttajatunnus'))})`}
          required={true}
          i18n={$_} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'puhelinnumero'}
          name={'puhelinnumero'}
          label={$_('laatija.puhelinnumero')}
          required={true}
          i18n={$_} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
      <div class="lg:py-0 w-full px-4 py-4">
        <Input
          id={'katuosoite'}
          name={'katuosoite'}
          label={$_('laatija.katuosoite')}
          required={true}
          i18n={$_} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4 my-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'postinumero'}
          name={'postinumero'}
          label={$_('laatija.postinumero')}
          required={true}
          i18n={$_} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'postitoimipaikka'}
          name={'postitoimipaikka'}
          label={$_('laatija.postitoimipaikka')}
          required={true}
          i18n={$_} />
      </div>

      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'maa'}
          name={'maa'}
          label={$_('laatija.maa')}
          required={true}
          i18n={$_} />
      </div>
    </div>
  </div>
  <div class="mt-8">
    <H1 text={$_('laatija.laatijatiedot')} />
    <div class="flex lg:flex-row flex-col py-4 -mx-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'patevyydenvoimassaolo'}
          name={'patevyydenvoimassaolo'}
          label={$_('laatija.patevyydenvoimassaolo')}
          disabled={true}
          required={true}
          i18n={$_} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'patevyystaso'}
          name={'patevyystaso'}
          label={$_('laatija.patevyystaso')}
          disabled={true}
          required={true}
          i18n={$_} />
      </div>
    </div>
    <div class="flex lg:flex-row flex-col py-4 -mx-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 py-4">
        <Input
          id={'paatoimintaalue'}
          name={'paatoimintaalue'}
          label={$_('laatija.paatoimintaalue')}
          required={true}
          i18n={$_} />
      </div>
    </div>
  </div>

  <div class="mt-8">
    <H1 text={$_('laatija.julkisettiedot')} />
    <div class="flex flex-col py-4 -mx-4">
      <div class="lg:w-1/3 lg:py-0 w-full px-4 mb-2">
        <Checkbox label={$_('laatija.puhelinnumero')} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 my-2">
        <Checkbox label={$_('laatija.sahkoposti')} />
      </div>
      <div class="lg:w-1/3 lg:py-0 w-full px-4 mt-2">
        <Checkbox label={$_('laatija.katuosoite')} />
      </div>
    </div>
  </div>
</form>
