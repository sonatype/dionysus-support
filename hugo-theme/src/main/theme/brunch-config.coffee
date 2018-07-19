module.exports =
  config:
    paths:
      public: 'static'

    files:
      javascripts:
        joinTo: 'theme.js'

      stylesheets:
        joinTo:
          'theme.css': /^app/

    plugins:
      # disable brunch auto-reloading, hugo-reloading will be used instead
      autoReload:
        enabled: false

      babel:
        presets: ['env']

      sass:
        options:
          includePaths: [
            'node_modules/bootstrap/scss/',
            'node_modules/@fortawesome/fontawesome-free/scss'
          ]
          precision: 8

      copycat:
        'fonts': [
          'node_modules/@fortawesome/fontawesome-free/webfonts'
        ]

# FIXME: this also does not work to solve the problem with hugo not picking up changes
#      fingerprint:
#        targets: [
#          'theme.js',
#          'theme.css'
#        ]
#        srcBasePath: 'static/theme.'
#        destBasePath: 'static/'
#        autoClearOldFiles: true
#        alwaysRun: true
#        autoReplaceAndHash: true
#        hashLength: 10
#        manifest: 'data/theme/assets.json'
#        manifestGenerationForce: true

# HACK: some problems with brunch generation and hugo picking up changes, so force a change externally
      afterBrunch: [
        "sleep 1; sh ./force-update.sh"
      ]

    modules:
      autoRequire:
        'theme.js': [
          'theme'
        ]

    npm:
      globals:
        $: 'jquery'
        jQuery: 'jquery'
