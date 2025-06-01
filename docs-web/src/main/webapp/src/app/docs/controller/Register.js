'use strict';

/**
 * Register page controller.
 */
angular.module('docs').controller('Register', function($scope, $state, Restangular, $translate, $dialog) {
  $scope.user = {};

  /**
   * Register a new user.
   */
  $scope.register = function() {
    Restangular.one('user/registration').post('', {
      username: $scope.user.username,
      password: $scope.user.password,
      email: $scope.user.email
    }).then(function() {
      var title = $translate.instant('register.success_title');
      var msg = $translate.instant('register.success_message');
      var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
      $dialog.messageBox(title, msg, btns).result.then(function() {
        $state.go('login');
      });
    }, function(data) {
      if (data.data.type === 'AlreadyExistingUsername') {
        var title = $translate.instant('register.error_title');
        var msg = $translate.instant('register.error_username_exists');
        var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
        $dialog.messageBox(title, msg, btns);
      }
    });
  };
}); 