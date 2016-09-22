
angular.module('robotApp')

    .directive("robot", function (socketService) {
        return {
            restrict: 'E',
            scope: true,
            templateUrl: "./partials/robot.html",
            link: function (scope) {

                scope.tasks = [];
                scope.currentTask = null;
                scope.robot = {
                    name: null,
                    inMove: false
                };

                scope.axis = [
                    { id: 1, velocity: 0 },
                    { id: 2, velocity: 0 },
                    { id: 3, velocity: 0 },
                    { id: 4, velocity: 0 },
                    { id: 5, velocity: 0 },
                    { id: 6, velocity: 0 }
                ];

                socketService.on('move', function (msg) {
                    console.log('message: ' + JSON.stringify(msg));

                    if (scope.robot && scope.robot.name === null) { scope.robot.name = msg.Name; }
                    if (scope.robot && scope.robot.inMove === null) { scope.robot.inMove = msg.RobInMove; }

                    var task = {
                        receivedAt: new Date(),
                        axis: [
                            { id: 1, velocity: Math.floor(msg.AxisAct.A1 * 1000) / 1000 },
                            { id: 2, velocity: Math.floor(msg.AxisAct.A2 * 1000) / 1000 },
                            { id: 3, velocity: Math.floor(msg.AxisAct.A3 * 1000) / 1000 },
                            { id: 4, velocity: Math.floor(msg.AxisAct.A4 * 1000) / 1000 },
                            { id: 5, velocity: Math.floor(msg.AxisAct.A5 * 1000) / 1000 },
                            { id: 6, velocity: Math.floor(msg.AxisAct.A6 * 1000) / 1000 }
                        ]
                    };

                    if (scope.currentTask === null) {
                        scope.currentTask = task;
                    } else {
                        scope.tasks.push(task);
                    }
                });
            }
        };
    });