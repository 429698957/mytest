var app = new Vue({
    el: "#app",
    data: {
        pages:15,
        pageNo:1,
        list:[],
        entity:{},
        ids:[],
        searchEntity:{},
        smsCode:'',
        name:'',
    },
    methods: {
        //注册
       /* register: function () {
            axios.post('/user/add/'+this.smsCode+'.shtml', this.entity).then(function (response) {
                console.log(response);
                if (response.data.success) {
                    window.location.href="home-index.html";
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },*/
        formSubmit:function () {
            var that=this;
            this.$validator.validate().then(
                function (result) {
                    if(result){
                        console.log(that);
                        axios.post('/user/add/'+that.smsCode+'.shtml',that.entity).then(function (response) {
                            if(response.data.success){
                                //跳转到其用户后台的首页
                                window.location.href="home-index.html";
                            }else{
                                that.$validator.errors.add(response.data.errorsList);
                                alert(response.data.message)
                            }
                        }).catch(function (error) {
                            console.log("1231312131321");
                        });
                    }
                }
            )
        },
        //点击获取验证码发短信
        createSmsCode:function () {
              axios.get('/user/sendCode.shtml?phone='+this.entity.phone).then(function (response) {
                  alert(response.data.message);
              })
        },
        getName:function () {
            axios.get('/login/name.shtml').then(function (response) {
                app.name =response.data;
            })
        }
    },
    //钩子函数 初始化了事件和
    created: function () {
      
           this.getName();

    }

})
