/**
 * Created by armin on 22.09.16.
 */
var express = require('express');
var app = express();
var http = require('http').Server(app);
var io = require('socket.io')(http);

var newCommand;
var record = false;
var commands = {};
var currentState = {};
var limits = [0,-1,180,300,100,300,-1];
var maxSpeed = [0,5,4,5,1,4,4];
var com = "";
var start;

io.on('connection', function (socket) {
    console.log("connected");
    socket.on('disconnect', function (socket) {
        console.log("disconnected");
    });
});


app.use('/', express.static('../frontend'));

app.post('/run/:robot/:command', function (req, res) {
    if (!currentState.hasOwnProperty(req.params.robot)) {
        currentState[req.params.robot] = [0,0,0,0,0,0,0];
    }
    var coms = commands[req.params.command].commands;
    var start = commands[req.params.command].start;
    var deltas = [0, start[1] - currentState[req.params.robot][1], start[2] - currentState[req.params.robot][2], start[3] - currentState[req.params.robot][3], start[4] - currentState[req.params.robot][4], start[5] - currentState[req.params.robot][5], start[6] - currentState[req.params.robot][6]];
    for (var i = 1; i < deltas.length; i++) {
        if (deltas[i] !== 0) {
            var object = {
                Name: req.params.robot,
                RobInMove: true,
                Runtime: 0,
                Timestamp: new Date().getTime(),
                ArrivalTime: new Date().getTime(),
                AxisAct: {
                    A1: 0,
                    A2: 0,
                    A3: 0,
                    A4: 0,
                    A5: 0,
                    A6: 0
                }
            };
            var min = 1000, minSpeed = 1000;
            for (var j = 1; j <= maxSpeed[i]; j++) {
                if (Math.abs(deltas[i] % j) <= min) {
                    min = deltas[i] % j;
                    minSpeed = j;
                }
            }
            if (deltas[i] < 0) {
                minSpeed = -minSpeed;
            }
            object.AxisAct["A" + i] = minSpeed;
            object.Runtime = Math.abs(deltas[i] / minSpeed);
            object.ArrivalTime = object.Timestamp + object.Runtime;
            io.emit('move', object);
        }
    }
    var tests = [];
    for (var i = 0; i < coms.length; i++) {
        var com = coms[i];
        com.Name = req.params.robot;
        tests.push(JSON.parse(JSON.stringify(com)));
        io.emit('move', com);
    }
    res.json(tests);
});

app.post('/:robot/:axis/:degree', function (req, res) {
    if (!currentState.hasOwnProperty(req.params.robot)) {
        currentState[req.params.robot] = [0,0,0,0,0,0,0];
    }
    var cs = currentState[req.params.robot].slice(0);
    var robot = req.params.robot;
    var axis = req.params.axis;
    var degree = parseFloat(req.params.degree);
    var object = {
        Name: robot,
        RobInMove: true,
        Runtime: 0,
        Timestamp: new Date().getTime(),
        ArrivalTime: new Date().getTime(),
        AxisAct: {
            A1: 0,
            A2: 0,
            A3: 0,
            A4: 0,
            A5: 0,
            A6: 0
        }
    };
    if (Math.abs(currentState[req.params.robot][axis] + degree) > limits[axis] && limits[axis] !== -1) {
        degree = currentState[req.params.robot][axis] - degree;
        currentState[req.params.robot][axis] = limits[currentState[req.params.robot].axis];
    } else {
        currentState[req.params.robot][axis] += parseFloat(degree);
        currentState[req.params.robot][axis] = currentState[req.params.robot][axis] % 360;
    }
    var speed = maxSpeed[axis];
    var minSpeed = 1000;
    var min = 1000;
    for (var i = 1; i <= speed; i++) {
        if (Math.abs(degree % i) <= min) {
            min = Math.abs(degree % i);
            minSpeed = i;
        }
    }
    if (degree < 0) {
        minSpeed = -minSpeed;
    }
    object.AxisAct["A" + axis] = minSpeed;
    object.ArrivalTime = Math.floor(object.Timestamp + degree / minSpeed);
    object.Runtime = object.ArrivalTime - object.Timestamp;

    if (record) {
        newCommand.push(object);
        if (start == null) {
            start = cs;
        }
    }
    io.emit('move', object);
    res.send("ok");
});

app.post('/record/:command', function (req, res) {
    newCommand = [];
    record = true;
    com = req.params.command;
    start = null;
    res.send("ok");
});

