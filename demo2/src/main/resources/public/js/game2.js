
var canvas = document.querySelector("#canvas");
var ctx = canvas.getContext("2d");

var requestAnimationFrame = window.requestAnimationFrame || 
                            window.mozRequestAnimationFrame || 
                            window.webkitRequestAnimationFrame || 
                            window.msRequestAnimationFrame;
  
// var cancelAnimationFrame  = window.cancelAnimationFrame || 
//                             window.mozCancelAnimationFrame;

var then;

var pingTimer; // open send ping per 10s for avoid socket closed (when socket was open)
var consistencyTimer; // send data per 150ms for keep consistency as a supplement (when game start)

var data = {};  // the object send to server

// global game object
var game =  {
    start: false, 
    debug: false,
    double: false, // double user
    master: false,
    rainbow: false,
    level: 0,
    passAll: false,
    crashNum: 0,
    score: 0,
    giftRate: 5,   // gift appear probability (1 / giftRate)
    width: canvas.width,
    height: canvas.height,
    levelData: [
        [0,0,0,1,1,0,0,0,0,0,1,0,0,1,0,0,0,1,0,0,0,0,1,0,1,0,0,0,0,0,0,1],
        [1,0,0,0,0,0,0,1,0,1,0,0,0,0,1,0,0,0,1,1,1,1,0,0,0,1,0,0,0,0,1,0,1,0,0,0,0,0,0,1],
        [0,0,1,0,0,1,0,0,0,1,0,1,1,0,1,0,0,1,0,0,0,0,1,0,0,0,1,0,0,1,0,0,0,0,0,1,1,0,0,0],
        [0,0,0,1,1,0,0,0,0,0,1,1,1,1,0,0,0,1,1,1,1,1,1,0,0,0,1,0,0,1,0,0],
        [0,0,0,1,1,0,0,0,0,0,1,1,1,1,0,0,0,0,1,1,1,1,0,0,0,0,0,1,1,0,0,0],
        [1,0,1,0,1,0,1,0,0,1,0,1,0,1,0,1,1,0,1,0,1,0,1,0,0,1,0,1,0,1,0,1,1,0,1,0,1,0,1,0],
        [0,0,1,1,1,1,0,0,0,0,1,1,1,1,0,0,0,0,1,1,1,1,0,0,0,0,0,1,1,0,0,0,0,0,1,1,1,1,0,0],
        [1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,1,1,0,0,0,0,0,0,1,1,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1],
        [1,1,1,1,1,1,1,1,1,0,1,0,0,1,0,1,1,1,0,1,1,0,1,1,1,0,1,0,0,1,0,1,1,1,1,1,1,1,1,1],
        [0,0,1,1,1,1,0,0,0,1,1,1,1,1,1,0,0,0,1,1,1,1,0,0,0,0,0,1,1,0,0,0,0,0,1,0,0,1,0,0],
    ],
    reset: function(allReset) {
        // reset game data
        if (allReset === true) {
            this.level = 0;
            this.score = 0; 
            // this.double = false;
            // this.master = false;
            this.rainbow = false;
        }

        player.reset();
        ball.reset();
        gift.reset();
        brick.reset(); 
        // vue 
        app.gameData.score = this.score;
        app.gameData.level = this.level + 1;
    },
    gameStart: function(reset) {
        if (reset === true) {
            this.reset()
        }
        debug("game start")
        then = Date.now();
        this.start = true;
        gameLoop();
    },
    gameOver: function() {
        this.start = false;
        socketObj.sendGameOver();
        if (check.isSoloOrInviter()) {
            app.addRecord(true);
        }
        this.reset(true);

        if (confirm("游戏结束!重新开始?")) {
            this.gameStart();
        } else {
            app.closeAll();
        }
    },
    updateScore: function(v) {
        v = v == null ? 1 : v;   // default 1, can be minus
        this.score += v;
        app.gameData.score = this.score;
    },
    updateLevel: function(level) {
        this.level = level;
        this.reset();
        socketObj.sendLevelPass();
    },
    randomLevel: function () {
        this.level = randomInt(0, this.levelData.length - 1);
        brick.crashNum = 0;
        brick.tempNum = randomInt(1, 8);
        brick.loadData();
        debug("next level need : " + brick.tempNum);

        app.gameData.level = this.level + 1;
        socketObj.sendLevelPass();
    }

}

