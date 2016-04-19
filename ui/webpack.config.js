var webpack = require('webpack');
var ExtractTextPlugin = require('extract-text-webpack-plugin');
var autoprefixer = require('autoprefixer');
const path = require('path');

var config = {
  context: path.join(__dirname, 'app'),
  entry: {
    app: './index.js',
    vendor: './vendor.js'
  },
  output: {
    path: __dirname + '/app',
    filename: 'bootzooka.bundle.js'
  },
  plugins: [
    new ExtractTextPlugin('styles.css'),
    new webpack.optimize.CommonsChunkPlugin('vendor', 'vendor.bundle.js'),
    new webpack.ProvidePlugin({ //https://webpack.github.io/docs/shimming-modules.html
      $: "jquery",
      jQuery: "jquery",
      "window.jQuery": "jquery"
    })
  ],
  module: {
    loaders: [
      {
        test: /\.js$/,
        exclude: /(node_modules)/,
        loader: 'ng-annotate!babel'
      },
      {test: /.html$/, loader: 'raw', exclude: /(node_modules)/},
      {test: /.css$/, loader: 'style!css'},
      {test: /\.(png|jpg|jpeg|gif|svg|woff|woff2|ttf|eot)$/, loader: 'file'}
    ]
  },
  postcss: function () {
    return [autoprefixer({browsers: ['last 2 versions']})];
  },
  devServer: {
    proxy: {
      '/api/*': {
        target: 'http://localhost:8080'
      }
    }
  }
};

if (process.env.NODE_ENV === 'production') {
  config.output.path = __dirname + '/dist/webapp';
  config.plugins.push(new webpack.optimize.UglifyJsPlugin());
} else {
  config.devtool = 'eval'
}

module.exports = config;
