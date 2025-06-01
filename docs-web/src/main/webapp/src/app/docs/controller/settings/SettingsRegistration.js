'use strict';

/**
 * Settings registration requests page controller.
 */
angular.module('docs').controller('SettingsRegistration', function($scope, Restangular, $translate, $dialog) {
  /**
   * Load pending registration requests.
   */
  $scope.loadPendingRequests = function() {
    Restangular.one('user/registration').get().then(function(data) {
      $scope.pendingRequests = data.requests;
    });
  };

  /**
   * Approve a registration request.
   */
  $scope.approveRequest = function(request) {
    var title = $translate.instant('settings.registration.approve_confirm_title');
    var msg = $translate.instant('settings.registration.approve_confirm_message', { username: request.username });
    var btns = [
      { result: 'cancel', label: $translate.instant('cancel') },
      { result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary' }
    ];

    $dialog.messageBox(title, msg, btns).result.then(function(btn) {
      if (btn === 'ok') {
        Restangular.one('user/registration', request.id).post('', {
          action: 'approve'
        }).then(function() {
          $scope.loadPendingRequests();
        });
      }
    });
  };

  /**
   * Reject a registration request.
   */
  $scope.rejectRequest = function(request) {
    var title = $translate.instant('settings.registration.reject_confirm_title');
    var msg = $translate.instant('settings.registration.reject_confirm_message', { username: request.username });
    var btns = [
      { result: 'cancel', label: $translate.instant('cancel') },
      { result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-danger' }
    ];

    $dialog.messageBox(title, msg, btns).result.then(function(btn) {
      if (btn === 'ok') {
        Restangular.one('user/registration', request.id).post('', {
          action: 'reject'
        }).then(function() {
          $scope.loadPendingRequests();
        });
      }
    });
  };

  // Load pending requests
  $scope.loadPendingRequests();
}); 