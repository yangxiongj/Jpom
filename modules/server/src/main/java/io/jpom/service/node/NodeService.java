package io.jpom.service.node;

import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.jiangzeyin.common.JsonMessage;
import com.alibaba.fastjson.JSONArray;
import io.jpom.common.BaseOperService;
import io.jpom.common.JpomManifest;
import io.jpom.common.forward.NodeForward;
import io.jpom.common.forward.NodeUrl;
import io.jpom.model.data.NodeModel;
import io.jpom.permission.BaseDynamicService;
import io.jpom.plugin.ClassFeature;
import io.jpom.system.ServerConfigBean;
import io.jpom.util.StringUtil;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 节点管理
 *
 * @author jiangzeyin
 * @date 2019/4/16
 */
@Service
public class NodeService extends BaseOperService<NodeModel> implements BaseDynamicService {

    private static final TimedCache<String, List<NodeModel>> TIMED_CACHE = new TimedCache<>(TimeUnit.MINUTES.toMillis(5));


    public NodeService() {
        super(ServerConfigBean.NODE);
    }

    /**
     * 获取所有节点 和节点下面的项目
     *
     * @return list
     */
    public List<NodeModel> listAndProject() {
        List<NodeModel> nodeModels = this.list();
        Iterator<NodeModel> iterator = nodeModels.iterator();
        while (iterator.hasNext()) {
            NodeModel nodeModel = iterator.next();
            if (!nodeModel.isOpenStatus()) {
                iterator.remove();
                continue;
            }
            try {
                // 获取项目信息不需要状态
                JSONArray jsonArray = NodeForward.requestData(nodeModel, NodeUrl.Manage_GetProjectInfo, JSONArray.class, "notStatus", "true");
                if (jsonArray != null) {
                    nodeModel.setProjects(jsonArray);
                } else {
                    iterator.remove();
                }
            } catch (Exception e) {
                iterator.remove();
            }
        }
        return nodeModels;
    }

    public String cacheNodeList(List<NodeModel> list) {
        String reqId = IdUtil.fastUUID();
        TIMED_CACHE.put(reqId, list);
        return reqId;
    }

    /**
     * 获取页面编辑的节点信息
     *
     * @param id id
     * @return list
     */
    public List<NodeModel> getNodeModel(String id) {
        return TIMED_CACHE.get(id);
    }

    public String addNode(NodeModel nodeModel, HttpServletRequest request) {
        if (!StringUtil.isGeneral(nodeModel.getId(), 2, 20)) {
            return JsonMessage.getString(405, "节点id不能为空并且2-20（英文字母 、数字和下划线）");
        }
        if (getItem(nodeModel.getId()) != null) {
            return JsonMessage.getString(405, "节点id已经存在啦");
        }
        String error = checkData(nodeModel);
        if (error != null) {
            return error;
        }
        JpomManifest jpomManifest = NodeForward.requestData(nodeModel, NodeUrl.Info, request, JpomManifest.class);
        if (jpomManifest == null) {
            return JsonMessage.getString(204, "节点连接失败，请检查节点是否在线");
        }
        addItem(nodeModel);
        return JsonMessage.getString(200, "操作成功");
    }

    public String updateNode(NodeModel nodeModel) throws Exception {
        NodeModel exit = getItem(nodeModel.getId());
        if (exit == null) {
            return JsonMessage.getString(405, "节点不存在");
        }
        String error = checkData(nodeModel);
        if (error != null) {
            return error;
        }
        updateItem(nodeModel);
        return JsonMessage.getString(200, "操作成功");
    }

    private String checkData(NodeModel nodeModel) {
        if (StrUtil.isEmpty(nodeModel.getName())) {
            return JsonMessage.getString(405, "节点名称 不能为空");
        }
        List<NodeModel> list = list();
        if (list != null) {
            for (NodeModel model : list) {
                if (model.getUrl().equalsIgnoreCase(nodeModel.getUrl()) && !model.getId().equalsIgnoreCase(nodeModel.getId())) {
                    return JsonMessage.getString(405, "已经存在相同的节点地址啦");
                }
            }
        }
        return null;
    }

    @Override
    public JSONArray listToArray(String dataId) {
        return (JSONArray) JSONArray.toJSON(this.list());
    }

    @Override
    public List<NodeModel> list() {
        return (List<NodeModel>) filter(super.list(), ClassFeature.NODE);
    }
}
