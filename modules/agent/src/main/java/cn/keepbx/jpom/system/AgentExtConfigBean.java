package cn.keepbx.jpom.system;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.jiangzeyin.common.spring.SpringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * agent 端外部配置
 *
 * @author jiangzeyin
 * @date 2019/4/16
 */
@Configuration
public class AgentExtConfigBean {

    private static AgentExtConfigBean agentExtConfigBean;
    /**
     * 白名单路径是否判断包含关系
     */
    @Value("${whitelistDirectory.checkStartsWith:true}")
    public boolean whitelistDirectoryCheckStartsWith;

    /**
     * 自动备份控制台日志，防止日志文件过大，目前暂只支持linux 不停服备份  如果配置none 则不自动备份 默认10分钟扫描一次
     */
    @Value("${log.autoBackConsoleCron:0 0/10 * * * ?}")
    public String autoBackConsoleCron;
    /**
     * 当文件多大时自动备份
     *
     * @see ch.qos.logback.core.util.FileSize
     */
    @Value("${log.autoBackSize:50MB}")
    public String autoBackSize;
    /**
     * 控制台日志保存时长单位天
     */
    @Value("${log.saveDays:7}")
    private int logSaveDays;

    @Value("${jpom.agent.id:}")
    private String agentId;

    @Value("${jpom.agent.url:}")
    private String agentUrl;

    @Value("${jpom.server.url:}")
    private String serverUrl;

    @Value("${jpom.server.token:}")
    private String serverToken;

    public String getAgentId() {
        return agentId;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getServerToken() {
        return serverToken;
    }

    /**
     * 获取当前的url
     *
     * @return 如果没有配置将自动生成：http://+本地IP+端口
     */
    public String getAgentUrl() {
        if (StrUtil.isEmpty(agentUrl)) {
            String localhostStr = NetUtil.getLocalhostStr();
            int port = ConfigBean.getInstance().getPort();
            agentUrl = String.format("http://%s:%s", localhostStr, port);
        }
        if (StrUtil.isEmpty(agentUrl)) {
            throw new JpomRuntimeException("获取Agent url失败");
        }
        return agentUrl;
    }

    /**
     * 创建请求对象
     *
     * @param openApi url
     * @return HttpRequest
     * @see cn.keepbx.jpom.common.ServerOpenApi
     */
    public HttpRequest createServerRequest(String openApi) {
        if (StrUtil.isEmpty(serverUrl)) {
            throw new JpomRuntimeException("请先配置server端url");
        }
        if (StrUtil.isEmpty(serverToken)) {
            throw new JpomRuntimeException("请先配置server端Token");
        }
        HttpRequest httpRequest = HttpUtil.createPost(String.format("%s%s", serverUrl, openApi));
        httpRequest.header("JPOM-TOKEN", serverToken);
        return httpRequest;
    }

    /**
     * 配置错误或者没有，默认是7天
     *
     * @return int
     */
    public int getLogSaveDays() {
        if (logSaveDays <= 0) {
            return 7;
        }
        return logSaveDays;
    }

    /**
     * 单例
     *
     * @return this
     */
    public static AgentExtConfigBean getInstance() {
        if (agentExtConfigBean == null) {
            agentExtConfigBean = SpringUtil.getBean(AgentExtConfigBean.class);
        }
        return agentExtConfigBean;
    }
}
