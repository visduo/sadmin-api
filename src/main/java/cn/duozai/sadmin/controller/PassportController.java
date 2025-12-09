package cn.duozai.sadmin.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.duozai.sadmin.repository.LoginlogEntity;
import cn.duozai.sadmin.repository.PermsEntity;
import cn.duozai.sadmin.repository.RoleEntity;
import cn.duozai.sadmin.repository.UsersEntity;
import cn.duozai.sadmin.repository.proxy.UsersEntityProxy;
import cn.duozai.sadmin.utils.ClientipUtil;
import cn.duozai.sadmin.utils.MD5SaltsUtil;
import cn.duozai.sadmin.utils.ResponseResult;
import cn.hutool.core.date.DateUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Post;
import org.noear.solon.core.handle.Context;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.Valid;

import java.util.Arrays;
import java.util.List;

/**
 * 权限认证控制器
 * @visduo
 *
 * @Valid：请求参数校验
 * 参考文档：https://solon.noear.org/article/49
 */
@Valid
@Mapping("/passport")
@Controller
public class PassportController {

    @Db
    EasyEntityQuery easyEntityQuery;

    /**
     * 登录
     * @visduo
     *
     * @param ctx 上下文对象
     * @param username 账户账号
     * @param password 账户密码
     * @return 登录结果
     */
    @Post
    @Mapping("/login")
    public ResponseResult login(Context ctx,
                                @NotBlank(message = "账户账号不能为空") String username,
                                @NotBlank(message = "账户密码不能为空") String password) {
        // 根据账户账号查询用户对象
        UsersEntity usersEntity = easyEntityQuery.queryable(UsersEntity.class)
                .where(u -> {
                    // 条件：u.username = #{username}
                    u.username().eq(username);
                })
                // 关联查询部门、角色实体
                .include(UsersEntityProxy::dept)
                .include(UsersEntityProxy::role)
                .firstOrNull();

        if (usersEntity == null) {
            // 用户对象不存在，返回失败信息
            return ResponseResult.failure("用户不存在", null);
        }

        // 密码校验
        // 判断条件：前端传递的明文密码+盐值加密结果 ?= 数据库存储的密文密码
        String md5SaltsPassword = MD5SaltsUtil.md5(password, usersEntity.getSalts());
        if (!md5SaltsPassword.equals(usersEntity.getPassword())) {
            // 密码错误，返回失败信息
            return ResponseResult.failure("密码错误", null);
        }

        // 账户状态校验，status = 1正常/0禁用
        if (usersEntity.getStatus() != 1) {
            return ResponseResult.failure("用户被禁用", null);
        }

        // 全部校验通过，登录成功
        // 保存登录日志
        LoginlogEntity loginlogEntity = new LoginlogEntity();
        loginlogEntity.setUserId(usersEntity.getId());
        loginlogEntity.setIp(ctx.realIp());
        // 调用客户端IP地址工具类根据IP信息获取IP地址
        loginlogEntity.setAddress(ClientipUtil.parse(ctx.realIp()));
        // 调用HuTool工具类获取当前时间戳
        loginlogEntity.setTimestamp(DateUtil.currentSeconds());
        // 插入登录日志
        easyEntityQuery.insertable(loginlogEntity).executeRows();

        // SaToken执行登录并生成Token
        StpUtil.login(usersEntity.getId());
        String token = StpUtil.getTokenValue();

        // 将用户对象存储在SaToken的会话Session中
        StpUtil.getSession().set("currentUser", usersEntity);

        // 返回结果，并将Token返回给前端
        return ResponseResult.success("登录成功", token);
    }

    /**
     * 注销
     * @visduo
     *
     * @return 注销结果
     */
    @Post
    @Mapping("/logout")
    public ResponseResult logout() {
        // 注销
        StpUtil.logout();
        return ResponseResult.success("注销成功", null);
    }

    /**
     * 获取登录用户信息
     * @visduo
     *
     * @return 登录用户信息实体
     */
    @Get
    @Mapping("/currentUser")
    public ResponseResult currentUser() {
        // 获取当前登录用户对象
        UsersEntity usersEntity = (UsersEntity) StpUtil.getSession().get("currentUser");
        return ResponseResult.success("查询成功", usersEntity);
    }

    /**
     * 获取当前登录用户权限列表
     * @visduo
     *
     * @return 当前登录用户权限列表
     */
    @Get
    @Mapping("/currentPerms")
    public ResponseResult currentPerms() {
        // 从会话中获取权限列表
        List<PermsEntity> permsList = (List<PermsEntity>) StpUtil.getSession().get("perms");
        if (permsList != null) {
            // 权限列表不为空，返回权限列表
            return ResponseResult.success("查询成功", permsList);
        }

        // 获取当前登录用户对象及对应角色
        UsersEntity usersEntity = (UsersEntity) StpUtil.getSession().get("currentUser");
        RoleEntity roleEntity = usersEntity.getRole();

        if(roleEntity.getId() == 1) {
            // 默认角色返回所有权限
            permsList = easyEntityQuery.queryable(PermsEntity.class)
                    .where(p -> {
                        // 只查询启用的权限，不查询隐藏的权限
                        p.status().eq(1);
                    })
                    .orderBy(p -> {
                        // 根据排序ID进行排序，越小越靠前
                        p.sortId().asc();
                    })
                    .toList();
        } else {
            // 非默认角色返回对应角色权限
            // 数据库中存储的角色权限列表为1,2,3,4格式，将其格式化成数组
            String[] permsIds = roleEntity.getPerms().split(",");
            // 将字符串数组转换成整数数组
            Integer[] permsIdsInt = Arrays.stream(permsIds).map(Integer::parseInt).toArray(Integer[]::new);

            permsList = easyEntityQuery.queryable(PermsEntity.class)
                    .where(p -> {
                        // 只查询启用的权限，不查询隐藏的权限
                        p.status().eq(1);
                        // 只查询角色授权的权限列表
                        p.id().in(permsIdsInt);
                    })
                    .orderBy(p -> {
                        // 根据排序ID进行排序，越小越靠前
                        p.sortId().asc();
                    })
                    .toList();
        }

        // 将权限列表存入会话
        StpUtil.getSession().set("perms", permsList);

        return ResponseResult.success("查询成功", permsList);
    }

}
