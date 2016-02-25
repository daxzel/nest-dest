angular.module('destApp', [])
    .controller('DestController', ['$scope', function($scope) {
        var dest = this;

        dest.updateConfig = function () {
            $.get( "/api/updateSettings", {
                heatingPrice : $scope.heatingPrice,
                coolingPrice : $scope.coolingPrice
            } );
        }
    }]);