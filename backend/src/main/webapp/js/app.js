(function() {
    'use strict';

    angular.module('MyID', [])
        .controller('MyIDController', MyIDController)
        .filter("lastToken", function () {
            return function lastTokenFilter(input) {
                if (input) {
                    var array = input.split('.');
                    return array[array.length-1];
                } else {
                    return input;
                }
            };
        })
        .filter("nodeType2Class", function () {
            return function nodeType2ClassFilter(input) {
                if (input === 'GenesisNode') {
                    return 'danger';
                } else if (input === 'RootNode') {
                    return 'warning';
                } else {
                    return 'info';
                }
            };
        });


    MyIDController.$inject = ['$scope', '$http'];
    function MyIDController($scope, $http) {
        $scope.allNodes = [];
        $http.get("/_ah/api/nodeApi/v1/nodes")
            .then(function(response) {
                $scope.allNodes = response.data.items;
            });
    }
})();