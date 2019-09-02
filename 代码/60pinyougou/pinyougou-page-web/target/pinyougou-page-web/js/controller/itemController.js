var app = new Vue({

    el: "#app",
    data: {
        num: 1,
        specificationItems: JSON.parse(JSON.stringify(skuList[0].spec)),//用于存储当前点击到的规格
        sku: skuList[0]

    },
    methods: {
        addNum: function (num) {
            this.num += num;
            if (this.num <= 0) {
                this.num = 1;
            }
        },
        selectSpecifcation: function (name, value) {
            //用于点击的时候调用 改变变量的值
            this.$set(this.specificationItems, name, value);
            this.search();
            // this.selectSpecifcation[name]=value;
        },
        //判断循环到的选项 是否在当前的变量中存在 如果是 返回true 否则返回false
        isSelected: function (name, value) {
            if (this.specificationItems[name] == value) {
                return true;

            }
            return false;
        },
        search: function () {
            //循环遍历sku的列表数组，判断当前的变量的值是否在数组中存在，如果存在，将对应的数组的元素赋值给sku
            for (var i = 0; i < skuList.length; i++) {

                //{"id":10561673,"title":" 16G","price":123,spec:{"网络":"移动3G","机身内存":"16G"}},
                var obj = skuList[i];
                if (JSON.stringify(this.specificationItems) == JSON.stringify(obj.spec)) {
                    this.sku = obj;
                    break;
                }

            }
        },
        addGoodsToCart: function () {
            axios.get('http://localhost:9107/cart/addGoodsToCartList.shtml?itemId=' + this.sku.id + '&num=' + this.num,{withCredentials:true}).then(function (response) {

                if (response.data.success) {
                    window.location.href="http://localhost:9107/cart.html";
                }else {
                    alert("88")
                }

                }
            )
        }
    },
    created: function () {

    }


});
