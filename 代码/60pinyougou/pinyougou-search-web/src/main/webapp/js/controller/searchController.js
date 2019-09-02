var app = new Vue({

    el: "#app",
    data: {
        preDott:false,//返回前面是否有省略号的标记
        nextDott:false,//返回后面是否有省略号的标记
        searchEntity: {},//作为条件查询的对象
        resultMap: {brandList:[]},//返回的结果对象
        searchMap: {'keyword': '', 'category': '', 'brand': '', spec: {}, 'price': '', 'pageNo': 1, 'pageSize': 40,'sortField':'','sortType':''},
        pageLabels: []//存储页面的变量

    },
    methods: {
        //根据搜索的条件 执行查询 返回结果 resultmap
        search: function () {
            axios.post("/itemSearch/search.shtml", this.searchMap).then(function (response) {
                    app.resultMap = response.data;
                    app.buildPageLabel();
                }
            )

        },
        //添加搜索项
        addSearchItem: function (key, value) {
            if (key == 'category' || key == 'brand' || key == 'price') {

                this.searchMap[key] = value

            } else {

                this.searchMap.spec[key] = value;
            }
            //发送请求 执行搜索
            this.search();
        },
        //移除掉搜索项
        removeSearchItem: function (key) {
            //移除变量里的值
            if (key == 'category' || key == 'brand' || key == 'price') {
                this.searchMap[key] = '';
            } else {
                delete this.searchMap.spec[key]
            }
            //重新发送请求查询
            this.search();
        },
        //目的是构建分页的标签数据，跟总页数有关系 每次搜索的时候都要调用
        buildPageLabel: function () {
            this.pageLabels = [];

            //循环遍历总页数，构建成{1,2,3,4,5,6}
            var firstPage = 1;
            var lastPage = this.resultMap.totalPages;//结束页码
            if (this.resultMap.totalPages > 5) {
                //如果 当前页小于等于3 显示前五页
                if (this.searchMap.pageNo <= 3) {
                    firstPage = 1;
                    lastPage = 5;
                    this.preDott=false
                    this.nextDott=true

                } else if (this.searchMap.pageNo >= (this.resultMap.totalPages - 2)) {
                    firstPage = this.resultMap.totalPages - 4;
                    lastPage = this.resultMap.totalPages;
                    this.preDott=true
                    this.nextDott=false
                }else{
                    firstPage = this.searchMap.pageNo - 2;
                    lastPage = this.searchMap.pageNo + 2;
                    this.preDott=true
                    this.nextDott=true
                }
            } else {

           //啥也不干
            }

            for (var i = firstPage; i <= lastPage; i++) {
                this.pageLabels.push(i)

            }
        },
        queryByPage:function (page) {
            var number=parseInt(page);
            if (number > this.resultMap.totalPages) {
                number=this.resultMap.totalPages;
            }
            if (number<1){
                number=1;
            }
            //1.改变当前页码的值
            this.searchMap.pageNo=number;
            //2.发送请求执行查询
            this.search();
        },
        clear:function () {
            this.searchMap={'keyword': this.searchMap.keyword, 'category': '', 'brand': '', spec: {}, 'price': '', 'pageNo': 1, 'pageSize': 40,'sortField':'','sortType':''}

        },
        doSort:function (sortField,sortType) {
            //1.改变两个变量的值
           this.searchMap.sortField=sortField;
           this.searchMap.sortType=sortType;

            //2.执行搜索

            this.search()
        },

        isKeywordsIsBrand:function () {
                //循环遍历品牌列表，判断 关键字是否是品牌就好 如果是返回true 反之
            for (var i = 0;i<this.resultMap.brandList.length;i++){
                if (this.searchMap.keyword.indexOf(this.resultMap.brandList[i].text)!=-1){
                    this.searchMap.brand=this.resultMap.brandList[i].text;
                    return true;
                }
                
                
                if (this.searchMap.keyword==this.resultMap.brandList[i].text){
                    return true;
                }

            }

             return false;
        }



    },
    //判断 搜索的关键字是否就是品牌 如果是 返回true 否则返回false
    created: function () {
          //1.获取url的参数值
            var jsonObj = this.getUrlParam();
        //2.赋值给keyword
        this.searchMap.keyword = decodeURIComponent(jsonObj.keyword);
        //3.执行搜索
        this.search();

    }


});
