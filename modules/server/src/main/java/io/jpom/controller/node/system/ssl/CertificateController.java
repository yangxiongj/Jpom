package io.jpom.controller.node.system.ssl;

import io.jpom.common.BaseServerController;
import io.jpom.common.forward.NodeForward;
import io.jpom.common.forward.NodeUrl;
import io.jpom.common.interceptor.OptLog;
import io.jpom.model.log.UserOperateLogV1;
import io.jpom.plugin.ClassFeature;
import io.jpom.plugin.Feature;
import io.jpom.plugin.MethodFeature;
import io.jpom.service.system.WhitelistDirectoryService;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * 证书管理
 *
 * @author Arno
 */
@Controller
@RequestMapping(value = "/node/system/certificate")
@Feature(cls = ClassFeature.SSL)
public class CertificateController extends BaseServerController {

    @Resource
    private WhitelistDirectoryService whitelistDirectoryService;

    @RequestMapping(value = "/list.html", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    @Feature(method = MethodFeature.LIST)
    public String certificate() {
        List<String> jsonArray = whitelistDirectoryService.getCertificateDirectory(getNode());
        setAttribute("certificate", jsonArray);
        return "node/system/certificate";
    }


    /**
     * 保存证书
     *
     * @return json
     */
    @RequestMapping(value = "/saveCertificate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @OptLog(UserOperateLogV1.OptType.SaveCert)
    @Feature(method = MethodFeature.EDIT)
    public String saveCertificate() {
        if (ServletFileUpload.isMultipartContent(getRequest())) {
            return NodeForward.requestMultipart(getNode(), getMultiRequest(), NodeUrl.System_Certificate_saveCertificate).toString();
        }
        return NodeForward.request(getNode(), getRequest(), NodeUrl.System_Certificate_saveCertificate).toString();
    }


    /**
     * 证书列表
     */
    @RequestMapping(value = "/getCertList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @Feature(method = MethodFeature.LIST)
    public String getCertList() {
        return NodeForward.request(getNode(), getRequest(), NodeUrl.System_Certificate_getCertList).toString();
    }

    /**
     * 删除证书
     *
     * @param id id
     * @return json
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @OptLog(UserOperateLogV1.OptType.DelCert)
    @Feature(method = MethodFeature.DEL)
    public String delete(String id) {
        return NodeForward.request(getNode(), getRequest(), NodeUrl.System_Certificate_delete).toString();
    }


    /**
     * 导出证书
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @OptLog(UserOperateLogV1.OptType.ExportCert)
    @Feature(method = MethodFeature.DOWNLOAD)
    public void export(String id) {
        NodeForward.requestDownload(getNode(), getRequest(), getResponse(), NodeUrl.System_Certificate_export);
    }
}
