const R = require('ramda');

const configModuleRulesLens = R.lensPath(['module', 'rules']);

const ruleLens = (config, testString) => R.compose(
  configModuleRulesLens,
  R.compose(
    R.lensIndex,
    R.findIndex(R.compose(R.test(R.__, testString), R.prop('test'))),
    R.view(configModuleRulesLens)
  )(config)
);

module.exports = async ({ config, mode }) => {
  config.module.rules.push({
    test: /\.css$/,
    loaders: [{
      loader: 'postcss-loader',
      options: {
        sourceMap: true,
        config: {
          path: './.storybook/'
        }
      }
    }]
  });

  const svelteLens = ruleLens(config, '.svelte');

  return R.over(svelteLens, R.mergeLeft({
    options: { 
      preprocess: require('svelte-preprocess')({
        postcss: {
          sourceMap: true,
          path: './.storybook/'
        }
      })
    }
  }), config);
}