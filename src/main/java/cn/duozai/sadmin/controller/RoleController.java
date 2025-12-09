package cn.duozai.sadmin.controller;

import cn.duozai.sadmin.repository.RoleEntity;
import cn.duozai.sadmin.repository.UsersEntity;
import cn.duozai.sadmin.utils.ResponseResult;
import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.api.pagination.EasyPageResult;
import com.easy.query.solon.annotation.Db;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.Valid;

import java.util.List;

/**
 * 角色控制器
 * @visduo
 */
@Valid
@Mapping("/role")
@Controller
public class RoleController {

    @Db
    EasyEntityQuery easyEntityQuery;

    /**
     * 获取角色列表
     * @visduo
     *
     * @param name 查询条件-角色名称
     * @param remarks 查询条件-角色备注
     * @param pageIndex 查询条件-页码
     * @param pageSize 查询条件-页大小
     * @return 角色列表分页结果
     */
    @Get
    @Mapping("/list")
    public ResponseResult list(@Param(required = false) String name,
                               @Param(required = false) String remarks,
                               @Param(required = false, defaultValue = "1") Integer pageIndex,
                               @Param(required = false, defaultValue = "10") Integer pageSize) {
        EasyPageResult<RoleEntity> rolePageResult = easyEntityQuery.queryable(RoleEntity.class)
                .where(r -> {
                    // 角色名称不为空时对角色名称进行模糊查询
                    r.name().like(StrUtil.isNotBlank(name), name);
                    // 角色备注不为空时对角色备注进行模糊查询
                    r.remarks().like(StrUtil.isNotBlank(remarks), remarks);
                })
                // 查询分页数据
                .toPageResult(pageIndex, pageSize);

        return ResponseResult.success("查询成功", rolePageResult);
    }

    /**
     * 添加角色
     * @visduo
     *
     * @param name 角色名称
     * @param remarks 角色备注
     * @return 添加结果
     */
    @Post
    @Mapping("/add")
    public ResponseResult add(@NotBlank(message = "角色名称不能为空") String name,
                              @NotBlank(message = "角色备注不能为空") String remarks) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName(name);
        roleEntity.setRemarks(remarks);

        // 插入角色数据
        easyEntityQuery.insertable(roleEntity).executeRows();

        return ResponseResult.success("添加成功", null);
    }

    /**
     * 修改角色
     * @visduo
     *
     * @param id 角色id
     * @param name 角色名称
     * @param remarks 角色备注
     * @return 修改结果
     */
    @Put
    @Mapping("/update/{id}")
    public ResponseResult update(@Path int id,
                                 @NotBlank(message = "角色名称不能为空") String name,
                                 @NotBlank(message = "角色备注不能为空") String remarks) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(id);
        roleEntity.setName(name);
        roleEntity.setRemarks(remarks);

        // 修改角色数据
        easyEntityQuery.updatable(roleEntity).executeRows();

        return ResponseResult.success("修改成功", null);
    }

    /**
     * 删除角色
     * @visduo
     *
     * @param id 角色id
     * @return 删除结果
     */
    @Delete
    @Mapping("/delete/{id}")
    public ResponseResult delete(@Path int id) {
        // 禁止删除ID为1的角色
        if (id == 1) {
            return ResponseResult.failure("默认角色禁止删除", null);
        }

        // 该角色下有关联用户，禁止删除
        long count = easyEntityQuery.queryable(UsersEntity.class)
                .where(u -> {
                    u.roleId().eq(id);
                }).count();

        if (count > 0) {
            return ResponseResult.failure("该角色下有关联用户，禁止删除", null);
        }

        // 删除角色数据
        easyEntityQuery.deletable(RoleEntity.class)
                .where(r -> {
                    r.id().eq(id);
                }).executeRows();

        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据角色ID获取角色信息
     * @visduo
     *
     * @param id 角色ID
     * @return 角色信息
     */
    @Get
    @Mapping("/get/{id}")
    public ResponseResult get(@Path int id) {
        RoleEntity roleEntity = easyEntityQuery.queryable(RoleEntity.class)
                .where(r -> {
                    r.id().eq(id);
                }).firstOrNull();

        return ResponseResult.success("查询成功", roleEntity);
    }

    /**
     * 获取角色列表（全量）
     * @visduo
     *
     * @return 角色列表
     */
    @Get
    @Mapping("/optionList")
    public ResponseResult optionList() {
        List<RoleEntity> roleList = easyEntityQuery.queryable(RoleEntity.class)
                .toList();

        return ResponseResult.success("查询成功", roleList);
    }

    /**
     * 修改角色权限
     * @visduo
     *
     * @param id 角色id
     * @param perms 授权的权限菜单列表
     * @return 修改结果
     */
    @Put
    @Mapping("/updatePerms/{id}")
    public ResponseResult updatePerms(@Path int id,
                                      @Param String perms) {
        // 禁止修改ID为1的角色
        if (id == 1) {
            return ResponseResult.failure("默认角色禁止修改", null);
        }

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(id);
        roleEntity.setPerms(perms);

        easyEntityQuery.updatable(roleEntity).executeRows();

        return ResponseResult.success("修改成功", null);
    }

}
