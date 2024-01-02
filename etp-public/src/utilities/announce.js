const assertiveAnnouncerId = 'assertive-announcer';
const politeAnnouncerId = 'polite-announcer';

const announce = announcerId => msg => {
  const announcer = document.getElementById(announcerId);
  const previous = announcer.children[0];
  if (previous !== undefined) {
    announcer.removeChild(previous);
  }
  const p = document.createElement('p');
  p.textContent = msg;
  announcer.appendChild(p);
};

export const announceAssertively = announce(assertiveAnnouncerId);
export const announcePolitely = announce(politeAnnouncerId);
