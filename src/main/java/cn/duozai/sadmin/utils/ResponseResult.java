package cn.duozai.sadmin.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果类
 * @visduo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseResult {

    /**
     * 响应码
     * 200成功/-1失败/500异常/401未登录/403无权限/404不存在/400参数错误
     */
    public Integer code;

    /**
     * 响应信息
     */
    public String message;

    /**
     * 响应数据
     */
    public Object data;

    /**
     * 响应成功
     * @visduo
     *
     * @param message 响应信息
     * @param data 响应数据
     * @return 响应结果实体
     */
    public static ResponseResult success(String message, Object data) {
        return new ResponseResult(200, message, data);
    }

    /**
     * 响应失败
     * @visduo
     *
     * @param message 响应信息
     * @param data 响应数据
     * @return 响应结果实体
     */
    public static ResponseResult failure(String message, Object data) {
        return new ResponseResult(-1, message, data);
    }

    /**
     * 响应异常
     * @visduo
     *
     * @param message 响应信息
     * @param data 响应数据
     * @return 响应结果实体
     */
    public static ResponseResult error(String message, Object data) {
        return new ResponseResult(500, message, data);
    }

}
