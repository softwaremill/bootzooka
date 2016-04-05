var webpack = require('webpack');

var config = {
  context: __dirname + '/app',
  entry: './index.js',
  output: {
    path: __dirname + '/dist',
    filename: 'bundle.js'
  },

  plugins: [],

  module: {
    loaders: [
      {
        test: /\.js$/,
        exclude: /(node_modules)/,
        loader: 'ng-annotate!babel'
      },
      {test: /.html$/, loader: 'raw', exclude: /(node_modules)/},
      {test: /.css$/, loader: 'style!css'},
      {test: /.styl$/, loader: 'style!css!stylus', exclude: /(node_modules)/},
      {test: /\.(png|jpg|jpeg|gif|svg|woff|woff2|ttf|eot)$/, loader: 'file'}
    ]
  }
};

if (process.env.NODE_ENV === 'production') {
  config.output.path = __dirname + '/dist';
  config.plugins.push(new webpack.optimize.UglifyJsPlugin());
}

module.exports = config;