function debug(msg) {
    if (game.debug === true && console !== null) {
        console.log(msg);
    }
}

function gameLoop() {
    var now = Date.now();
    var delta = now - then;

    update(delta / 1000);
    render();

   if (game.start)
        requestAnimationFrame(gameLoop);

    then = now;
};

var socketObj = {
    url: "ws://" + window.location.host + "/demo2/socket",
    socket: null,
    openSocket: function(token) {
        if (!token) 
            return;
        this.socket = new WebSocket(this.url + "?token=" + token);
        var self = this;
        this.socket.onopen = function() {
            console.log("open");
            app.user.validated = true;
            pingTimer = setInterval(function() {
                var buf  = new ArrayBuffer(1);
                self.socket.send(buf);
            }, 1000 * 10);
            consistencyTimer = setInterval(function() {

                self.sendConsistency();
            }, 150);
        }
        this.socket.onerror = function() {
            alert("websocket连接错误！");
        }

        this.socket.onclose = function() {
            console.log("closed");
            clearInterval(pingTimer);
            clearInterval(consistencyTimer);
        }

        this.socket.onmessage = function(e) {
            if (e.data instanceof Blob) { // binary data
                if (e.data.size === 1) {
                    debug("pong...");
                } else if (e.data.size === 2) {
                    app.addRecord(false);
                    alert("您的滑稽队友已经离线\n这局的分数全都归您了");
                    game.reset();
                    game.double = false;
                    game.master = false;
                    game.start = false;
                    app.closeAll();
                }
                return;
            }
            
            var data = JSON.parse(e.data);

            /**
             * handle invite and confirm message
             * data struct:
             *     invite message   : {t:0, n:{name}, f:{uuid}}
             *     confirm message  : {t:1, n:{name}}
             *     consistency      : {t:2, ...}
             *     gift show        : {t:3, tk:{giftTypeKey}, x:{x}, y:{y}}
             *     gift effect      : {t:4, s: {p | p2}}
             *     gift reset       : {t:5}
             *     ball move        : {t:6}
             *     crash            : {t:8}
             *     gameOver         : {t:9}
             *     
             */
            if (data.hasOwnProperty('t')) {
                debug(e.data);
                if (data.t === 0) {   // is invite message
                    app.notificationList.push(data);
                } else if (data.t === 1){    // is confirm message
                    var name = data.n;
                    console.log("用户:" + name + "同意了您的邀请");
                    game.double = true;
                    game.master = true;
                    app.toggleCanvas();
                } else if (data.t === 2) {   // is consistency 
                    ball.x = data.bx;
                    ball.y = data.by;
                    ball.toRigth = data.tr === 1 ? true : false;
                    ball.toDown = data.td === 1 ? true : false;
                    brick.setShowBrickByIdx(data.b);
                } else if (data.t === 8) {   // crash

                } else if (data.t === 3) {   // gift show
                    gift.appear(data.x, data.y, data.tk);
                } else if (data.t === 4) {   // gift effect
                    var source = data.s === 'p' ? player2 : player; 
                    gift.effect(source);
                } else if (data.t === 5) {
                    gift.reset();
                } else if (data.t === 6) { 
                    ball.move = true;
                } else if (data.t === 7) {
                    game.start = false;
                    if (confirm("游戏结束!重新开始?")) {
                        game.gameStart(true);
                    } else {
                        app.closeAll();
                    }
                } else if (data.t === 9) {
                    if (data.PA === 1) {
                        if (!game.passAll) {
                            alert("尊敬的滑稽勇士，你已经完成了滑稽大作战\n接下来是滑稽深渊模式，去挑战吧");
                            ball.reset();
                        }
                        game.passAll = true;
                        gift.reset();
                        brick.reset(); 
                    } else {
                        game.updateLevel(data.l);
                    }
                    game.score = data.s;
                    app.gameData.score = this.score;
                }
            } else {  // key event 
                //{"x":581,kt":1,"kc":68,"bx":464,"by":158,"tr":true,"td":true}
                console.log(e.data);
                player2.x = data.x;
                if (data.kt === 1) {
                    player2.keysDown[data.kc] = true;
                } else {
                    delete player2.keysDown[data.kc];
                }

                // update ball position if this side is invitee 
                if (!game.master) {
                    ball.x = data.bx;
                    ball.y = data.by;
                    ball.toRigth = data.tr;
                    ball.toDown = data.td;
                }
            }
        }
    },
    shouldSend: function() {
        return game.double && game.master;
    },
    sendConsistency: function () {
        if (game.start && this.shouldSend()) {
            var data = {
                t: 2,
                bx: ball.x,
                by: ball.y,
                tr: ball.toRigth === true ? 1 : 0,
                td: ball.toDown === true ? 1 : 0,
                b: brick.getShowBrickIdx(),
            }
            this.socket.send(JSON.stringify(data));
        }
    },
    sendGiftShow: function(x, y, key) {
        if (this.shouldSend()) {
            this.socket.send(JSON.stringify({ t:3, tk: key, x: x, y: y,}));
        }
    },
    sendGiftEffect: function (source) {
        if (this.shouldSend()) {
            this.socket.send(JSON.stringify({ t:4, s: source === player ? 'p' : 'p2',}));
        }
    },
    sendGiftReset: function () {
        if (this.shouldSend()) {
             this.socket.send(JSON.stringify({t:5}));
        }
    },
    sendBallMove: function() {
        if (this.shouldSend()) {
            this.socket.send(JSON.stringify({t:6}));
        }
    },
    sendGameOver: function() {
        if (this.shouldSend()) {
             this.socket.send(JSON.stringify({t:7}));
        }
    },
    sendCrash: function(brickId) {
        if (this.shouldSend()) {
             this.socket.send(JSON.stringify({t:8, i:brickId}));
        }
    },
    sendLevelPass: function() {
        if (this.shouldSend()) {
            var data = {
                t: 9,
                s: game.score,   // make score consistent 
                PA: game.passAll === true ? 1 : 0,
                l: game.level,
            }
            this.socket.send(JSON.stringify(data))
        }
    },
}


