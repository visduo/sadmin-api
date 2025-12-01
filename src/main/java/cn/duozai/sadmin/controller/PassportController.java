package cn.duozai.sadmin.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.duozai.sadmin.repository.LoginlogEntity;
import cn.duozai.sadmin.repository.UsersEntity;
import cn.duozai.sadmin.utils.ClientipUtil;
import cn.duozai.sadmin.utils.MD5SaltsUtil;
import cn.duozai.sadmin.utils.ResponseResult;
import cn.hutool.core.date.DateUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Post;
import org.noear.solon.core.handle.Context;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.Valid;

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

}
