
angular.module('remoteApp')

    .directive("remote", function ($http) {
        return {
            restrict: 'E',
            scope: true,
            templateUrl: "./partials/remote.html",
            link: function (scope) {
                scope.a1 = 0;
                scope.a2 = 0;
                scope.a3 = 0;
                scope.a4 = 0;
                scope.a5 = 0;
                scope.a6 = 0;

                scope.name = null;

                scope.$watch("a1", function() { scope.send(1, scope.a1); });
                scope.$watch("a2", function() { scope.send(2, scope.a2); });
                scope.$watch("a3", function() { scope.send(3, scope.a3); });
                scope.$watch("a4", function() { scope.send(4, scope.a4); });
                scope.$watch("a5", function() { scope.send(5, scope.a5); });
                scope.$watch("a6", function() { scope.send(6, scope.a6); });

                scope.send = function(axe, degree) {
                    $http({
                        method: "POST",
                        url: "/" + scope.name +"/" + axe + "/" + degree
                    });
                };
            }
        };
    });