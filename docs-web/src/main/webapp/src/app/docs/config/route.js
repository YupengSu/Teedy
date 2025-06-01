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