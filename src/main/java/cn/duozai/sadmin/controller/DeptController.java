package cn.duozai.sadmin.controller;

import cn.duozai.sadmin.repository.DeptEntity;
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
 * 部门控制器
 * @visduo
 */
@Valid
@Mapping("/dept")
@Controller
public class DeptController {

    @Db
    EasyEntityQuery easyEntityQuery;

    /**
     * 获取部门列表
     * @visduo
     *
     * @param name 查询条件-部门名称
     * @param pageIndex 查询条件-页码
     * @param pageSize 查询条件-页大小
     * @return 部门列表分页结果
     */
    @Get
    @Mapping("/list")
    public ResponseResult list(@Param(required = false) String name,
                               @Param(required = false, defaultValue = "1") Integer pageIndex,
                               @Param(required = false, defaultValue = "10") Integer pageSize) {
        EasyPageResult<DeptEntity> deptPageResult = easyEntityQuery.queryable(DeptEntity.class)
                .where(d -> {
                    // 部门名称不为空时对部门名称进行模糊查询
                    d.name().like(StrUtil.isNotBlank(name), name);
                })
                // 查询分页数据
                .toPageResult(pageIndex, pageSize);

        return ResponseResult.success("查询成功", deptPageResult);
    }

    /**
     * 添加部门
     * @visduo
     *
     * @param name 部门名称
     * @return 添加结果
     */
    @Post
    @Mapping("/add")
    public ResponseResult add(@NotBlank(message = "部门名称不能为空") String name) {
        DeptEntity deptEntity = new DeptEntity();
        deptEntity.setName(name);

        // 插入部门数据
        easyEntityQuery.insertable(deptEntity).executeRows();

        return ResponseResult.success("添加成功", null);
    }

    /**
     * 修改部门
     * @visduo
     *
     * @param id 部门id
     * @param name 部门名称
     * @return 修改结果
     */
    @Put
    @Mapping("/update/{id}")
    public ResponseResult update(@Path int id,
                                 @NotBlank(message = "部门名称不能为空") String name) {
        DeptEntity deptEntity = new DeptEntity();
        deptEntity.setId(id);
        deptEntity.setName(name);

        // 修改部门数据
        easyEntityQuery.updatable(deptEntity).executeRows();

        return ResponseResult.success("修改成功", null);
    }

    /**
     * 删除部门
     * @visduo
     *
     * @param id 部门id
     * @return 删除结果
     */
    @Delete
    @Mapping("/delete/{id}")
    public ResponseResult delete(@Path int id) {
        // 该部门下有关联用户，禁止删除
        long count = easyEntityQuery.queryable(UsersEntity.class)
                .where(u -> {
                    u.deptId().eq(id);
                }).count();

        if (count > 0) {
            return ResponseResult.failure("该部门下有关联用户，禁止删除", null);
        }

        // 删除部门数据
        easyEntityQuery.deletable(DeptEntity.class)
                .where(d -> {
                    d.id().eq(id);
                }).executeRows();

        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据部门ID获取部门信息
     * @visduo
     *
     * @param id 部门ID
     * @return 部门信息
     */
    @Get
    @Mapping("/get/{id}")
    public ResponseResult get(@Path int id) {
        DeptEntity deptEntity = easyEntityQuery.queryable(DeptEntity.class)
                .where(d -> {
                    d.id().eq(id);
                }).firstOrNull();

        return ResponseResult.success("查询成功", deptEntity);
    }

    /**
     * 获取部门列表（全量）
     * @visduo
     *
     * @return 部门列表
     */
    @Get
    @Mapping("/optionList")
    public ResponseResult optionList() {
        List<DeptEntity> deptList = easyEntityQuery.queryable(DeptEntity.class)
                .toList();

        return ResponseResult.success("查询成功", deptList);
    }

}
