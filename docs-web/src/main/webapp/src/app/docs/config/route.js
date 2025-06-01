.state('settings.registration', {
  url: '/registration',
  views: {
    'settings': {
      templateUrl: 'partial/docs/settings.registration.html',
      controller: 'SettingsRegistration'
    }
  },
  resolve: {
    init: ['$rootScope', function($rootScope) {
      $rootScope.pageTitle = 'settings.registration.title';
    }]
  }
})

.state('register', {
  url: '/register',
  templateUrl: 'partial/docs/register.html',
  controller: 'Register',
  resolve: {
    init: ['$rootScope', function($rootScope) {
      $rootScope.pageTitle = 'register.title';
    }]
  }
})

.state('settings.ldap', {
  url: '/ldap',
  views: {
    'settings': {
      templateUrl: 'partial/docs/settings.ldap.html',
      controller: 'SettingsLdap'
    }
  }
})

.state('settings.monitoring', {
  url: '/monitoring',
  views: {
    'settings': {
      templateUrl: 'partial/docs/settings.monitoring.html',
      controller: 'SettingsMonitoring'
    }
  }
})

.state('document', {
  url: '/document',
  templateUrl: 'partial/docs/document.html',
  controller: 'Document',
  resolve: {
    init: ['$rootScope', function($rootScope) {
      $rootScope.pageTitle = 'document.title';
    }]
  }
}) 