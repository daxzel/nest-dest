function getSocketApiUrl() {
    var loc = window.location, socketUri;
    if (loc.protocol === "https:") {
        socketUri = "wss:";
    } else {
        socketUri = "ws:";
    }
    socketUri += "//" + loc.host;
    socketUri += loc.pathname + "api/websocket'";
    return socketUri
}

angular.module('destApp', ['ngWebSocket'])
    .controller('DestController', ['$scope', 'utilities', function ($scope, utilities) {
        var dest = this;
        $scope.utilities = utilities;

        dest.updateConfig = function () {
            $.get("/api/updateSettings", {
                heatingPrice: $scope.heatingPrice,
                coolingPrice: $scope.coolingPrice
            });
        }
    }]).factory('utilities', function ($websocket) {
    // var dataStream = $websocket('ws://localhost:9000/api/websocket');
    var dataStream = $websocket(getSocketApiUrl());
    var collection = [];

    dataStream.onMessage(function (message) {
        // collection.push(JSON.parse(message.data));
        collection.push(message.data);
        alert(message.data);
    });

    return {
        collection: collection,
        get: function () {
            dataStream.send(JSON.stringify({action: 'get'}));
        }
    };
})