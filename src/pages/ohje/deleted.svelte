<script>
  import { _ } from '@Language/i18n';
  import Navigation from '@Pages/ohje/navigation';

  const i18n = $_;
</script>

<div class="w-full mt-3 flex space-x-4">
  <div class="w-2/6 max-w-xs">
    <Navigation />
  </div>
  <div class="w-4/6 flex-grow">
    <div class="flex flex-col items-center justify-center space-y-6">
      <span class="font-icon text-6xl text-primary"> delete </span>
      <span> {i18n('ohje.editor.delete-success')} </span>
    </div>
  </div>
</div>
