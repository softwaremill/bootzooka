// Libraries
EnvJasmine.loadGlobal(EnvJasmine.libDir + "jquery-1.8.2-min.js");
EnvJasmine.loadGlobal(EnvJasmine.libDir + "angular-1.2.6/angular.js");
EnvJasmine.loadGlobal(EnvJasmine.libDir + "angular-1.2.6/angular-route.js");
EnvJasmine.loadGlobal(EnvJasmine.libDir + "angular-1.2.6/angular-resource.js");
EnvJasmine.loadGlobal(EnvJasmine.libDir + "angular-1.2.6/angular-sanitize.js");
EnvJasmine.loadGlobal(EnvJasmine.libDir + "angular-1.2.6/angular-cookies.js");
EnvJasmine.loadGlobal(EnvJasmine.libDir + "bootstrap-2.2.2.js");


// Testing libraries
EnvJasmine.loadGlobal(EnvJasmine.testDir + "../lib/require/require-2.0.6.js");
EnvJasmine.loadGlobal(EnvJasmine.testDir + "require.conf.js");
EnvJasmine.loadGlobal(EnvJasmine.testDir + "../lib/angular-1.2.6/angular-mocks.js");

// Application
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "app.js");
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "services/flashService.js");
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "effects/effects.js")


EnvJasmine.loadGlobal(EnvJasmine.rootDir + "services/profileService.js");
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "controllers/profileCtrl.js")

EnvJasmine.loadGlobal(EnvJasmine.rootDir + "controllers/loginCtrl.js");
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "controllers/passwordRecoveryCtrl.js");
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "controllers/registerCtrl.js");
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "controllers/userSessionCtrl.js");

EnvJasmine.loadGlobal(EnvJasmine.rootDir + "services/passwordRecoveryService.js");
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "services/registerService.js");
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "services/userSessionService.js");

EnvJasmine.loadGlobal(EnvJasmine.rootDir + "directives/bsBlur.js");
EnvJasmine.loadGlobal(EnvJasmine.rootDir + "directives/bsRepeatPassword.js");
