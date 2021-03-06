package io.jpom.common.commander.impl;

import cn.hutool.core.util.StrUtil;
import io.jpom.common.commander.AbstractTomcatCommander;
import io.jpom.model.data.TomcatInfoModel;

/**
 * tomcat的linux管理命令
 *
 * @author LF
 */
public class LinuxTomcatCommander extends AbstractTomcatCommander {

    @Override
    public String execCmd(TomcatInfoModel tomcatInfoModel, String cmd) {
        String tomcatPath = tomcatInfoModel.pathAndCheck();
        if (StrUtil.isBlank(tomcatPath)) {
            return "tomcat path blank";
        }
        // 拼接命令
        String command = String.format("java -Djava.util.logging.config.file=%sconf/logging.properties " +
                        "-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager " +
                        "-Djdk.tls.ephemeralDHKeySize=2048 " +
                        "-Djava.protocol.handler.pkgs=org.apache.catalina.webresources " +
                        "-Dignore.endorsed.dirs=%s " +
                        "-classpath %sbin/bootstrap.jar:%sbin/tomcat-juli.jar " +
                        "-Dcatalina.base=%s " +
                        "-Dcatalina.home=%s " +
                        "-Djava.io.tmpdir=%stemp/ " +
                        "org.apache.catalina.startup.Bootstrap %s", tomcatPath, tomcatPath,
                tomcatPath, tomcatPath, tomcatPath,
                tomcatPath, tomcatPath, cmd);
        //
        exec(command, false);
        // 查询操作结果并返回
        return getStatus(tomcatInfoModel, cmd);
    }
}
