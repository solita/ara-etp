'use strict';

exports.defineTags = function(dictionary) {
  dictionary.defineTag('sig', {
    mustHaveValue: true,
    onTagged: function(doclet, tag) {
      doclet.type = 'Function';
      doclet.signature = ` :: ${tag.text}`;
      doclet.returns = '';
    }
  });
};
