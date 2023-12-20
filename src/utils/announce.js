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

const flush = module => () => flashMessageStore.flush(module);

export const announcementsForModule = module => ({
  announceSuccess: announceSuccess(module),
  announceError: announceError(module),
  clearAnnouncements: flush(module)
});
