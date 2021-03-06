package io.jpom.service.build;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import io.jpom.common.BaseOperService;
import io.jpom.model.data.BuildModel;
import io.jpom.permission.BaseDynamicService;
import io.jpom.plugin.ClassFeature;
import io.jpom.system.ServerConfigBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 构建service
 *
 * @author bwcx_jzy
 * @date 2019/7/16
 **/
@Service
public class BuildService extends BaseOperService<BuildModel> implements BaseDynamicService {

    public BuildService() {
        super(ServerConfigBean.BUILD);
    }

    @Override
    public void updateItem(BuildModel buildModel) {
        buildModel.setModifyTime(DateUtil.now());
        super.updateItem(buildModel);
    }

    public boolean checkOutGiving(String outGivingId) {
        List<BuildModel> list = list();
        if (list == null || list.isEmpty()) {
            return false;
        }
        for (BuildModel buildModel : list) {
            if (buildModel.getReleaseMethod() == BuildModel.ReleaseMethod.Outgiving.getCode() &&
                    outGivingId.equals(buildModel.getReleaseMethodDataId())) {
                return true;
            }
        }
        return false;
    }

    public boolean checkNode(String nodeId) {
        List<BuildModel> list = list();
        if (list == null || list.isEmpty()) {
            return false;
        }
        for (BuildModel buildModel : list) {
            if (buildModel.getReleaseMethod() == BuildModel.ReleaseMethod.Project.getCode()) {
                String releaseMethodDataId = buildModel.getReleaseMethodDataId();
                if (StrUtil.startWith(releaseMethodDataId, nodeId + ":")) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkNodeProjectId(String nodeId, String projectId) {
        List<BuildModel> list = list();
        if (list == null || list.isEmpty()) {
            return false;
        }
        for (BuildModel buildModel : list) {
            if (buildModel.getReleaseMethod() == BuildModel.ReleaseMethod.Project.getCode()) {
                String releaseMethodDataId = buildModel.getReleaseMethodDataId();
                if (StrUtil.equals(releaseMethodDataId, nodeId + ":" + projectId)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public <E> List<E> list(Class<E> cls) {
        List<E> list = super.list(cls);
        JSONArray jsonArray = ((JSONArray) JSONArray.toJSON(list));
        jsonArray = filter(jsonArray, ClassFeature.BUILD);
        if (jsonArray == null) {
            return null;
        }
        return jsonArray.toJavaList(cls);
    }

    @Override
    public JSONArray listToArray(String dataId) {
        return (JSONArray) JSONArray.toJSON(this.list());
    }
}
