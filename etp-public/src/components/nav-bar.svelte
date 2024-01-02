<script>
  import IconHome from '@Asset/icons/home.svg';
  import IconSearchFile from '@Asset/icons/search_file-light.svg';
  import IconSearchPerson from '@Asset/icons/search_person-light.svg';
  import IconStatistics from '@Asset/icons/stats.svg';

  import NavButton, { styles as navbuttonStyles } from '@Component/nav-button';
  import Container, { styles as containerStyles } from '@Component/container';
  import { _ } from '@Localization/localization';

  export let navButtonClicked = () => {};
</script>

<style>
  img {
    width: 2.5em;
    height: 2.5em;
  }
</style>

<Container {...containerStyles.mainnavigation}>
  <nav class="flex xl:px-16 flex-col lg:flex-row">
    <NavButton
      link="/"
      {...navbuttonStyles.lightgreen}
      click={navButtonClicked}>
      <img slot="iconleft" src={IconHome} alt={$_('NAVBAR_ICON_ALT_ETUSIVU')} />
      <span slot="title" class="uppercase">{$_('NAVBAR_ETUSIVU')}</span>
      <span slot="subtitle" class="font-light text-sm">
        {$_('NAVBAR_ETUSIVU_KUVAUS')}
      </span>
    </NavButton>
    <NavButton
      link="/ethaku"
      {...navbuttonStyles.green}
      click={navButtonClicked}>
      <img
        slot="iconleft"
        src={IconSearchFile}
        alt={$_('NAVBAR_ICON_ALT_ETHAKU')} />
      <span slot="title" class="uppercase">
        {$_('NAVBAR_ENERGIATODISTUSHAKU')}
      </span>
      <span slot="subtitle" class="font-light text-sm">
        {$_('NAVBAR_ENERGIATODISTUSHAKU_KUVAUS')}
      </span>
    </NavButton>
    <NavButton
      link="/laatijahaku"
      {...navbuttonStyles.darkgreen}
      click={navButtonClicked}>
      <img
        slot="iconleft"
        src={IconSearchPerson}
        alt={$_('NAVBAR_ICON_ALT_LAATIJAHAKU')} />
      <span slot="title" class="uppercase">{$_('NAVBAR_LAATIJAHAKU')}</span>
      <span slot="subtitle" class="font-light text-sm">
        {$_('NAVBAR_LAATIJAHAKU_KUVAUS')}
      </span>
    </NavButton>
    <NavButton
      link="/tilastot"
      {...navbuttonStyles.ashblue}
      click={navButtonClicked}>
      <img
        slot="iconleft"
        src={IconStatistics}
        alt={$_('NAVBAR_ICON_ALT_TILASTOT')} />
      <span slot="title" class="uppercase">{$_('NAVBAR_TILASTOT')}</span>
      <span slot="subtitle" class="font-light text-sm">
        {$_('NAVBAR_TILASTOT_KUVAUS')}
      </span>
    </NavButton>
  </nav>
</Container>
