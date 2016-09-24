
angular.module('robotApp')

    .directive("robots", function () {
        return {
            restrict: 'E',
            scope: true,
            templateUrl: "./partials/robots.html",
            link: function (scope) {
                scope.robots = [];

                scope.$on("move", function (event, msg) {
                    if (_.findWhere(scope.robots, { name: msg.Name }) == null) {
                        var robot = {
                            name: msg.Name,
                            inMove: false,
                            axis: [
                                { id: 1, angle: 0 },
                                { id: 2, angle: 0 },
                                { id: 3, angle: 0 },
                                { id: 4, angle: 0 },
                                { id: 5, angle: 0 },
                                { id: 6, angle: 0 }
                            ],
                            currentTask: null,
                            tasks: [{
                                receivedAt: new Date(),
                                runtime: msg.Runtime,
                                axis: [
                                    { id: 1, velocity: Math.floor(msg.AxisAct.A1 * 1000) / 1000 },
                                    { id: 2, velocity: Math.floor(msg.AxisAct.A2 * 1000) / 1000 },
                                    { id: 3, velocity: Math.floor(msg.AxisAct.A3 * 1000) / 1000 },
                                    { id: 4, velocity: Math.floor(msg.AxisAct.A4 * 1000) / 1000 },
                                    { id: 5, velocity: Math.floor(msg.AxisAct.A5 * 1000) / 1000 },
                                    { id: 6, velocity: Math.floor(msg.AxisAct.A6 * 1000) / 1000 }
                                ]
                            }]
                        };

                        scope.robots.push(robot);
                    }
                });
            }
        };
    });