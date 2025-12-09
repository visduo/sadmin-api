package cn.duozai.sadmin.controller;

import cn.duozai.sadmin.repository.PermsEntity;
import cn.duozai.sadmin.utils.ResponseResult;
import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;
import org.noear.solon.validation.annotation.Valid;

import java.util.List;

/**
 * 权限控制器
 * @visduo
 */
@Valid
@Mapping("/perms")
@Controller
public class PermsController {

    @Db
    EasyEntityQuery easyEntityQuery;

    /**
     * 获取权限列表
     * @visduo
     *
     * @param name 查询条件-权限名称
     * @param identifier 查询条件-权限标识
     * @return 权限列表分页结果
     */
    @Get
    @Mapping("/list")
    public ResponseResult list(@Param(required = false) String name,
                               @Param(required = false) String identifier) {
        List<PermsEntity> permsList = easyEntityQuery.queryable(PermsEntity.class)
                .where(p -> {
                    // 权限名称不为空时对权限名称进行模糊查询
                    p.name().like(StrUtil.isNotBlank(name), name);
                    // 权限标识不为空时对权限标识进行模糊查询
                    p.identifier().like(StrUtil.isNotBlank(identifier), identifier);
                })
                .orderBy(p -> {
                    // 根据排序ID进行排序，越小越靠前
                    p.sortId().asc();
                })
                // 查询全量列表数据
                .toList();

        return ResponseResult.success("查询成功", permsList);
    }

    /**
     * 添加权限
     * @visduo
     *
     * @param parentId 父级权限ID
     * @param name 权限名称
     * @param identifier 权限标识
     * @param path 权限路由路径
     * @param component 权限路由组件
     * @param type 权限类型
     * @param sortId 排序ID
     * @param status 权限状态
     * @return 添加结果
     */
    @Post
    @Mapping("/add")
    public ResponseResult add(@NotNull(message = "父级权限ID不能为空") Integer parentId,
                              @NotBlank(message = "权限名称不能为空") String name,
                              @NotBlank(message = "权限标识不能为空") String identifier,
                              @Param(required = false) String path,
                              @Param(required = false) String component,
                              @NotBlank(message = "权限类型不能为空") String type,
                              @NotNull(message = "排序ID不能为空") Integer sortId,
                              @NotNull(message = "权限状态不能为空") Integer status) {
        PermsEntity permsEntity = new PermsEntity();
        permsEntity.setParentId(parentId);
        permsEntity.setName(name);
        permsEntity.setIdentifier(identifier);
        permsEntity.setPath(path);
        permsEntity.setComponent(component);
        permsEntity.setType(Integer.parseInt(type));
        permsEntity.setSortId(sortId);
        permsEntity.setStatus(status);

        // 插入权限数据
        easyEntityQuery.insertable(permsEntity).executeRows();

        return ResponseResult.success("添加成功", null);
    }

    /**
     * 修改权限
     * @visduo
     *
     * @param id 权限id
     * @param name 权限名称
     * @return 修改结果
     */
    @Put
    @Mapping("/update/{id}")
    public ResponseResult update(@Path int id,
                                 @NotNull(message = "父级权限ID不能为空") Integer parentId,
                                 @NotBlank(message = "权限名称不能为空") String name,
                                 @NotBlank(message = "权限标识不能为空") String identifier,
                                 @Param(required = false) String path,
                                 @Param(required = false) String component,
                                 @NotBlank(message = "权限类型不能为空") String type,
                                 @NotNull(message = "排序ID不能为空") Integer sortId,
                                 @NotNull(message = "权限状态不能为空") Integer status) {
        PermsEntity permsEntity = new PermsEntity();
        permsEntity.setId(id);
        permsEntity.setParentId(parentId);
        permsEntity.setName(name);
        permsEntity.setIdentifier(identifier);
        permsEntity.setPath(path);
        permsEntity.setComponent(component);
        permsEntity.setType(Integer.parseInt(type));
        permsEntity.setSortId(sortId);
        permsEntity.setStatus(status);

        // 修改权限数据
        easyEntityQuery.updatable(permsEntity).executeRows();

        return ResponseResult.success("修改成功", null);
    }

    /**
     * 删除权限
     * @visduo
     *
     * @param id 权限id
     * @return 删除结果
     */
    @Delete
    @Mapping("/delete/{id}")
    public ResponseResult delete(@Path int id) {
        // 该权限下有子权限，禁止删除
        long count = easyEntityQuery.queryable(PermsEntity.class)
                .where(p -> {
                    p.parentId().eq(id);
                }).count();

        if (count > 0) {
            return ResponseResult.failure("该权限下有子权限，禁止删除", null);
        }

        // 删除权限数据
        easyEntityQuery.deletable(PermsEntity.class)
                .where(p -> {
                    p.id().eq(id);
                }).executeRows();

        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据权限ID获取权限信息
     * @visduo
     *
     * @param id 权限ID
     * @return 权限信息
     */
    @Get
    @Mapping("/get/{id}")
    public ResponseResult get(@Path int id) {
        PermsEntity permsEntity = easyEntityQuery.queryable(PermsEntity.class)
                .where(p -> {
                    p.id().eq(id);
                }).firstOrNull();

        return ResponseResult.success("查询成功", permsEntity);
    }

}
