package com.pinyougou.shop.secutity;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailsServicel implements UserDetailsService {

    @Reference
    private SellerService sellerService;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {



        //从数据库中获取用户的信息
        //1.1根据页面传递过来的用户名 查询
        TbSeller one = sellerService.findOne(username);
        //1.2如果用户不存在 直接返回null
        if (one==null) {
            return null;

        }
        if (!one.getStatus().equals("1")) {
            return null;
        }
        //1.3如果存在 需要匹配密码
        String password=one.getPassword();
        //交给springsecurity框架 自动匹配
        //给用户设置权限
        /*List<GrantedAuthority> list = new ArrayList<>();
        list.add(new SimpleGrantedAuthority("ROLE_USER"));//授权角色*/

        return new User(username,password, AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
    }
}
