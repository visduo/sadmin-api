package cn.duozai.sadmin.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.duozai.sadmin.repository.UsersEntity;
import cn.duozai.sadmin.repository.proxy.UsersEntityProxy;
import cn.duozai.sadmin.utils.MD5SaltsUtil;
import cn.duozai.sadmin.utils.ResponseResult;
import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.api.pagination.EasyPageResult;
import com.easy.query.solon.annotation.Db;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.NotBlank;

/**
 * 用户控制器
 * @visduo
 */
@Mapping("/users")
@Controller
public class UsersController {

    @Db
    EasyEntityQuery easyEntityQuery;

    /**
     * 获取用户列表
     * @visduo
     *
     * @param deptId 查询条件-关联部门ID
     * @param roleId 查询条件-关联角色ID
     * @param status 查询条件-账户状态
     * @param username 查询条件-账户账号
     * @param realname 查询条件-真实姓名
     * @param remarks 查询条件-用户备注
     * @param pageIndex 查询条件-页码
     * @param pageSize 查询条件-页大小
     * @return 用户列表分页结果
     */
    @Get
    @Mapping("/list")
    public ResponseResult list(@Param(required = false) Integer deptId,
                               @Param(required = false) Integer roleId,
                               @Param(required = false) Integer status,
                               @Param(required = false) String username,
                               @Param(required = false) String realname,
                               @Param(required = false) String remarks,
                               @Param(required = false, defaultValue = "1") Integer pageIndex,
                               @Param(required = false, defaultValue = "10") Integer pageSize) {
        EasyPageResult<UsersEntity> userPageResult = easyEntityQuery.queryable(UsersEntity.class)
                .where(u -> {
                    // 关联部门ID不为空时进行精确查询
                    u.deptId().eq(deptId != null, deptId);
                    // 关联角色ID不为空时进行精确查询
                    u.roleId().eq(roleId != null, roleId);
                    // 账户状态不为空时进行精确查询
                    u.status().eq(status != null, status);
                    // 账户账号不为空时进行模糊查询
                    u.username().like(StrUtil.isNotBlank(username), username);
                    // 真实姓名不为空时进行模糊查询
                    u.realname().like(StrUtil.isNotBlank(realname), realname);
                    // 用户备注不为空时进行模糊查询
                    u.remarks().like(StrUtil.isNotBlank(remarks), remarks);
                })
                // 关联查询部门、角色实体
                .include(UsersEntityProxy::dept)
                .include(UsersEntityProxy::role)
                // 查询分页数据
                .toPageResult(pageIndex, pageSize);

        return ResponseResult.success("查询成功", userPageResult);
    }

    /**
     * 添加用户
     * @visduo
     *
     * @param deptId 关联部门ID
     * @param roleId 关联角色ID
     * @param username 账户账号
     * @param password 账户密码
     * @param realname 真实姓名
     * @param remarks 用户备注
     * @param status 账户状态
     * @return 添加结果
     */
    @Post
    @Mapping("/add")
    public ResponseResult add(@NotBlank(message = "关联部门ID不能为空") Integer deptId,
                              @NotBlank(message = "关联角色ID不能为空") Integer roleId,
                              @NotBlank(message = "账户账号不能为空") String username,
                              @NotBlank(message = "账户密码不能为空") String password,
                              @NotBlank(message = "真实姓名不能为空") String realname,
                              @NotBlank(message = "用户备注不能为空") String remarks,
                              @NotBlank(message = "账户状态不能为空") Integer status) {
        // 检查账户账号是否已存在
        long count = easyEntityQuery.queryable(UsersEntity.class)
                .where(u -> {
                    u.username().eq(username);
                }).count();

        if (count > 0) {
            return ResponseResult.failure("账户账号已存在", null);
        }

        UsersEntity usersEntity = new UsersEntity();
        usersEntity.setDeptId(deptId);
        usersEntity.setRoleId(roleId);
        usersEntity.setUsername(username);
        usersEntity.setRealname(realname);
        usersEntity.setRemarks(remarks);
        usersEntity.setStatus(status);

        // 生成盐值并加密密码
        String salts = MD5SaltsUtil.salts();
        String md5Password = MD5SaltsUtil.md5(password, salts);
        usersEntity.setPassword(md5Password);
        usersEntity.setSalts(salts);

        // 插入用户数据
        easyEntityQuery.insertable(usersEntity).executeRows();

        return ResponseResult.success("添加成功", null);
    }

    /**
     * 修改用户
     * @visduo
     *
     * @param id 用户id
     * @param deptId 关联部门ID
     * @param roleId 关联角色ID
     * @param password 账户密码
     * @param realname 真实姓名
     * @param remarks 用户备注
     * @param status 账户状态
     * @return 修改结果
     */
    @Put
    @Mapping("/update/{id}")
    public ResponseResult update(@Path int id,
                                 @NotBlank(message = "关联部门ID不能为空") Integer deptId,
                                 @NotBlank(message = "关联角色ID不能为空") Integer roleId,
                                 @Param(required = false) String password,
                                 @NotBlank(message = "真实姓名不能为空") String realname,
                                 @NotBlank(message = "用户备注不能为空") String remarks,
                                 @NotBlank(message = "账户状态不能为空") Integer status) {
        // 禁止修改当前登录的用户
        if (StpUtil.getLoginIdAsInt() == id) {
            return ResponseResult.failure("不能修改当前登录用户", null);
        }

        UsersEntity usersEntity = new UsersEntity();
        usersEntity.setId(id);
        usersEntity.setDeptId(deptId);
        usersEntity.setRoleId(roleId);
        usersEntity.setRealname(realname);
        usersEntity.setRemarks(remarks);
        usersEntity.setStatus(status);

        // 如果传递了密码，则重新生成盐值并加密
        if (StrUtil.isNotBlank(password)) {
            String salts = MD5SaltsUtil.salts();
            String md5Password = MD5SaltsUtil.md5(password, salts);
            usersEntity.setPassword(md5Password);
            usersEntity.setSalts(salts);

            // 密码修改后，将该用户踢下线
            StpUtil.kickout(id);
        }

        // 修改用户数据
        easyEntityQuery.updatable(usersEntity).executeRows();

        return ResponseResult.success("修改成功", null);
    }

    /**
     * 删除用户
     * @visduo
     *
     * @param id 用户id
     * @return 删除结果
     */
    @Delete
    @Mapping("/delete/{id}")
    public ResponseResult delete(@Path int id) {
        // 禁止删除ID为1的用户
        if (id == 1) {
            return ResponseResult.failure("默认用户禁止删除", null);
        }

        // 禁止删除当前登录的用户
        if (StpUtil.getLoginIdAsInt() == id) {
            return ResponseResult.failure("不能删除当前登录用户", null);
        }

        // 删除前，将该用户踢下线
        StpUtil.kickout(id);

        // 删除用户数据
        easyEntityQuery.deletable(UsersEntity.class)
                .where(u -> {
                    u.id().eq(id);
                }).executeRows();

        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据用户ID获取用户信息
     * @visduo
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @Get
    @Mapping("/get/{id}")
    public ResponseResult get(@Path int id) {
        UsersEntity usersEntity = easyEntityQuery.queryable(UsersEntity.class)
                .where(u -> {
                    u.id().eq(id);
                })
                .include(UsersEntityProxy::dept)
                .include(UsersEntityProxy::role)
                .firstOrNull();

        return ResponseResult.success("查询成功", usersEntity);
    }

}