var bgReady = false;
var bgImage = new Image();
bgImage.onload = function() { bgReady = true; };
bgImage.src = "images/background.png";

var playerReady = false;
var playerImg = new Image();
playerImg.onload = function() { playerReady = true; }
playerImg.src = "images/huaji2-left.png";

var player2Ready = false;
var player2Img = new Image();
player2Img.onload = function() { player2Ready = true; }
player2Img.src = "images/huaji2-rigth.png";

var ballReady = false;
var ballImg = new Image();
ballImg.onload = function() { ballReady = true; }
ballImg.src = "images/huaji.png";

var brickReady = false;
var brickImg = new Image();
brickImg.onload = function() { brickReady = true; }
brickImg.src = "images/brick.png";

var slowImg = new Image();
slowImg.src = "images/t_slow30.png";  

var speedImg = new Image();
speedImg.src = "images/t_pen30.png";

var scoreAddImg = new Image();
scoreAddImg.src = "images/t_gift30.png";

var ballspeedImg = new Image();
ballspeedImg.src = "images/t_yinxian30.png"

var rainbowImg = new Image();
rainbowImg.src = "images/t_rainbow30.png";

var scoreCutImg = new Image();
scoreCutImg.src = "images/t_tu30.png";

var ball = {
    move: false,
    speed: 380,
    width: 30,
    height: 30,
    x: 0,
    y: 535,
    toRigth: true,
    toDown: false,
    config: {
        DEFAULT_SPEED: 380,
    },
    getLeft: function() {
        return this.x;
    },
    getTop: function() {
        return this.y;
    },
    getRight: function() {
        return this.x + this.width;
    },
    getBottom: function() {
        return this.y + this.height;
    },
    getCenterX: function() {
        return this.x + this.width / 2;
    },
    getCenterY: function() {
        return this.y + this.height / 2;
    },
    reset: function() {
        this.move = false;
        this.x = player.x + (player.width / 2) - this.width / 2;
        this.y = 530;
        this.toRigth = true;
        this.toDown = false;
        this.speed = this.config.DEFAULT_SPEED;
    },
    //
    slowDown: function(reduce) {
        reduce = reduce || 200;  // default 200
        var oldSpeed = this.speed;
        this.speed -= reduce;
        var self = this;
        setTimeout(function() {
            self.speed = oldSpeed;
        }, 1000 * 5);
    },
    speedUp: function(increase, isProtect) {
        increase = increase || 200; // default 100
        var oldSpeed = this.speed;
        this.speed += increase;
        var self = this;
        setTimeout(function slow() {
            if (isProtect === true) {
                if (ball.toDown) {   // avoid ball downward with hight speed
                    game.rainbow = true;
                    debug("in protect...");
                    setTimeout(slow, 500);
                    return;
                }
            }
            game.rainbow = false;
            self.speed = oldSpeed;    
        }, 1000 * 5);
    }
}

