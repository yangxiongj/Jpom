package io.jpom;

import cn.jiangzeyin.common.EnableCommonBoot;
import cn.jiangzeyin.common.spring.event.ApplicationEventLoad;
import io.jpom.common.Type;
import io.jpom.common.interceptor.LoginInterceptor;
import io.jpom.common.interceptor.PermissionInterceptor;
import io.jpom.permission.CacheControllerFeature;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * jpom 启动类
 *
 * @author jiangzeyin
 * @date 2017/9/14
 */
@SpringBootApplication(scanBasePackages = {"io.jpom"})
@ServletComponentScan
@EnableCommonBoot
public class JpomServerApplication implements ApplicationEventLoad {


    /**
     * 启动执行
     *
     * @param args 参数
     */
    public static void main(String[] args) throws Exception {
        JpomApplication jpomApplication = new JpomApplication(Type.Server, JpomServerApplication.class, args);
        jpomApplication
                // 拦截器
                .addInterceptor(LoginInterceptor.class)
                .addInterceptor(PermissionInterceptor.class)
                .run(args);
    }


    @Override
    public void applicationLoad() {
        CacheControllerFeature.init();
    }
}
