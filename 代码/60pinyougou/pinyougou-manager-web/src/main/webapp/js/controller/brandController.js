var app = new Vue({

    el: "#app",
    data: {
        list: [],
        pages: 15,
        pageNo: 10,
        entiry: {},
        ids:[],
        searchEntity:{}
    },
    methods: {

        findAll: function () {

            axios.get("/brand/findAll.shtml").then(function (response) {
                app.list = response.data


            }).catch(function (error) {


            })
        },
        searchList: function (cutPage) {
            axios.post("/brand/findPage.shtml?pageNo=" + cutPage).then(function (response) {
                app.pageNo = response.data.pageNum;
                app.pages = response.data.pages;
                app.list = response.data.list;

            }).catch(function (error) {
                alert(error)
            })
        },
        add: function () {
            axios.post("/brand/add.shtml", app.entiry).then(function (response) {
                if (response.data.success) {
                    app.searchList(1);
                } else {
                    alert(response.data.message)
                }
            })
        },
        findOne: function (id) {
            axios.get("/brand/findOne/" + id + ".shtml").then(function (response) {

                app.entiry = response.data;
            })
        },
        update: function () {
            axios.post("/brand/update.shtml", app.entiry).then(function (response) {
                if (response.data.success) {
                    app.searchList(1);
                } else {
                    alert(response.data.message)
                }
            })
        },
        save: function () {
            if (this.entiry.id == null || this.entiry.id == undefined) {
                this.add();
            } else {
                this.update();
            }
        },
        dele:function () {

            axios.post("/brand/delete.shtml",this.ids).then(function (response) {
                if (response.data.success) {
                    app.ids=[];
                    app.searchList(1);
                } else {
                    alert(response.data.message)
                }
            })
        },
        searchListPage:function (curPage) {

            axios.post("/brand/search.shtml?pageNo="+curPage,this.searchEntity).then(function (response) {
                app.pageNo = response.data.pageNum;
                app.pages = response.data.pages;
                app.list = response.data.list;

            }).catch(function (error) {
                alert(error)
            })
        }
    }
    ,
    created: function () {
        //this.findAll();
        this.searchList(1);
    }


});
