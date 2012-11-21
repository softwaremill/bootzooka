require.config({
    baseUrl: EnvJasmine.rootDir,
    paths: {
        specs:      EnvJasmine.specsDir,

        // Libraries
        // FIXME: we don't want to depend on jQuery necessarily, but for
        // now the sbt plugin requires it
        jquery:     '../assets/jquery-1.8.2-min'

    }
});