var player = {
    speed: 450,
    width: 64,
    x: 300,
    y: 560,
    toRigth: false,
    toLeft: false,
    keysDown: {},
    config: {
        DEFAULT_SPEED: 450,
    },
    getRight: function() {
        return this.x + this.width;
    },
    getBottom: function() {
        return this.y + this.height;
    },
    reset: function() {
        player.x = game.width / 2 - player.width / 2;    
        player.y = 560;
        this.speed = this.config.DEFAULT_SPEED;
        this.toRigth = false;
        this.toLeft = false;
        this.keysDown = {};
    },
    // 
    slowDown: function(reduce) {
        reduce = reduce || 200;  // default 200
        this.speed -= reduce;
        var self = this;
        setTimeout(function() {
            self.speed = self.config.DEFAULT_SPEED;
        }, 1000 * 5);
    },
    speedUp: function(increase) {
        increase = increase || 100; // default 100
        this.speed += increase;
        var self = this;
        setTimeout(function() {
            self.speed = self.config.DEFAULT_SPEED;
        }, 1000 * 5);
    }
}

// other user
var player2 = {
    speed: 450,
    width: 64, 
    x: 300,
    y: 560,
    keysDown: {},
    config: {
        DEFAULT_SPEED: 450,
    },
    getRight: function() {
        return this.x + this.width;
    },
    getBottom: function() {
        return this.y + this.height;
    },
    slowDown: function(reduce) {
        reduce = reduce || 200;  // default 200
        this.speed -= reduce;
        var self = this;
        setTimeout(function() {
            self.speed = self.config.DEFAULT_SPEED;
        }, 1000 * 5);
    },
    speedUp: function(increase) {
        increase = increase || 100; // default 100
        this.speed += increase;
        var self = this;
        setTimeout(function() {
            self.speed = self.config.DEFAULT_SPEED;
        }, 1000 * 5);
    }
}

var brick = {
    width: 120,
    height: 35,
    brickNum: 0,
    crashNum: 0,
    tempNum: 0,   // monster mode
    data: [],
    loadData: function() {

        this.data = game.levelData[game.level].slice(0);  // copy
        this.data.forEach(function(e) { // count brickNum
            if (e === 1) {
                this.brickNum++;
            }
        }, this);
        debug("loadData!" + this.brickNum);
    },
    reset: function() {
        this.crashNum = 0;
        this.brickNum = 0;
        this.tempNum = 0;
        this.loadData();
    },
    getShowBrickIdx: function () {
        var a = [];
        for (var i = 0; i < this.data.length; i++) {
            if (this.data[i] === 1) {
                a.push(i);
            }
        }
        return a;
    },
    setShowBrickByIdx: function (a) {
        if (a.length === 0)
            return;
        var arr = new Array(40);
        for (var i = 0; i < arr.length; i++) {
            arr[i] = 0;
        }
        for (var i = 0; i < a.length; i++) {
            arr[a[i]] = 1;
        }
        this.data = arr;
    },
}

