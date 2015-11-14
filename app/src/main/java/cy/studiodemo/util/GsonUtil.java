package cy.studiodemo.util;


import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GsonUtil {

    public static <T> T json2bean(String result, Class<T> clazz) {
        Gson gson = new Gson();
        T t = gson.fromJson(result, clazz);
        return t;
    }

    /**
     * 解析json数据为实体集合
     *
     * @param jsonArray json数组
     * @param clazz     对应实体类字节码
     * @return 返回对应实体集合
     * @throws Exception
     */
    public static <T> List<T> parseDataToCollection(JSONArray jsonArray, Class<T> clazz) throws Exception {

        Gson gson = new Gson();
        if (jsonArray == null || jsonArray.length() == 0) {
            return null;
        }
        List<T> data = new ArrayList<T>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject entityObj = jsonArray.getJSONObject(i);
            T entity = gson.fromJson(entityObj.toString(), clazz);
            data.add(entity);
        }
        return data;
    }
}
