var baseUrl = window.location.origin + '/demo5';


/* 遮罩组件 */
Vue.component('my-mask', {
    template: `
        <div class="mask" @click="click"></div>
    `,
    data: function() {
        return {}
    },
    methods: {
        click: function() {
            this.$emit('click');
        }
    },
});

/* 登录组件 */
Vue.component('login', {
    template: `
    <div id="login" class="login-wrap center">
        <div class="header text-center">
            <div class="signin touch" @click="toggleMenu('signin')">登录</div>
            <div class="signup touch" @click="toggleMenu('signup')">注册</div>
            <div class="tabline" :class="{rigth: !showSignin}"></div>
        </div>
        <div class="login-body">
            <div class="signin-body" :class="{active : showSignin}">
                <div class="input" :class="{active : signin.usernameFocus}">
                    <label for="username" class="label" :class="{focus : signin.usernameFocus}">请输入您的邮箱</label>
                    <input @focus="toggleSigninFocus('username')" @blur="toggleSigninFocus('username')" v-model="signin.username" id="username" type="text" autocomplete="off">
                </div>
                <div class="input" :class="{active : signin.passwordFocus}">
                    <label for="password" class="label" :class="{focus : signin.passwordFocus}">请输入您的密码</label>
                    <input @focus="toggleSigninFocus('password')" @blur="toggleSigninFocus('password')" v-model="signin.password" id="password" type="password" autocomplete="off">
                </div>
                <div class="input">
                    <input type="submit" class="submit touch" @click.stop="validateSignin" value="登录" :disabled="signin.validated == false">
                </div>
            </div>
            <div v-show="showSignup" class="signup-body">
                <div class="input" :class="{active : signup.emailFocus}">
                    <label for="email" class="label" :class="{focus : signup.emailFocus}">请输入您的邮箱</label>
                    <input @focus="toggleSignupFocus()" @blur="toggleSignupFocus()" v-model="signup.email" id="email" type="text" autocomplete="off">
                </div>
                <div class="input" id="captcha2">
                    <p id="wait2" v-show="!geetest.init">正在加载验证码......</p>
                </div>
                <div class="input">
                    <input type="submit" class="submit touch" @click.stop="sendEmail" value="发送验证邮件" :disabled="signup.validated == false">
                </div>
            </div>
            <div v-show="showSendEmailSuccess">
                <div class="text-center">请登录邮箱完成注册</div>
            </div>
        </div>
        <div class="footer">
            <div class="wechat-login"></div>
            <div class="weibo-login"></div>
            <div class="qq-login"></div>
        </div>
    </div>
    `,
    data: function() {
        return {
            geetest: {
                init: false,
                captchaObj: null,
                gt: '',
                challenge: '',
                offline: false,
            },
            signin: {
                usernameFocus: false,
                passwordFocus: false,
                username: '',
                password: '',
                validated: false,
            },
            signup: {
                emailFocus: false,
                email: '',
                validated: false,
                reSendText: '',
            },
            showSignin: true,   
            showSignup: true, 
            showSendEmailSuccess: false,
        }
    },
    watch: {
        signin: {
            handler: function() {
                // this.validateSignin();
                if (this.signin.username != '' && this.signin.password != '') {
                    this.signin.validated = true;
                } else {
                    this.signin.validated = false;
                }
            },
            deep: true,
        },
    },
    mounted: function() {
        this.initGeetest("#captcha2");
        var self = this;
        setTimeout(function() {
        self.$http.post(baseUrl + '/api/user/test').then(function(res) {
            console.log(res)
        }, function(res) {
            console.log(res)
        })
            
        }, 1000)
    },
    methods: {
        initGeetest: function(id) {
            this.$http.get("geetest?t=" + (+ new Date())).then(function(res) {
                console.log(res);
                this.geetest.gt = res.data.gt;
                this.geetest.challenge = res.data.challenge;
                this.geetest.offline = !res.data.success;
                var self = this;
                initGeetest({
                    gt: res.data.gt,
                    challenge: res.data.challenge,
                    new_captcha: res.data.new_captcha, // 用于宕机时表示是新验证码的宕机
                    offline: !res.data.success, // 表示用户后台检测极验服务器是否宕机，一般不需要关注
                    product: "float", // 产品形式，包括：float，popup
                    width: "100%"
                }, function(captchaObj) {
                    captchaObj.appendTo(id);
                    self.geetest.init = true;
                    self.geetest.captchaObj = captchaObj;
                    captchaObj.onSuccess(function() {
                        self.signup.validated = true;
                    })
                });
            }, function (res) {
               
            });
        },
        toggleMenu: function (type) {
            if ((type === 'signin' && document.querySelector(".tabline").classList.contains('rigth'))
                || (type === 'signup' && !document.querySelector(".tabline").classList.contains('rigth'))) {
                this.showSignin = !this.showSignin;
            }
        },
        toggleSigninFocus: function(type) {
            console.log("in toggleSigninFocus")
            if (type === 'username') {
                this.signin.usernameFocus = this.signin.username === '' ? !this.signin.usernameFocus : this.signin.usernameFocus;
            } else if (type === 'password') {
                this.signin.passwordFocus = this.signin.password === '' ? !this.signin.passwordFocus : this.signin.passwordFocus;
            }
        },
        toggleSignupFocus: function() {
            console.log("in toggleSignupFocus")
            this.signup.emailFocus = this.signup.email === '' ? !this.signup.emailFocus : this.signup.emailFocus;
        },
        validateSignin: function() {
            if (this.signin.validated) {
                //登录
                this.$http.post(
                    'api/user/signin',
                    {
                        username: this.signin.username,
                        password: this.signin.password,
                    },
                    { emulateJSON: true }
                ).then(function(res) {
                    console.log(res)
                    var data = res.data;
                    if (data.code === 0) {
                        this.$emit('login-success');
                    } else {
                        alert(data.message);
                    }
                }, function(res) {
                    console.log(res);
                    alert('服务器开小差了，请稍后重试');
                }); 
            }
            // console.log(this.signin.validated);
        },
        sendEmail: function() {
            var rex = /^\w+([-+.]\w+)*@\w+\.((com|cn)|(com.cn))$/;
            var email = this.signup.email;
            if (email === '') {
                console.log("邮箱不能为空");
            } else if (!rex.test(email)) {
                console.log("邮箱格式不正确")
            } else {

                var result = this.geetest.captchaObj.getValidate();
                if (result) {
                    this.$http.post(
                        "api/user/send/email/validate_code",
                        { 
                            email: this.signup.email,
                            challenge: result.geetest_challenge,
                            validate: result.geetest_validate,
                            seccode: result.geetest_seccode,
                        },
                        { emulateJSON: true }
                    ).then(function(res) {
                        console.log(res);
                        if (res.data.code === 0) {
                            this.showSignup = false,
                            this.showSendEmailSuccess = true;
                            // this.signup.validated = false;
                            // this.signup.reSendText = 2;
                            // this.initGeetest('#captcha3');
                            // this.reSendTimer();                            
                        }
                    }, function(res) {
                        alert('服务器开小差了，请稍后重试');
                    });
                } else {
                    console.log("极验校验失败")
                }
            }
        },
        // reSendTimer: function() {
        //     if (this.signup.reSendText == 0) {
        //         this.signup.reSendText = "重新发送";
        //         this.signup.validated = true;
        //         // this.
        //     } else {
        //         this.signup.reSendText = this.signup.reSendText - 1;
        //         setTimeout(this.reSendTimer, 1000);
        //     }
        // }

    }
});
