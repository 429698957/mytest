<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
       <#include "hear.ftl" />
</head>
<body>
<!--${name} 插值表达式-->

<#--<#assign 指定变量-->
<#--<#assign 变量名="" />--> <#assign myname="zhangsan" />
<#--<#assign 变量名="" />--> <#assign myname12={"id":1,"name":"zhangsan"} />
<#--<#assign 变量名="" />--> <#assign myname123='{"id":1,"name":"zhangsan"}' />
<#assign flag=true />

<h1 style="color: red">${name}</h1>
<br/>
<h1 style="color: red">${myname}</h1>
<br/>
<#--获得对象数据的值-->
${user.id?c}
${user.name}

<br/>
<#if flag>
<div>
    数据1
</div>
<#else >
<div>
    数据2
</div>
</#if>

<table>

    <tr>
        <td>id</td>
        <td>name</td>
        <td>index</td>
    </tr>
    <#list mylist as item>
    <tr>
        <td>${item.id}</td>
        <td>${item.name}</td>
        <td>${item_index+1}</td>
    </tr>
    </#list>
</table>
<br/>
总记录数:${mylist?size}

<#--${myname12}<br/>-->
${myname123}

<#assign mypersonjson=myname123?eval /></br>
${mypersonjson.id}
${mypersonjson.name}

<br/>
${date?date}<br/>
${date?time}<br/>
${date?datetime}<br/>

${date?string("yyyy/MM/dd HH:mm:ss")}

${nullkey!"app里没设置nullkuy的值 我是默认的值，如果nullkey没有值 就显示这个 不要赋默认值为空 就写个叹号就行"}<br/>

<#if nullkey?exists> <#--exists用两个问号代替也可以-->
${nullkey}
<#else>
没有纸
</#if><br>

<#if 1 gt 2> <#--gt是大于号 gte大于等于 不能用1>2 因为会把>当做符号看待-->
    1确实小于2
<#else>
1》2
</#if>
</body>
</html>

