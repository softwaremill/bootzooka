import 'angular-mocks/angular-mocks';

var testsContext = require.context(".", true, /-spec\.js$/);
testsContext.keys().forEach(testsContext);
