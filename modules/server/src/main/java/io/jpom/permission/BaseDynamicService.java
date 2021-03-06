package io.jpom.permission;

import cn.jiangzeyin.common.spring.SpringUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.jpom.common.BaseServerController;
import io.jpom.model.BaseModel;
import io.jpom.model.data.UserModel;
import io.jpom.plugin.ClassFeature;
import io.jpom.service.user.RoleService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bwcx_jzy
 * @date 2019/8/15
 */
public interface BaseDynamicService {

    /***
     *  过滤角色数据
     * @param jsonArray 原array
     * @param classFeature 功能
     * @return 过滤后的，如果当前没有登录信息就不过滤
     */
    default JSONArray filter(JSONArray jsonArray, ClassFeature classFeature) {
        // 获取当前用户
        UserModel userModel = BaseServerController.getUserModel();
        if (jsonArray == null || userModel == null) {
            return jsonArray;
        }
        RoleService bean = SpringUtil.getBean(RoleService.class);
        Set<String> dynamicList = bean.getDynamicList(userModel, classFeature);
        if (dynamicList == null) {
            if (userModel.isSystemUser()) {
                // 系统管理全部权限
                return jsonArray;
            }
            return null;
        }
        List<Object> collect = jsonArray.stream().filter(o -> {
            JSONObject jsonObject = (JSONObject) o;
            String id = jsonObject.getString("id");
            return dynamicList.contains(id);
        }).collect(Collectors.toList());
        return (JSONArray) JSONArray.toJSON(collect);
    }

    /***
     *  过滤角色数据
     * @param list 原list
     * @param classFeature 功能
     * @return 过滤后的，如果当前没有登录信息就不过滤
     */
    default List<? extends BaseModel> filter(List<? extends BaseModel> list, ClassFeature classFeature) {
        // 获取当前用户
        UserModel userModel = BaseServerController.getUserModel();
        if (list == null || userModel == null) {
            return list;
        }
        RoleService bean = SpringUtil.getBean(RoleService.class);
        Set<String> dynamicList = bean.getDynamicList(userModel, classFeature);
        if (dynamicList == null) {
            if (userModel.isSystemUser()) {
                // 系统管理全部权限
                return list;
            }
            // 没有角色没有权限
            return null;
        }
        //
        return list.stream().filter(baseModel -> dynamicList.contains(baseModel.getId())).collect(Collectors.toList());
    }

    // -------------------------------------- 转换数据为tree

    /**
     * 查询动态数据的array
     *
     * @param dataId 上级数据id
     * @return array
     */
    JSONArray listToArray(String dataId);

    /**
     * 查询功能下面的所有动态数据
     *
     * @param classFeature 功能
     * @param roleId       角色id
     * @param dataId       上级数据id
     * @return tree array
     */
    default JSONArray listDynamic(ClassFeature classFeature, String roleId, String dataId) {
        JSONArray listToArray = listToArray(dataId);
        if (listToArray == null || listToArray.isEmpty()) {
            return null;
        }
        JSONArray jsonArray = new JSONArray();
        listToArray.forEach(obj -> {
            JSONObject jsonObject = new JSONObject();
            JSONObject data = (JSONObject) obj;
            String name = data.getString("name");
            String id = data.getString("id");
            jsonObject.put("title", name);
            jsonObject.put("id", id);
            RoleService bean = SpringUtil.getBean(RoleService.class);
            boolean doChildren = this.doChildren(classFeature, roleId, id, jsonObject);
            if (!doChildren) {
                List<String> checkList = bean.listDynamicData(roleId, classFeature);
                if (checkList != null && checkList.contains(id)) {
                    jsonObject.put("checked", true);
                }
            }
            jsonArray.add(jsonObject);
        });
        return jsonArray;
    }

    /**
     * 处理子级数据
     *
     * @param classFeature 功能
     * @param roleId       角色id
     * @param dataId       数据id
     * @param jsonObject   parent
     * @return 是否包含子级
     */
    default boolean doChildren(ClassFeature classFeature, String roleId, String dataId, JSONObject jsonObject) {
        Set<ClassFeature> children = DynamicData.getChildren(classFeature);
        if (children == null) {
            return false;
        }
        JSONArray childrens = new JSONArray();
        children.forEach(classFeature1 -> {
            RoleService bean = SpringUtil.getBean(RoleService.class);
            JSONArray jsonArray1 = bean.listDynamic(roleId, classFeature1, dataId);
            if (jsonArray1 == null || jsonArray1.isEmpty()) {
                return;
            }
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("children", jsonArray1);
            jsonObject1.put("title", classFeature1.getName());
            jsonObject1.put("id", classFeature1.name());
            childrens.add(jsonObject1);
        });
        if (!childrens.isEmpty()) {
            jsonObject.put("children", childrens);
        }
        return true;
    }

    // -------------------------------------- 转换数据为tree  end

    // -------------------------------------- 前端接收选中

    /**
     * 接收前端的值
     *
     * @param classFeatureListMap map
     * @param classFeature        功能
     * @param jsonArray           array
     * @return list
     */
    default List<String> parserValue(Map<ClassFeature, List<String>> classFeatureListMap, ClassFeature classFeature, JSONArray jsonArray) {
        if (jsonArray == null) {
            return null;
        }
        List<String> list = new ArrayList<>();
        jsonArray.forEach(o -> {
            JSONObject jsonObject = (JSONObject) o;
            JSONArray children = jsonObject.getJSONArray("children");
            if (children != null && !children.isEmpty()) {
                parserChildren(classFeature, classFeatureListMap, children);
            }
            list.add(jsonObject.getString("id"));
        });
        return list;
    }

    /**
     * 转换子级
     *
     * @param classFeature        功能
     * @param classFeatureListMap map
     * @param jsonArray           array
     */
    default void parserChildren(ClassFeature classFeature, Map<ClassFeature, List<String>> classFeatureListMap, JSONArray jsonArray) {
        Set<ClassFeature> children = DynamicData.getChildren(classFeature);
        if (children == null) {
            return;
        }
        Map<ClassFeature, JSONArray> jsonArrayMap = this.convertArray(jsonArray);
        for (ClassFeature child : children) {
            JSONArray jsonArray1 = jsonArrayMap.get(child);
            List<String> list = parserValue(classFeatureListMap, child, jsonArray1);
            classFeatureListMap.put(child, list);
        }
    }

    /**
     * 将二级数据转换为map
     *
     * @param jsonArray array
     * @return map
     */
    default Map<ClassFeature, JSONArray> convertArray(JSONArray jsonArray) {
        Map<ClassFeature, JSONArray> newMap = new HashMap<>();
        jsonArray.forEach(o -> {
            JSONObject jsonObject = (JSONObject) o;
            String id = jsonObject.getString("id");
            ClassFeature classFeature = ClassFeature.valueOf(id);
            newMap.put(classFeature, jsonObject.getJSONArray("children"));
        });
        return newMap;
    }

    // -------------------------------------- 前端接收选中-------------------- end
}