// gift
var gift = {
    show: false,
    speed: 100, 
    width: 30,
    height: 30,
    x: 400,
    y: 0,
    type: { id: 0, img: null},
    TYPE_ENUM: {
        BALL_SPEED_UP: { id: 1, img: ballspeedImg },
        // BALL_SLOW_DOWN: { id: 2, img: rainbowImg },
        PLAYER_SPEED_UP: { id: 3, img: speedImg },
        PLAYER_SLOW_DOWN: { id: 4, img: slowImg },
        SCORE_ADD: { id: 5, img: scoreAddImg },
        SCORE_CUT: { id: 6, img: scoreCutImg },
        RAIN_BOW: { id: 7, img: rainbowImg},
    },
    reset: function() {
        this.show = false;
        socketObj.sendGiftReset();
    },
    appear: function(x, y, typeKey) {
        this.show = true;
        this.x = x;
        this.y = y
        this.type = this.TYPE_ENUM[typeKey];
    },
    effect: function(source) {
        this.reset();
        switch (this.type) {
            case this.TYPE_ENUM.BALL_SPEED_UP:
                ball.speedUp();
                break;
            case this.TYPE_ENUM.BALL_SLOW_DOWN:
                ball.slowDown();
                break;
            case this.TYPE_ENUM.PLAYER_SPEED_UP:
                source.speedUp();
                break;
            case this.TYPE_ENUM.PLAYER_SLOW_DOWN:
                source.slowDown();
                break;
            case this.TYPE_ENUM.SCORE_ADD:
                game.updateScore(5);
                break;
            case this.TYPE_ENUM.SCORE_CUT:
                game.updateScore(-5);
                break;
            case this.TYPE_ENUM.RAIN_BOW:
                game.rainbow = true;
                ball.speedUp(null, true);
                player.speedUp();
                game.updateScore(10);
                break;
        }
        socketObj.sendGiftEffect(source);
        debug("gift effect!");
    },
    getRight: function() {
        return this.x + this.width;
    },
    getBottom: function() {
        return this.y + this.height;
    },
    appearRandom: function(x, y) {
        if (!this.show && (randomInt(1, game.giftRate) === 1)) {
            var keys = Object.keys(this.TYPE_ENUM);
            var key = keys[randomInt(0, keys.length - 1)];
            if (key == 'RAIN_BOW') {
                if (randomInt(1, 5) !== 1) { // 1 / 5
                    key = keys[randomInt(0, keys.length - 1)];
                }
            }
            debug("gift show :" + key)
            this.appear(x, y, key);  

            // send gitf data to other side
            socketObj.sendGiftShow(x, y, key);
        }
    }
} 


var render = function() {
    // if (bgReady) { ctx.drawImage(bgImage, 0, 0)}
    ctx.clearRect(0,0,canvas.width,canvas.height);  
    if (ballReady) { ctx.drawImage(ballImg, ball.x, ball.y); }
    if (game.double && player2Ready) { ctx.drawImage(player2Img, player2.x, player2.y)}
    if (playerReady) { ctx.drawImage(playerImg, player.x, player.y); }
    if (gift.show) {
         ctx.drawImage(gift.type.img, gift.x, gift.y);
    }
    if (brickReady) {
        initBrick();
    }
}


