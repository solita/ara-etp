<script>
  import { _ } from '@Language/i18n';
  import Navigation from './navigation';
</script>

<style>
  .material-icons {
    font-size: 54px;
  }
</style>

<div class="w-full mt-3 flex space-x-4">
  <div class="w-2/6 max-w-xs">
    <Navigation />
  </div>
  <div class="w-4/6 flex-grow">
    <div class="flex flex-col items-center justify-center space-y-6">
      <span class="material-icons text-primary"> delete </span>
      <span> {$_(`ohje.editor.delete-success`)}</span>
    </div>
  </div>
</div>
