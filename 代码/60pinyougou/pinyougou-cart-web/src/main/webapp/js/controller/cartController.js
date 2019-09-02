var app = new Vue({

    el: "#app",
    data: {
        cartList: [],
        totalMoney: 0,
        totalNum: 0,
        abc: '',
        addressList: [],
        address: {},
        order: {paymentType: '1'}

    },
    methods: {
        findCardList: function () {
            axios.get('/cart/findCartList.shtml').then(function (response) {
                    app.cartList = response.data;
                    app.totalMoney = 0
                    app.totalNum = 0


                    for (var i = 0; i < response.data.length; i++) {
                        var obj = response.data[i]//cart
                        for (var n = 0; n < obj.orderItemList.length; n++) {
                            var objx = obj.orderItemList[n]//orderItem对象
                            app.totalMoney += objx.totalFee;
                            app.totalNum += objx.num;

                        }

                    }

                }
            )
        },
        //向已有的购物车中添加商品
        addGoodsToCartList: function (itemId, num) {
            axios.get('/cart/addGoodsToCartList.shtml?itemId=' + itemId + '&num=' + num).then(function (response) {
                    if (response.data.success) {
                        //刷新
                        app.findCardList();
                    }

                }
            )
        },
        hasUser: function () {
            axios.get('/cart/hasUser.shtml').then(function (response) {

                if ('anonymousUser' == response.data) {
                    app.abc = ''
                } else {
                    app.abc = response.data;
                }

            })
        },
        findAddressList: function () {
            axios.get('/address/findAddressListByUserId.shtml').then(function (response) {
                    app.addressList = response.data

                    for (var i = 0; i < app.addressList.length; i++) {
                        var isDefault = app.addressList[i].isDefault;
                        if (isDefault == 1) {
                            app.address = app.addressList[i]
                            break;
                        }

                    }
                }
            )
        },
        selectAddress: function (address) {
            this.address = address
        },
        isSelected: function (address) {
            //判断是否打钩
            if (this.address == address) {
                return true;
            }
            return false;
        },
        selectType: function (type) {
            this.order.paymentType = type;
        },
        //当点击提交订单的时候调用
        submitOrder: function () {
            //先获取地址的信息赋值给变量order
            this.order.receiverAreaName=this.address.address;
            this.order.receiverMobile=this.address.mobile;
            this.order.receiver=this.address.contact;
            axios.post('/order/submitOrder.shtml', this.order).then(function (response) {

                    if (response.data.success) {
                        window.location.href = "pay.html";
                    }
                }
            )
        }

    },
    created: function () {
        this.findCardList();
        this.hasUser();
        var href = window.location.href;//获取当前浏览器的url地址
        if(href.indexOf("getOrderInfo.html")!=-1){
            this.findAddressList();

        }

    }


});
