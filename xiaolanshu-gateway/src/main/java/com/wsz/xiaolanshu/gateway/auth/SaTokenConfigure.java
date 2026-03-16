package com.wsz.xiaolanshu.gateway.auth;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-11-26 14:40
 * @Company:
 */
@Configuration
@Slf4j
public class SaTokenConfigure {
    // 注册 Sa-Token全局过滤器
    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                // 拦截地址
                .addInclude("/**")    /* 拦截全部path */
                // 开放地址
                .addExclude(
                        "/",             // 放行根路径（最重要！）
                        "/index.html",          // 放行index.html
                        "/favicon.ico",         // 放行图标
                        "/discover",            // 放行discover页面
                        "/assets/**",           // 放行assets目录下的所有文件
                        "/css/**",              // 放行CSS文件
                        "/js/**",               // 放行JS文件
                        "/img/**",              // 放行图片
                        "/fonts/**",            // 放行字体文件
                        "/public/**"            // 放行public目录
                )
                // 鉴权方法：每次访问进入
                .setAuth(obj -> {
                    // 登录校验 -- 拦截所有路由，并排除/user/doLogin 用于开放登录
                    SaRouter.match("/**") // 拦截所有路由
                            .notMatch("/auth/login") // 排除登录接口
                            .notMatch("/auth/verification/code/send") // 排除验证码发送接口
                            .notMatch("/user/user/profile")
                            .notMatch("/note/channel/list")
                            .notMatch("/note/discover/note/list")
                            .notMatch("/note/note/detail")
                            .notMatch("/comment/comment/list")
                            .notMatch("/note/note/isLikedAndCollectedData")
                            .notMatch("/comment/comment/child/list")
                            .check(r -> StpUtil.checkLogin()); // 校验是否登录


                    // 权限认证 -- 不同模块, 校验不同权限
                    // SaRouter.match("/auth/user/logout", r -> StpUtil.checkPermission("user"));
                    // SaRouter.match("/auth/user/logout", r -> StpUtil.checkRole("admin"));
                    SaRouter.match("/auth/logout", r -> StpUtil.checkPermission("app:note:publish"));
                    // SaRouter.match("/admin/**", r -> StpUtil.checkPermission("admin"));
                    // SaRouter.match("/goods/**", r -> StpUtil.checkPermission("goods"));
                    // SaRouter.match("/orders/**", r -> StpUtil.checkPermission("orders"));

                    // 更多匹配 ...  */
                })
                .setError(e -> {
                    // return SaResult.error(e.getMessage());
                    // 手动抛出异常，抛给全局异常处理器
                    if (e instanceof NotLoginException) { // 未登录异常
                        throw new NotLoginException(e.getMessage(), null, null);
                    } else if (e instanceof NotPermissionException || e instanceof NotRoleException) { // 权限不足，或不具备角色，统一抛出权限不足异常
                        throw new NotPermissionException(e.getMessage());
                    } else { // 其他异常，则抛出一个运行时异常
                        throw new RuntimeException(e.getMessage());
                    }
                });

    }
}