// shitty code !!!
var update = function (modifier) {
    if (!game.start) 
        return;

    /* player1 */
    if (65 in player.keysDown) { // left
        player.x -= Math.floor(player.speed * modifier);
        player.x = player.x > 10 ? player.x : 10;
    } else if (68 in player.keysDown) { // right
        player.x += Math.floor(player.speed * modifier);
        player.x = player.getRight() <= game.width - 10 ? player.x : game.width - 10 - player.width;
    }

    /* player2 */
    if (65 in player2.keysDown) { 
        if (player2.x > 10)
            player2.x -= player2.speed * modifier;
    } else if (68 in player2.keysDown) {
        if (player2.x + player2.width <= game.width - 10)
            player2.x += player2.speed * modifier;
    }

    // ball follow player while ball.move is false
    if (!ball.move) {
        if (check.isSoloOrInviter()) {
            ball.x = player.x + (player.width / 2) - ball.width / 2;
        } else {
            ball.x = player2.x + (player2.width / 2) - ball.width / 2;
            return;
        }
        if (87 in player.keysDown) {  // up
            ball.move = true;
            socketObj.sendBallMove();
        }
        return;
    }

    //////////////////////////
    //         gift         //
    //////////////////////////
    if (gift.show) {
        gift.y += gift.speed * modifier;
        if (check.isSoloOrInviter()) {
            var p = check.isBetweenPlayers(gift.x, gift.getRight());
            if (p != null && gift.getBottom() >= game.height - 60) {
                debug("you got gift")
                gift.effect(p);
            } else if (gift.getBottom() >= game.height - 10) {
                gift.reset();
            }
        }
    }


    //////////////////////////
    //         ball         //
    //////////////////////////
    if (ball.toDown) {
        ball.y += Math.floor(ball.speed * modifier);
        if (check.isBetweenPlayers(ball.x, ball.getRight()) && 
            ball.getBottom() >= game.height - 40) {    // rebound from player
            ball.toDown = false;
            // check monster mode
            if (check.isSoloOrInviter() && game.passAll && (brick.crashNum >= brick.tempNum)) {
                game.randomLevel();
            }
        }

        if (ball.getBottom() >= canvas.height - 10) {  
            if (check.isSoloOrInviter() && !game.rainbow) {  // touch ground, game over
                console.log("game over ....")
                game.gameOver();
                return false;
            }
            ball.toDown = false;
        }
    } else {
        ball.y -= Math.floor(ball.speed * modifier);
        if (ball.y <= 0) {
            ball.y = 0;  // avoid bug
            ball.toDown = true;
        }
    }

    if (ball.toRigth) {
        ball.x += Math.floor(ball.speed * 0.9 * modifier);
        if (ball.x + ball.width >= game.width) {
            ball.x = game.width - ball.width; // avoid bug
            ball.toRigth = false;
        }
    } else {
        ball.x -= Math.floor(ball.speed * 0.9 * modifier);
        if (ball.x <= 0) {
            ball.x = 0; // avoid bug
            ball.toRigth = true;
        }
    }

    // if (!check.isSoloOrInviter()) {
    //     return;
    // }

    //////////////////////////
    //         crash        //
    //////////////////////////
    var line = Math.floor(ball.y / (brick.height + 15)) + 1;  // index from 1
    if (line <= 5) {
        // var ballCenterY = ball.y + ball.height / 2;
        var column = Math.floor(ball.getCenterX() / (brick.width + 10)) + 1;
        var brickLeft = 10 * column + (column  - 1) * brick.width;
        var brickRight = brickLeft + brick.width;
        // var brickCenterY = (10 + brick.height) * (line - 1) + 10 + brick.height / 2;
        var brickTop = (line - 1) * (brick.height + 10);
        var birckBottom = brickTop + brick.height + 10;

        var brickId = (line - 1) * 8 + column - 1;  // 下标从0开始
        if (brick.data[brickId] === 1) {
            // 四种碰撞检测 By XiaoFeng
            /**
             *        ↓
             *     =======   
             *  →  =======  ←
             *        ↑
             */

            brick.data[brickId] = 0;
            crash(brickLeft + brick.width / 2, birckBottom);
            ball.toDown = true;
        }        

        // if (ballCenterY <= birckBottom && ballCenterY >= brickTop) {
        if (ball.x <= brickLeft && column != 1) {
            var leftBrickId = brickId - 1;
            if (brick.data[leftBrickId] === 1) {
                brick.data[leftBrickId] = 0;
                ball.toRigth = true;
                crash(brickLeft + brick.width / 2 - gift.width, birckBottom);
            }
        }
        if (ball.getRight() >= brickRight && column != 8) {
            var rightBrickId = brickId + 1;
            if (brick.data[rightBrickId] === 1) {
                brick.data[rightBrickId] = 0;
                ball.toRigth = false;
                crash(brickLeft + brick.width / 2, birckBottom);
            }
        }

        if (ball.y + ball.width + 10 >= birckBottom && line != 5) {
            var bottomBrickId = brickId + 8;
            if (brick.data[bottomBrickId] === 1) {
                brick.data[bottomBrickId] = 0;
                ball.toDown = false;
                crash(brickLeft + brick.width / 2, birckBottom);
            }
        }
    }

    if (brick.crashNum === brick.brickNum) {
        if (!game.passAll) {
            if (game.level === game.levelData.length - 1) {   // pass all level
                if (check.isSoloOrInviter()) {
                    gift.reset();
                    ball.reset();
                    game.passAll = true;
                    game.randomLevel();
                    alert("尊敬的滑稽勇士，你已经完成了滑稽大作战!\n接下来是滑稽深渊模式，去挑战吧");
                    return;
                }
            }
        }
        debug("this level done");
        if (check.isSoloOrInviter()) {
            game.updateLevel(game.level+1);
        }
        
    }
};

