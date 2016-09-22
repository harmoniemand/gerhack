
angular.module('robotApp', [
    'btford.socket-io'
]);

angular.element(document).ready(function () {
    angular.bootstrap(document, ['robotApp']);
});