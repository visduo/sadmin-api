package cn.duozai.sadmin.filter;

import cn.duozai.sadmin.utils.ResponseResult;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.exception.StatusException;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;
import org.noear.solon.validation.ValidatorException;

/**
 * 统一异常处理过滤器
 * @visduo
 *
 * 参考文档：https://solon.noear.org/article/765
 */
@Component
public class ExceptionFilter implements Filter {

    /**
     * 统一异常处理过滤
     *
     * @param ctx 上下文对象
     * @param chain 过滤器链
     * @throws Throwable 抛出异常对象
     */
    @Override
    public void doFilter(Context ctx, FilterChain chain) throws Throwable {
        try {
            // 放行操作
            chain.doFilter(ctx);
        } catch (ValidatorException e){
            // ValidatorException：请求参数校验异常
            // 参考文档：https://solon.noear.org/article/49
            // 封装返回400状态
            ctx.render(new ResponseResult(400, e.getMessage(), null));
        } catch (StatusException e){
            // StatusException：请求状态异常
            // 参考文档：https://solon.noear.org/article/871
            // 封装返回对应状态
            ctx.render(new ResponseResult(e.getCode(), e.getMessage(), null));
        } catch (Throwable e) {
            // 封装返回500状态
            ctx.render(new ResponseResult(500, e.getMessage(), null));
        }
    }

}
