var app = new Vue({

    el: "#app",
    data: {
        totalMoney: 0,
        out_trade_no: '',

    },
    methods: {
        createNative: function () {
            axios.get('/pay/createNative.shtml').then(function (response) {
                    if (response.data) {

                        //有数据
                        app.totalMoney = response.data.total_fee / 100;
                        app.out_trade_no = response.data.out_trade_no;


                        var qrious = new QRious({
                            element: document.getElementById("qrious"),
                            level: "H",
                            size: 250,
                            value: response.data.code_url
                        });




                            //发送请求x`
                            app.queryPayStatus(response.data.out_trade_no)



                    } else {
                        //没有数据
                    }

                }
            )
        },
        //根据支付的订单号发送请求获取支付的状态 Result
        queryPayStatus: function (out_trade_no) {
            axios.get('/pay/queryStatus.shtml?out_trade_no=' + out_trade_no).then(function (response) {


                    if (response.data.success) {
                        //z支付成功
                        window.location.href="paysuccess.html?money="+app.totalMoney;

                    } else {
                        //支付失败或者超时
                        if (response.data.message=='支付超时') {

                            axios.get('/')



                        }else {
                            window.location.href="payfail.html"
                        }
                    }
                }
            )
        }


    },
    created: function () {
        if(window.location.href.indexOf("pay.html")!=-1){
            this.createNative();
        }else {
            let urlParamObject = this.getUrlParam();
            if(urlParamObject.money)
                this.totalMoney=urlParamObject.money;
        }
    }


});
