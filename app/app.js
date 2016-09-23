
angular.module('robotApp', [
    'btford.socket-io',
    'ngMaterial',
    'ngAnimate'
]).run(function ($rootScope, socketFactory) {
    $rootScope.socket = socketFactory();

    $rootScope.socket.on('move', function (msg) {
        console.log('message: ' + JSON.stringify(msg));

        $rootScope.$broadcast("move", msg);
    });
});

angular.element(document).ready(function () {
    angular.bootstrap(document, ['robotApp']);
});