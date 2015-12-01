/* globals module require __dirname */

var path = require('path');
var webpack = require('webpack');
const autoprefixer = require('autoprefixer');
const ExtractTextPlugin = require('extract-text-webpack-plugin');

var entry = ['./index.js'];

if (process.env.NODE_ENV === 'development') {
  entry = entry.concat([
    'webpack-dev-server/client?http://localhost:3000',
    'webpack/hot/only-dev-server',
  ]);
}

module.exports = {
  devtool: 'source-map',
  entry: entry,
  output: {
    path: path.join(__dirname, '../cloud-config-server/src/main/resources/static/dist'),
    filename: 'bundle.js',
    publicPath: '/dist/',
  },
  plugins: [
    new ExtractTextPlugin('bundle.css', { allChunks: true }),
    new webpack.HotModuleReplacementPlugin(),
    new webpack.NoErrorsPlugin(),
  ],
  resolve: {
    extensions: ['', '.js', '.jsx', '.scss', '.css'],
    alias: {
      "semantic-ui-components": path.join(__dirname, "./node_modules/semantic-ui-css/components")
    }
  },
  postcss: [autoprefixer],
  module: {
    loaders: [{
      test: /(\.js|\.jsx)$/,
      loaders: ['react-hot', 'babel'],
      exclude: /node_modules/
    }, {
      test: /(\.scss|\.css)$/,
      loader: ExtractTextPlugin.extract('style', 'css!postcss!sass')
    }, {
      test: /\.(woff|woff2|ttf|svg)$/,
      loader: 'url?limit=100000',
      exclude: /node_modules/
    }, {
        test: /\.(eot|png)$/,
        loader: 'file',
        exclude: /node_modules/
    }
  ]},
  devServer: {
    proxy: {
      "/api/*": {
        target: 'http://localhost:8001'
      },
    },
  }
};
