var app = new Vue({

    el: "#app",
    data: {
        seckillId: 0,
        timeString: '', //绑定倒计时的时间 显示数据
        count: 0,
        messageInfo:'',
    },
    methods: {

        //方法点击立即抢购的时候调用
        submitOrder: function () {
            axios.get('/seckillOrder/submitOrder.shtml?id=' + this.seckillId).then(function (response) {
                    if (response.data.success) {
                        //提示正在排队中
                       app.messageInfo = response.data.message;

                    } else {
                        if (response.data.message == '403') {
                            //要去登录
                            alert('滚去登录')
                            var url = window.location.href;//获取当前浏览器url地址
                            window.location.href = "http://localhost:9111/page/login.shtml?url=" + url
                        } else {
                            alert(response.data.message)
                        }
                    }
                }
            )
        },
        //

        convertTimeString: function (alltime) {
            var allsecond = Math.floor(alltime / 1000);//毫秒数转成 秒数。
            var days = Math.floor(allsecond / (60 * 60 * 24));//天数
            var hours = Math.floor((allsecond - days * 60 * 60 * 24) / (60 * 60));//小数数
            var minutes = Math.floor((allsecond - days * 60 * 60 * 24 - hours * 60 * 60) / 60);//分钟数
            var seconds = allsecond - days * 60 * 60 * 24 - hours * 60 * 60 - minutes * 60; //秒数
            if (days > 0 && days < 10) {
                days = "0" + days + "天";
            }
            if (days > 10) {
                days = days + "天";
            }
            if (hours < 10) {
                hours = "0" + hours;
            }
            if (minutes < 10) {
                minutes = "0" + minutes;
            }
            if (seconds < 10) {
                seconds = "0" + seconds;
            }
            return days + hours + ":" + minutes + ":" + seconds;
        },
        cacalate: function (alltime) {
            var clock = window.setInterval(function () {
                alltime = alltime - 1000;
                app.timeString = app.convertTimeString(alltime);
                if (alltime <= 0) {
                    window.clearInterval(clock);
                }
            }, 1000)
        },
        getGoodsById: function () {
            axios.get('/seckillGoods/getGoodsById.shtml?id=' + this.seckillId).then(function (response) {

                app.cacalate(response.data.time);//参数是毫秒值
                app.count = response.data.count;//剩余库存
            })
        },
        //查询订单的状态 当点击立即抢购之后执行。
        queryStatus:function () {
            let count =0;
            //三秒钟执行一次
            let queryorder = window.setInterval(function () {
                count+=3;
                axios.get('/seckillOrder/queryOrderStatus.shtml').then(
                    function (response) {
                        if(response.data.success){
                            //跳转到支付页面
                            window.clearInterval(queryorder);
                            alert("跳转到支付页面");
                            window.location.href="pay/pay.html";

                        }else{
                            if(response.data.message=='403'){
                                //要登录
                            }else{
                                //不需要登录需要提示
                                app.messageInfo=response.data.message+"....."+count;
                            }
                        }
                    }
                )
            },3000)

        },

    },
    created: function () {
        //拿url里的ID值
        var urlParam = this.getUrlParam();
        this.seckillId = urlParam.id
        this.getGoodsById();

    },


});
