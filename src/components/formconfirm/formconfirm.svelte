<script>
  import { _ } from '@Language/i18n';

  let dirty = false;
  let currentPushed = false;

  const pushCurrent = () => {
    history.pushState(null, null, null);
    currentPushed = true;
  };

  const backWithoutConfirm = () => {
    dirty = false;
    history.back();
  };

  const popAndReload = () => {
    if (currentPushed) {
      history.back();
    }

    window.location.reload();
  };

  const confirmBack = () => {
    if (dirty) {
      const confirm = window.confirm($_('navigation.form-confirm'));

      if (confirm) {
        dirty = false;
        history.back();
      } else {
        pushCurrent();
      }
    }
  };

  const confirmUnload = evt => {
    if (dirty) {
      if (currentPushed) {
        backWithoutConfirm();
      }

      evt.preventDefault();
      evt.returnValue = $_('navigation.form.confirm');
      return $_('navigation.form.confirm');
    }
  };

  const formChange = () => {
    if (!currentPushed) {
      pushCurrent();
    }

    dirty = true;
  };

  const reset = evt => {
    evt.preventDefault();
    dirty = false;
    popAndReload();
  };

  const beforeSubmit = () => {
    if (!currentPushed) {
      pushCurrent();
    }
    backWithoutConfirm();
  };
</script>

<svelte:window on:beforeunload={confirmUnload} on:popstate={confirmBack} />

<slot {dirty} {formChange} {reset} {beforeSubmit} />
