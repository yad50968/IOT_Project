var express = require('express');
var MariasqlDB = require('mariasql')
var dateTime = require('node-datetime');
var sha3 = require('js-sha3').sha3_512;
var cryptoXor = require('crypto-xor');

var gsu = '2000'
var spu = '1000'

var mariaclient = new MariasqlDB({
    host:'localhost',
    port:3306,
    user:'root',
    password:'root',
    db:'iot',
    charset:'utf8'
});

var router = express.Router()

router.post('/register', function(req, res) {
    var username = decrypt(req.body.username, gsu)
    var Ku = decrypt(req.body.Ku, gsu)
    mariaclient.query("INSERT INTO Server_User SET username = ?, Ku = ?", [username, Ku])
    res.send("ok");
})

router.post('/updateToken', function(req, res) {
    var username = decrypt(req.body.username, gsu)
    var token = decrypt(req.body.token, gsu)
    mariaclient.query("UPDATE Server_User SET token = ? WHERE username = ?", [token, username])
    res.send("ok");
})

router.post('/insertdata', function(req, res) {
    var dt = dateTime.create().format('Y-m-d H:M:S');
    mariaclient.query("INSERT INTO health SET heartbeat = ?, time = ?", [parseFloat(req.body.heartbeat), dt]);
    res.send("ok");
});

router.post('/gethistorydata', function(req, res) {

    var cdata = decrypt(req.body.getdata, spu)
    var username = cdata.split("_")[0]
    var DBToken = ""
    var Ku = ""
    mariaclient.query("SELECT token FROM Server_User WHERE username = ?", [username], function(err, rows) {
        if (err) throw err;
        else {
            DBToken = rows[0].token;

            if(cdata.split("_")[1] == DBToken) {

                var o = {}

                o["1"] = [];
                o["2"] = [];
                o["3"] = [];
                o["4"] = [];

                mariaclient.query("SELECT TimeStamp, Value, DataType FROM Server_sensor",[], function(err, rows) {
                    if (err) throw err;
                    else {
                        var c = 0;
                        rows.forEach(function(item) {
                            switch(item.DataType) {
                                case "1":
                                    o["1"].push(item);
                                    break;
                                case "2":
                                    o["2"].push(item);
                                    break;
                                case "3":
                                    o["3"].push(item);
                                    break;
                                case "4":
                                    o["4"].push(item);
                                    break;
                                default:
                                console.log("ff")
                                    break;
                            }
                            c++;
                            if(c == rows.length) {
                                res.json({"data": encrypt(JSON.stringify(o), spu)});
                            }
                        })
                    }
                });
            }
        }
   });
});


router.post('/gatewayhistorydata', function(req, res) {

    var data = decrypt(req.body.data, gsu)
    var jo = JSON.parse(data)
    var ja = jo["data"]

    ja.forEach(function(item) {
        console.log(item)
        var DataType = item["DataType"]
        var Value = item["Value"]
        var name = item["name"]
        var reportid = item["reportid"]
        var DeviceId = item["DeviceId"]
        var TimeStamp = item["TimeStamp"]

        mariaclient.query("INSERT INTO Server_sensor SET  `Value`=?, DataType=?, name=?, reportid=?, DeviceId=?, TimeStamp=?", [Value, DataType, name, reportid, DeviceId, TimeStamp], function(err, rows) {
            if (err) {
            }else {
            }
        });
    });


    res.json(data)
});



function decrypt(input, key) {
    return xor(input, key);
}

function encrypt(input, key) {
    return xor(input, key);
}

function xor(input, key) {
    var output = [];

    for (var i = 0; i < input.length; i++) {
        var charCode = input.charCodeAt(i) ^ key[i % key.length].charCodeAt(0);
        output.push(String.fromCharCode(charCode));
    }
    return output.join("");
}


module.exports = router;
