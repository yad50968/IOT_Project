# Iot_Project_Gateway

### This project is for MOST project: IOT Trustworthy Architecture Design and Implementation <br>
Implement and do some changes with the authentication protocal in [1]. <br>
The user have to register first, then login to pass the auth process. <br>
If pass, user can get the token and use it to get data from Gateway.


### PRE SETTING

* Install python , node first!
* If you use Windows need npm install --global --production windows-build-tools

1. Setting gateway/app/src/main/java/liutzuyuan/gateway/Setting.java

```
    // Shared secret key with client
    static final String spu = "1000";
    static final String op = "CDC202D5123E20F62B6D676AC72CB318";

    // Known string
    static final String amf = "B9B9";
    static final String Xs = "gateway";

    static final String serverURL = "http://ip:3000";
    
    // Shared secret key with server
    static final String gsu = "2000"

    // SQL ip:port
    static final String sqlip = "ip:port";

    // SQL DB name
    static final String sqlname = "iot";

    // SQL account
    static final String sqlaccount = "";

    // SQL password
    static final String sqlpassword = "";

    // Gateway's listen port
    static final int gatewaylistenport = 8003;

    // Gateway send data to server's interval
    static final int sendDataToServerTime = 1000*10*60;
```

2. Setting client/app/src/main/java/com/cwsu/iot/Setting.java
```sh

    // Shared secret key with gateway
    static final String spu = "1000";
    static final String op = "CDC202D5123E20F62B6D676AC72CB318";

    // Known string
    static final String Xs = "gateway";

    static final String gatewayURL = "http://localhost:8003";

    static final String serverURL = "http://ip:3000";
    
```

3. Setting server/routes/index.js 
```sh
    // mysql connection setting
    var mariaclient = new MariasqlDB({
        host:'localhost',
        port:3306,
        user:'',
        password:'',
        db:'',
        charset:'utf8'
    });
    
    
    // Shared secret key with gateway
    var gsu = '2000'
```
4. Install node package
```sh
    cd server/
    sudo npm install
```

5. Construct mysql schema
```sh
    use SQL_Schema.sql
```

### RUN
1. start server
2. start gateway
3. start app

# Reference
1. [Java implementation of 3GPP™ TS 35.206 Milenage algorithm](https://github.com/brake/milenage)
2. [3G/4G protocol spec. in 3GPP](http://www.3gpp.org)
3. [Authentication and Authorization Applications in 4G Networks](http://spi.unob.cz/papers/2015/2015-04.pdf)
