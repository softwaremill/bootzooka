'use strict';

var webpack = require('webpack');

module.exports = config => {
  config.set({
    files: [
      'app/vendor.js',
      'app/index.js',
      'test/tests.webpack.js'
    ],
    preprocessors: {
      'app/vendor.js': ['webpack'],
      'app/index.js': ['webpack'],
      'test/tests.webpack.js': ['webpack', 'sourcemap']
    },
    webpack: {
      devtool: 'inline-source-map',
      plugins: [
          new webpack.ProvidePlugin({ //https://webpack.github.io/docs/shimming-modules.html
            $: "jquery",
            jQuery: "jquery",
            "window.jQuery": "jquery"
        })
      ],
      module: {
        loaders: [
          {test: /\.js$/, loader: 'babel-loader', exclude: /(node_modules)/},
          {test: /.html$/, loader: 'raw', exclude: /(node_modules)/},
          {test: /.css$/, loader: 'style!css'},
          {test: /\.(png|jpg|jpeg|gif|svg|woff|woff2|ttf|eot)$/, loader: 'file'}
        ]
      }
    },
    frameworks: ['jasmine'],
    exclude: [],
    port: 7070,
    logLevel: config.LOG_INFO,
    autoWatch: false,
    singleRun: true,
    browsers: ['PhantomJS']
  });
};
