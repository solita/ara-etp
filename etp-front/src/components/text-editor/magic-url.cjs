// When using es modules, quill-magic-url imports did not work correctly
// for some reason. As a workaround, import it in common-js file
// and export its default export again here

const MagicUrl = require('quill-magic-url');

module.exports = MagicUrl.default;
