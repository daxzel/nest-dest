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
    .controller('DestController', ['$scope', 'utilities', function ($scope, socket) {
        var dest = this;
        $scope.socket = socket;

        dest.updateConfig = function () {
            $.get("/api/updateSettings", {
                heatingPrice: $scope.heatingPrice,
                coolingPrice: $scope.coolingPrice
            });
        }
    }]).factory('utilities', function ($websocket) {
    var dataStream = $websocket(getSocketApiUrl());

    var socket = {
        cost: 0,
        get: function () {
            dataStream.send(JSON.stringify({action: 'get'}));
        }
    }
    dataStream.onMessage(function (message) {
        // collection.push(JSON.parse(message.data));
        socket.cost = message.data
    });
    return socket;
})