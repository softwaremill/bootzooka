var webpack = require('webpack');
var ExtractTextPlugin = require("extract-text-webpack-plugin");
var autoprefixer = require('autoprefixer');
const path = require('path');

var config = {
  context: path.join(__dirname, 'app'),
  entry: {
    app: './app.js',
    vendor: './vendor.js'  
  },
  output: {
    path: __dirname + '/dist',
    filename: 'bootzooka.bundle.js'
  },
  plugins: [
      new ExtractTextPlugin('styles.css'),
      new webpack.optimize.CommonsChunkPlugin('vendor', 'vendor.bundle.js')
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
      {
          test: /.styl$/, 
          loader: ExtractTextPlugin.extract('style-loader', 
          'css-loader!stylus!postcss-loader'), exclude: /(node_modules)/
      },
      {test: /\.(png|jpg|jpeg|gif|svg|woff|woff2|ttf|eot)$/, loader: 'file'}
    ]
  },
  postcss: function () {
    return [autoprefixer({browsers: ['last 2 versions']})];
  }
};

if (process.env.NODE_ENV === 'production') {
  config.plugins.push(new webpack.optimize.UglifyJsPlugin());
} else {
  config.devtool = 'eval'
}

module.exports = config;