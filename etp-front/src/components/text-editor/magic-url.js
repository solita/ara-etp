// Workaround for quill-magic-url ESM import issues
// Import the module and re-export the default

import MagicUrlModule from 'quill-magic-url';

// quill-magic-url exports its main class as 'default' property
const MagicUrl = MagicUrlModule.default || MagicUrlModule;

export default MagicUrl;