app.post('/end', function (req, res) {
    if (!record) {
        res.send("ok");
        return;
    }
    for (var i = newCommand.length - 1; i > 0; i--) {
        var last = newCommand[i];
        var before = newCommand[i - 1];
        if (last.AxisAct.A1 !== 0 && before.AxisAct.A1 !== 0) {
            var dist = last.AxisAct.A1 * (last.ArrivalTime - last.Timestamp) + before.AxisAct.A1 * (before.ArrivalTime - before.Timestamp);
            var min = 1000, minSpeed = 1000;
            for (var j = 1; j <= maxSpeed[1]; j++) {
                if (dist % j <= min) {
                    min = dist % j;
                    minSpeed = j;
                }
            }
            before.ArrivalTime = before.Timestamp + (dist / minSpeed);
            before.Runtime = before.ArrivalTime - before.Timestamp;
            newCommand.splice(i, 1);
        } else if (last.AxisAct.A2 !== 0 && before.AxisAct.A2 !== 0) {
            var dist = last.AxisAct.A2 * (last.ArrivalTime - last.Timestamp) + before.AxisAct.A2 * (before.ArrivalTime - before.Timestamp);
            var min = 1000, minSpeed = 1000;
            for (var j = 1; j <= maxSpeed[2]; j++) {
                if (dist % j <= min) {
                    min = dist % j;
                    minSpeed = j;
                }
            }
            before.ArrivalTime = before.Timestamp + (dist / minSpeed);
            before.Runtime = before.ArrivalTime - before.Timestamp;
            newCommand.splice(i, 1);
        } else if (last.AxisAct.A3 !== 0 && before.AxisAct.A3 !== 0) {
            var dist = last.AxisAct.A3 * (last.ArrivalTime - last.Timestamp) + before.AxisAct.A3 * (before.ArrivalTime - before.Timestamp);
            var min = 1000, minSpeed = 1000;
            for (var j = 1; j <= maxSpeed[3]; j++) {
                if (dist % j <= min) {
                    min = dist % j;
                    minSpeed = j;
                }
            }
            before.ArrivalTime = before.Timestamp + (dist / minSpeed);
            before.Runtime = before.ArrivalTime - before.Timestamp;
            newCommand.splice(i, 1);
        } else if (last.AxisAct.A4 !== 0 && before.AxisAct.A4 !== 0) {
            var dist = last.AxisAct.A4 * (last.ArrivalTime - last.Timestamp) + before.AxisAct.A4 * (before.ArrivalTime - before.Timestamp);
            var min = 1000, minSpeed = 1000;
            for (var j = 1; j <= maxSpeed[4]; j++) {
                if (dist % j <= min) {
                    min = dist % j;
                    minSpeed = j;
                }
            }
            before.ArrivalTime = before.Timestamp + (dist / minSpeed);
            before.Runtime = before.ArrivalTime - before.Timestamp;
            newCommand.splice(i, 1);
        } else if (last.AxisAct.A5 !== 0 && before.AxisAct.A5 !== 0) {
            var dist = last.AxisAct.A5 * (last.ArrivalTime - last.Timestamp) + before.AxisAct.A5 * (before.ArrivalTime - before.Timestamp);
            var min = 1000, minSpeed = 1000;
            for (var j = 1; j <= maxSpeed[5]; j++) {
                if (dist % j <= min) {
                    min = dist % j;
                    minSpeed = j;
                }
            }
            before.ArrivalTime = before.Timestamp + (dist / minSpeed);
            before.Runtime = before.ArrivalTime - before.Timestamp;
            newCommand.splice(i, 1);
        } else if (last.AxisAct.A6 !== 0 && before.AxisAct.A6 !== 0) {
            var dist = last.AxisAct.A6 * (last.ArrivalTime - last.Timestamp) + before.AxisAct.A6 * (before.ArrivalTime - before.Timestamp);
            var min = 1000, minSpeed = 1000;
            for (var j = 1; j <= maxSpeed[6]; j++) {
                if (dist % j <= min) {
                    min = dist % j;
                    minSpeed = j;
                }
            }
            before.ArrivalTime = before.Timestamp + (dist / minSpeed);
            before.Runtime = before.ArrivalTime - before.Timestamp;
            newCommand.splice(i, 1);
        }
    }
    commands[com] = {commands: newCommand, start: start};
    com = "";
    record = false;
    res.send("ok");
});

app.get('/commands', function (req, res) {
    res.json(commands);
});

app.get('/states', function (req, res) {
    res.json(currentState);
});

http.listen(3000, function () {
    console.log("listening on 3000");
});