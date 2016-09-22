/**
 * Created by armin on 22.09.16.
 */
var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);

io.on('connect', function (socket) {
    console.log("connected");
});

io.on('disconnect', function (socket) {
    console.log("disconnected");
});

app.post('/:axis/:degree', function (req, res) {
    console.log(req.params);
    io.emit('move', {
        Name: "robot",
        RobInMove: true,
        Runtime: 0,
        Timestamp: new Date().getTime(),
        ArrivalTime: new Date().getTime(),
        AxisAct: {
            A1: Math.random(),
            A2: Math.random(),
            A3: Math.random(),
            A4: Math.random(),
            A5: Math.random(),
            A6: Math.random()
    }});
    res.send("ok");
});

http.listen(3000, function () {
    console.log("listening on 3000");
});