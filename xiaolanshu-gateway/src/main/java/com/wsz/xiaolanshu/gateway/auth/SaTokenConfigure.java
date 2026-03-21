package com.wsz.xiaolanshu.gateway.auth;

import cn.dev33.satoken.exception.DisableServiceException;
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

    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                .addInclude("/**")
                .addExclude(
                        "/", "/index.html", "/favicon.ico", "/discover", "/assets/**",
                        "/css/**", "/js/**", "/img/**", "/fonts/**", "/public/**"
                )
                .setAuth(obj -> {
                    // 1. 基础登录与全局封禁校验
                    SaRouter.match("/**")
                            .notMatch("/auth/login", "/auth/verification/code/send",
                                    "/user/user/profile", "/note/channel/list",
                                    "/note/discover/note/list", "/note/note/detail",
                                    "/comment/comment/list", "/note/note/isLikedAndCollectedData",
                                    "/comment/comment/child/list")
                            .check(r -> {
                                StpUtil.checkLogin();
                                // 获取当前登录用户ID

                                // 【修改点】：只校验全局封禁（默认服务名是 <default>）
                                // 只有当 isDisable 返回 true，且 isDisable(userId, "publish_comment") 时，不能混为一谈。
                                // 使用 checkDisable(id, "<default>") 专门指代全局封禁
                                StpUtil.checkDisable(StpUtil.getLoginIdAsLong(), "<default>");
                            });


                    // 2. 校验：禁止发布/编辑笔记
                    // (请根据你实际的 Controller 路由地址调整)
                    SaRouter.match("/note/note/publish", "/note/note/update")
                            .check(r -> StpUtil.checkDisable(StpUtil.getLoginIdAsLong(), "publish_note"));

                    // 3. 校验：禁止发布评论
                    SaRouter.match("/comment/comment/publish")
                            .check(r -> StpUtil.checkDisable(StpUtil.getLoginIdAsLong(), "publish_comment"));

                    // 4. 校验：禁止点赞/收藏
                    SaRouter.match("/note/note/like", "/note/note/unlike",
                                    "/note/note/collect", "/note/note/uncollect",
                                    "/comment/comment/like", "/comment/comment/unlike")
                            .check(r -> StpUtil.checkDisable(StpUtil.getLoginIdAsLong(), "like_collect"));

                    // 5. 校验：禁止修改个人资料
                    SaRouter.match("/user/user/update")
                            .check(r -> StpUtil.checkDisable(StpUtil.getLoginIdAsLong(), "update_profile"));

                    // 后台角色校验等...
                    SaRouter.match("/admin/**", r -> StpUtil.checkRoleOr("super_admin", "operation_admin"));
                })
                .setError(e -> {
                    if (e instanceof NotLoginException) {
                        throw new NotLoginException(e.getMessage(), null, null);
                    } else if (e instanceof DisableServiceException) {
                        // ================== 捕获分类封禁异常并提示 ==================
                        DisableServiceException de = (DisableServiceException) e;
                        String service = (String) de.getService();

                        switch (service) {
                            case "publish_note":
                                throw new NotPermissionException("您的账号已被限制发布和编辑笔记！");
                            case "publish_comment":
                                throw new NotPermissionException("您的账号已被限制发表评论！");
                            case "like_collect":
                                throw new NotPermissionException("您的账号已被限制进行互动（点赞/收藏）！");
                            case "update_profile":
                                throw new NotPermissionException("您的账号已被限制修改个人资料！");
                            default:
                                throw new NotPermissionException("您的账号已被封禁，禁止任何操作！");
                        }
                    } else if (e instanceof NotPermissionException || e instanceof NotRoleException) {
                        throw new NotPermissionException(e.getMessage());
                    } else {
                        throw new RuntimeException(e.getMessage());
                    }
                });
    }
}
