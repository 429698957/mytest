package com.pinyougou.shop.config;

import com.pinyougou.shop.secutity.UserDetailsServicel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
public class ShopSecurityConfig extends WebSecurityConfigurerAdapter {
     @Autowired
    private UserDetailsServicel userDetailsServicel;
     @Autowired
     private PasswordEncoder bCryptPasswordEncoder;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
      //  auth.inMemoryAuthentication().withUser("user").password("{noop}123456").roles("USER");

        auth.userDetailsService(userDetailsServicel).passwordEncoder(bCryptPasswordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //1.拦截请求 admin/**
        http.authorizeRequests()
                //2.放行静态资源和登录页面
                .antMatchers("/css/**","/img/**","/js/**","/plugins/**","/*.html","/seller/add.shtml").permitAll()
                .anyRequest().authenticated();
        //也可以.anyMathers("/**").hasRole("ADMIN") 表示有ADMIN这个角色 就放过。
        //3.配置自定义的表单的登录
        http.formLogin()
                .loginPage("/shoplogin.html")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/admin/index.html",true)
                .failureUrl("/shoplogin.html?error");

        //4.关闭防盗链
        http.csrf().disable();
        http.headers().frameOptions().sameOrigin();
        //退出并清空缓存
        http.logout().logoutUrl("/logout").invalidateHttpSession(true);
    }
}
