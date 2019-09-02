var app = new Vue({

    el: "#app",
    data: {
          username:''
    },
    methods: {
          getinfo:function () {
                axios.get("/login/userinfo.shtml").then(
                    function (response) {
                        app.username=response.data;
                    }
                )
          }
    },
    created: function () {
           this.getinfo()
    }


});
