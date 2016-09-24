
angular.module('robotApp')

    .directive("robot", function ($interval) {
        return {
            restrict: 'E',
            scope: {
                data: '='
            },
            templateUrl: "./partials/robot.html",
            link: function (scope, element, attrs) {

                scope.robot = scope.data;

                $interval(function () {
                    if (scope.robot.currentTask == null) {
                        if (scope.robot.tasks.length > 0) {
                            scope.robot.currentTask = scope.robot.tasks[0];
                            scope.robot.tasks = scope.robot.tasks.reverse();
                            scope.robot.tasks.pop();
                            scope.robot.tasks = scope.robot.tasks.reverse();
                        }

                        return;
                    }

                    for (let i = 0; i < scope.robot.currentTask.axis.length; i++) {
                        scope.robot.axis[i].angle += scope.robot.currentTask.axis[i].velocity;
                        scope.robot.axis[i].angle = scope.robot.axis[i].angle % 100;

                        if (scope.robot.axis[i].angle < 0) {
                            scope.robot.axis[i].angle = 100 + scope.robot.axis[i].angle;
                        }
                    }
                

                    scope.robot.currentTask.runtime -= 1;

                    if (scope.robot.currentTask.runtime < 1) {
                        if (scope.robot.tasks.length == 0) {
                            scope.robot.currentTask = null;
                            return;
                        }

                        scope.robot.currentTask = scope.robot.tasks[0];
                        scope.robot.tasks = scope.robot.tasks.reverse();
                        scope.robot.tasks.pop();
                        scope.robot.tasks = scope.robot.tasks.reverse();
                    }
                }, 500);

                scope.$on('move', function (event, msg) {
                    console.log('message: ' + JSON.stringify(msg));

                    if (msg.Name != scope.robot.name) {
                        return; // not the droids youre looking for
                    }

                    var task = {
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
                    };

                    if (scope.robot.currentTask === null) {
                        scope.robot.currentTask = task;
                    } else {
                        scope.robot.tasks.push(task);
                    }
                });
            }
        };
    });