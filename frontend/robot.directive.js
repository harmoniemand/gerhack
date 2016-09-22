
angular.module('robotApp')

.directive("robot", function(socketService) {
    return {
            restrict: 'E',
            scope: true,
            templateUrl: "./partials/robot.html",
            link: function (scope) {

            }
    };
});