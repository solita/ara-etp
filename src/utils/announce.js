import { flashMessageStore } from '@/stores';
import { announceAssertively } from '@Utility/aria-live';

const announceSuccess = module => msg => {
  announceAssertively(msg);
  flashMessageStore.add(module, 'success', msg);
};

const announceError = module => msg => {
  announceAssertively(msg);
  flashMessageStore.add(module, 'error', msg);
};

const annouceWarning = module => msg => {
  announceAssertively(msg);
  flashMessageStore.add(module, 'warn', msg);
};

const flush = module => () => flashMessageStore.flush(module);

export const announcementsForModule = module => ({
  announceError: announceError(module),
  announceSuccess: announceSuccess(module),
  announceWarning: annouceWarning(module),
  clearAnnouncements: flush(module)
});
