'use strict';

/**
 * Settings user activity controller.
 */
angular.module('docs').controller('SettingsUserActivity', function ($scope, Restangular, $translate, $timeout) {

  $scope.loading = true;
  $scope.error = null;

  // 处理文件名显示，移除UUID部分
  $scope.formatMessage = function(message) {
    if (!message) return '';
    
    // 如果消息中包含UUID格式的文件名（例如：704b4415-bca8-41cb-8982-5af857155ee1InfiJanice_NeurIPS_2025.pdf）
    var uuidPattern = /[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/i;
    if (uuidPattern.test(message)) {
      // 移除UUID部分
      return message.replace(uuidPattern, '');
    }
    
    return message;
  };

  // Calculate date range for the last week
  var endDate = new Date();
  var startDate = new Date();
  startDate.setDate(endDate.getDate() - 35);

  // Set endDate time to the end of the day
  endDate.setHours(23, 59, 59, 999);

  // Format dates to YYYY-MM-DD
  var formatDate = function(date) {
    var d = new Date(date),
        month = '' + (d.getMonth() + 1),
        day = '' + d.getDate(),
        year = d.getFullYear();

    if (month.length < 2) 
        month = '0' + month;
    if (day.length < 2) 
        day = '0' + day;

    return [year, month, day].join('-');
  };

  var startDateStr = formatDate(startDate);
  var endDateStr = formatDate(endDate);

  // Fetch audit logs for the last week
  Restangular.one('auditlog').get({
    startDate: startDateStr,
    endDate: endDateStr
  }).then(function (data) {
    $scope.loading = false;
    console.log("Audit log data received:", data);
    var logs = data.logs;
    
    console.log("Logs array:", logs);
    if (!logs || logs.length === 0) {
      $scope.error = $translate.instant('settings.user_activity.no_data');
      return;
    }
    
    // Group logs by user
    $scope.userActivitiesGrouped = {};
    logs.forEach(function(log) {
        if (!$scope.userActivitiesGrouped[log.username]) {
            $scope.userActivitiesGrouped[log.username] = [];
        }
        $scope.userActivitiesGrouped[log.username].push(log);
    });

  }, function (error) {
    $scope.loading = false;
    $scope.error = $translate.instant('settings.user_activity.error');
    console.error("Error fetching audit logs:", error);
  });

}); 