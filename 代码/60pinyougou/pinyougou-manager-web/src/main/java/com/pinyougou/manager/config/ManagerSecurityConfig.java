package com.pinyougou.manager.config;


import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class ManagerSecurityConfig extends WebSecurityConfigurerAdapter {


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
       auth.inMemoryAuthentication().withUser("admin").password("{noop}admin").roles("ADMIN");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
         //1.拦截请求 admin/**
         http.authorizeRequests()
                 //2.放行静态资源和登录页面
                 .antMatchers("/css/**","/img/**","/js/**","/plugins/**","/login.html").permitAll()
                 .anyRequest().authenticated();
              //也可以.anyMathers("/**").hasRole("ADMIN") 表示有ADMIN这个角色 就放过。
               //3.配置自定义的表单的登录
        http.formLogin()
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/admin/index.html",true)
                .failureUrl("/login.html?error");
               //4.关闭防盗链
        http.csrf().disable();
        http.headers().frameOptions().sameOrigin();
        //退出并清空缓存
        http.logout().logoutUrl("/logout").invalidateHttpSession(true);
    }
}
