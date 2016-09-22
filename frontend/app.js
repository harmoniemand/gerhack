
angular.module('robotApp', [
    'btford.socket-io',
    'ngMaterial'
]);

angular.element(document).ready(function () {
    angular.bootstrap(document, ['robotApp']);
});