var crash = function(x, y) {
    brick.crashNum++;
    game.crashNum++;

    game.updateScore();

    if (!game.double || game.master) {
        gift.appearRandom(x, y);
    }
}


addEventListener("keydown", function (e) {
    // switch(e.keyCode) {
    //     case 65:
    //         player.toLeft = true;
    //         break;
    //     case 68:
    //         player.toRigth = true;
    //         break;
    // }
    if (!game.start) return;
    if (e.keyCode === 65 || e.keyCode === 68) {
        if (!(e.keyCode in player.keysDown)) {
            sendData(1, e.keyCode);   // 先发送数据
        }
    }
    player.keysDown[e.keyCode] = true;
}, false);  

addEventListener("keyup", function (e) {
    if (!game.start) return;
    if (e.keyCode === 65 || e.keyCode == 68) {
        if (e.keyCode in player.keysDown) {
            sendData(0, e.keyCode); 
        }
    }
    delete player.keysDown[e.keyCode];
}, false);

var DIRECTION_KEY = new Set([37, 39, 65, 68]);

function sendData(type, keyCode) {
    // 游戏开始而且双人模式才想websocket发送数据
    // console.log("send data" + e.keyCode)
    if (game.start && game.double) {
        data.x = player.x;
        data.kt = type;
        data.kc = keyCode;
        // 如果是主方则发送球的位置给客方
        if (game.master) {
            data.bx = Math.floor(ball.x);
            data.by = Math.floor(ball.y);
            data.tr = ball.toRigth;
            data.td = ball.toDown;
        }
        socketObj.socket.send(JSON.stringify(data));
    }

}

var initBrick = function() {
    for (var i = 0; i < brick.data.length; i++) {
        var line = Math.floor(i / 8);
        var column = i % 8;
        if (brick.data[i] === 1) {
            ctx.drawImage(brickImg, 10 + ((120 + 10) * column), 10 + ((35 + 10) * line));
        }
    }
}



var check = {
    isBetweenPlayers: function(left, right) {
        if (right >= player.x && left <= player.getRight()) {
            return player;
        }
        if (game.double) {
            if (right >= player2.x && left <= player2.getRight()) {
                return player2;
            }
        }
        return null;
    },
    isSoloOrInviter: function() {
        return !game.double || game.master;
    }
}

/* return int range of [min, max] */
function randomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}
