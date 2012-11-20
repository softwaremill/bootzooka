basePath = '../';

files = [
  JASMINE,
  JASMINE_ADAPTER,
  '../src/main/webapp/assets/js/angular-1.1.0.js',
  '../src/main/webapp/assets/js/angular-*.js',
  'lib/angular/angular-mocks.js',
  '../src/main/webapp/app/*.js',
  'unit/**/*.js'
];

autoWatch = true;

browsers = ['Chrome'];

junitReporter = {
  outputFile: 'test_out/unit.xml',
  suite: 'unit'
};
