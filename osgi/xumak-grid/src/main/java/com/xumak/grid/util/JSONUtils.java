package com.xumak.grid.util;

import com.google.common.collect.Lists;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * JSON Utils.
 * -­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-‐
 * Change History
 * --------------------------------------------------------------------------------------
 * Version | Date       | Developer             | Changes
 * 1.0     | 2016/03/08 | J.Alejandro Morataya  | Initial Creation
 * --------------------------------------------------------------------------------------
 */
public final class JSONUtils {

    private static final Logger LOG = LoggerFactory.getLogger(JSONUtils.class);

    /**
     * Private Constructor.
     */
    private JSONUtils() {

    }

    /**
     * Get a map from JSONString.
     *
     * @param jsonStringObject string Object
     * @return Map from JsonString
     */
    public static Map jsonStringObjectToMap(final String jsonStringObject) {
        //for each json object, parse it and put the properties in a map
        final HashMap<String, Object> properties = new HashMap();
        try {
            final JSONObject jsonObject = new JSONObject(jsonStringObject);
            final Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                final String key = keys.next();
                Object value = jsonObject.get(key);
                if (value instanceof JSONArray) {
                    final JSONArray array = (JSONArray) value;
                    final List<Object> tempList = Lists.newArrayList();
                    for (int i = 0; i < array.length(); i++) {
                        tempList.add(array.get(i));
                    }
                    value = tempList;
                }
                properties.put(key, value);
            }
        } catch (JSONException e) {
            LOG.error("Error, when trying to map a json, {}", e.getMessage());
        }

        return properties;
    }

    /***
     * GetJson From Reader.
     * @param reader request buffered reader
     * @return parsed Json Object
     */
    public static JSONObject getJsonFromReader(final BufferedReader reader) {
        /**
         * Stringbuffer.
         */
        final StringBuffer jb = new StringBuffer();
        JSONObject jsonObject1 = new JSONObject();
        String line = null;
        try {
            line = reader.readLine();
            while (line != null) {
                jb.append(line);
                line = reader.readLine();
            }

            jsonObject1 = new JSONObject(jb.toString());


        } catch (JSONException | IOException e) {
            LOG.error("Error, when trying to read the json, {}", e.getMessage());
        }

        return jsonObject1;
    }

    /***
     * Return json status.
     * @param code request status code
     * @param msg request status msg
     * @param data request status data
     * @return String
     */
    public static String getStatus(final String code, final String msg, final List<DataBean> data) {
        final JSONObject json = new JSONObject();

        try {
            json.put("status", code);
            json.put("message", msg.trim());

            if (data != null) {

                final JSONObject jsonItme = new JSONObject();
                for (final DataBean item : data) {
                    jsonItme.put(item.getData().keys().next(), item.getData().get(item.getData().keys().next()));

                }
                json.put("data", jsonItme);
            }

        } catch (JSONException e) {
            LOG.error("Error, when creating json status {}, {}", code, e.getMessage());
        }

        return json.toString();

    }


}
