var app = new Vue({
    el: "#app",
    data: {
        pages: 15,
        pageNo: 1,
        list: [],
        entity: {
            goods: {},
            goodsDesc: {itemImages: [], customAttributeItems: [], specificationItems: []},
            itemList: []
        },
        image_entity: {color: '', url: ''},
        ids: [],
        searchEntity: {},
        imageurl: '',
        ItemCat1List: [],
        ItemCat2List: [],
        ItemCat3List: [],
        //customAttributeItems:[],
        brandTextList: [],
        specList: [],//存储规格的列表

    },
    methods: {
        searchList: function (curPage) {
            axios.post('/goods/search.shtml?pageNo=' + curPage, this.searchEntity).then(function (response) {
                //获取数据
                app.list = response.data.list;

                //当前页
                app.pageNo = curPage;
                //总页数
                app.pages = response.data.pages;
            });
        },
        //查询所有品牌列表
        findAll: function () {
            console.log(app);
            axios.get('/goods/findAll.shtml').then(function (response) {
                console.log(response);
                //注意：this 在axios中就不再是 vue实例了。
                app.list = response.data;

            }).catch(function (error) {

            })
        },
        findPage: function () {
            var that = this;
            axios.get('/goods/findPage.shtml', {
                params: {
                    pageNo: this.pageNo
                }
            }).then(function (response) {
                console.log(app);
                //注意：this 在axios中就不再是 vue实例了。
                app.list = response.data.list;
                app.pageNo = curPage;
                //总页数
                app.pages = response.data.pages;
            }).catch(function (error) {

            })
        },
        //该方法只要不在生命周期的
        add: function () {
            this.entity.goodsDesc.introduction = editor.html()
            axios.post('/goods/add.shtml', this.entity).then(function (response) {

                if (response.data.success) {
                    alert("添加成功")
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        update: function () {
            this.entity.goodsDesc.introduction = editor.html()
            axios.post('/goods/update.shtml', this.entity).then(function (response) {
                console.log(response);
                if (response.data.success) {
                    app.searchList(1);
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        save: function () {
            if (this.entity.goods.id != null) {
                this.update();
            } else {
                this.add();
            }

            window.location.href="goods.html"
        },
        findOne: function (id) {
            axios.get('/goods/findOne/' + id + '.shtml').then(function (response) {
                alert(123)
                app.entity = response.data;
                editor.html(app.entity.goodsDesc.introduction)
                app.entity.goodsDesc.itemImages = JSON.parse(app.entity.goodsDesc.itemImages);
                app.entity.goodsDesc.customAttributeItems = JSON.parse(app.entity.goodsDesc.customAttributeItems);
                app.entity.goodsDesc.specificationItems = JSON.parse(app.entity.goodsDesc.specificationItems)
                for(var i = 0;i<app.entity.itemList.length;i++){
                    var itemObj = app.entity.itemList[i];
                    itemObj.spec=JSON.parse(itemObj.spec)


                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        dele: function () {
            axios.post('/goods/delete.shtml', this.ids).then(function (response) {
                console.log(response);
                if (response.data.success) {
                    app.searchList(1);
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        uploadFile: function () {
            var formData = new FormData();
            formData.append('file', file.files[0]);
            axios({
                url: 'http://localhost:9110/upload/uploadFile.shtml',
                data: formData,
                method: 'post',
                headers: {
                    'Content-Type': 'multipart/form-data'
                },
                //开启跨域请求携带相关认证信息
                withCredentials: true
            }).then(function (response) {
                if (response.data.success) {
                    //上传成功
                    //app.imageurl=response.data.message;
                    app.image_entity.url = response.data.message;

                } else {
                    //上传失败
                    alert(response.data.message);
                }
            })
        },
        addImage_Entity: function () {
            this.entity.goodsDesc.itemImages.push(this.image_entity)
        },
        remove_image_entity: function () {

            //移除图片
            for (var i = 0; i <= this.ids.length; i++) {
                this.entity.goodsDesc.itemImages.splice(this.ids[i], 1);
            }

        },
        findItemCat1List: function () {
            axios.get('/itemCat/findByParentId/0.shtml').then(function (response) {
                app.ItemCat1List = response.data;

            }).catch(function (error) {
                console.log("没拿到数据");
            });
        },
        //当点击 复选框的时候 调用 影响变量 entity.goodsDesc.specificationItems的值
        updateChecked: function (event, specName, specValue) {
            var searchObject = this.searchObjectByKey(this.entity.goodsDesc.specificationItems, specName, 'attributeName');
            //1.如果有对象
            if (searchObject != null) {

                //判断 是否是勾选 如果勾选 添加数据
                if (event.target.checked) {
                    searchObject.attributeValue.push(specValue);
                } else {
                    //不勾选移除呗          找到值对应的下标 移除同名数值。
                    searchObject.attributeValue.splice(searchObject.attributeValue.indexOf(specValue), 1);
                    //如果全不勾选，即不保存attributeValue 那就把对象干掉
                    //判断数组的长度为0 干掉对象
                    if (searchObject.attributeValue.length == 0) {
                        this.entity.goodsDesc.specificationItems.splice(this.entity.goodsDesc.specificationItems.indexOf(searchObject), 1)
                    }
                }

                //规格选的值 添加到对象中的attributeValue中

            } else {
                //2.如果没有对象,直接添加对象就好
                this.entity.goodsDesc.specificationItems.push({
                    "attributeValue": [specValue], "attributeName": specName
                })
            }

        },


        //根据attributeName从变量中获取对象   key表示要找的属性的值
        searchObjectByKey: function (list, specName, key) {
            //   var specificationItems = this.entity.goodsDesc.specificationItems;
            //[{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]},{"attributeName":"屏幕尺寸","attributeValue":["6寸","5.5寸"]}]
            for (var i = 0; i < list.length; i++) {
                var obj = list[i];//{"attributeValue":["移动3G","移动4G"],"attributeValue":["6寸","5.5寸"]}
                if (specName == obj[key]) {
                    return obj;
                }


            }
            return null;
        },

        createList: function () {
            //1.初始化
            this.entity.itemList = [{'price': 0, 'num': 0, 'status': '0', 'isDefault': '0', spec: {}}];

            //2.循环遍历entity.goodsDesc.specificationItems:
            //[{"attributeValue":["移动3G","移动4G"],"attributeName":"网络"}]
            var specificationItemsObject = this.entity.goodsDesc.specificationItems;
            for (var i = 0; i < specificationItemsObject.length; i++) {
                var obj = specificationItemsObject[i];//  {"attributeValue":["移动3G","移动4G"],"attributeName":"网络"}
                //3.拼接要的格式
                this.entity.itemList = this.addColumn(this.entity.itemList, obj.attributeName, obj.attributeValue);
            }
        },
        /**
         *
         * @param list  [{'price':0,'num':0,'status':'0','isDefault':'0',spec:{}}]
         * @param columnName  "网络"
         * @param columnValue   ["移动3G","移动4G"]
         */
        addColumn: function (list, columnName, columnValue) {
            var newList = [];
            for (var i = 0; i < list.length; i++) {
                var oldRow = list[i];// {'price':0,'num':0,'status':'0','isDefault':'0',spec:{}}
                for (var j = 0; j < columnValue.length; j++) {
                    var newRow = JSON.parse(JSON.stringify(oldRow));
                    newRow.spec[columnName] = columnValue[j];//移动3G  深克隆     {'price':0,'num':0,'status':'0','isDefault':'0',spec:{"网络":"移动3G"}}
                    newList.push(newRow);
                }
            }
            return newList;
        },
        isChecked:function (specName,specValue){
            //1.根据规格的名称，从变量中查询是否有对象
            var searchObj = this.searchObjectByKey(this.entity.goodsDesc.specificationItems,specName,'attributeName');
            //2.如有对象再查询 是否匹配规格的选项值
            if (searchObj != null) {
                if (searchObj.attributeValue.indexOf(specValue)!=-1) {
                         return true;
                }  
            }

            return false;
            
        }

    },
    watch: {
        //监听
        'entity.goods.category1Id': function (newval, oldval) {
            if (newval != undefined) {
                axios.get('/itemCat/findByParentId/' + newval + '.shtml').then(function (response) {
                        app.ItemCat2List = response.data;

                    }
                )
            }
        },
        'entity.goods.category2Id': function (newval, oldval) {
            if (newval != undefined) {
                axios.get('/itemCat/findByParentId/' + newval + '.shtml').then(function (response) {
                        app.ItemCat3List = response.data;

                    }
                )
            }
        },


        'entity.goods.category3Id': function (newval, oldval) {
            if (newval != undefined) {
                axios.get('/itemCat/findOne/' + newval + '.shtml').then(function (response) {

                        //app.entity.goods.typeTemplateId = response.data.typeId;
                        //直接赋值 视图不会渲染
                        //app.entity.goods.typeTemplateId=response.data.typeId;
                        //第一个参数 是指定给哪一个对象赋值
                        //第二个参数 是给指定的哪一个属性赋值
                        //第三个参数 指定的赋值的值是多少
                        //设置 值  并且视图会渲染
                        app.$set(app.entity.goods, 'typeTemplateId', response.data.typeId)


                    }
                )
            }
        },


        'entity.goods.typeTemplateId': function (newval, oldval) {
            if (newval != undefined) {
                axios.get('/typeTemplate/findOne/' + newval + '.shtml').then(function (response) {

                        var typeTemplate = response.data

                        app.brandTextList = JSON.parse(typeTemplate.brandIds)
                        if (app.entity.goods.id == null || app.entity.goods.id == undefined) {
                            app.entity.goodsDesc.customAttributeItems = JSON.parse(typeTemplate.customAttributeItems);

                        }
                    }
                )
            }

            if (newval != undefined) {
                axios.get('/typeTemplate/findSpecList.shtml?typeTemplateId=' + newval).then(function (response) {
                        app.specList = response.data;


                    }
                )


            }
        }
    },


    //钩子函数 初始化了事件和
    created: function () {

        var requestObject = this.getUrlParam();

        this.findItemCat1List()
        this.findOne(requestObject.id)

    }

})
