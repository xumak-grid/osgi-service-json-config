package com.xumak.grid.util;

import org.apache.sling.commons.json.JSONObject;

/**
 * Created by jesquivel on 9/29/17.
 */
public class StatusBean {
    private String code;
    private String option;
    private String msg;
    private JSONObject data;

    /***
     * Get Status code.
     * @return String status code.
     */
    public String getCode() {
        return code;
    }

    /***
     * Set Status code.
     * @param code request status code.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /***
     * Get Status option.
     * @return String status option.
     */
    public String getOption() {
        return option;
    }

    /***
     * Set Status option.
     * @param option request status option.
     */
    public void setOption(String option) {
        this.option = option;
    }

    /***
     * Get Status msg.
     * @return String status msg.
     */
    public String getMsg() {
        return msg;
    }

    /***
     * Set Status msg.
     * @param msg request status msg.
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /***
     * Get Status json object.
     * @return JSONObject status data.
     */
    public JSONObject getData() {
        return data;
    }

    /***
     * Set Status data.
     * @param data request status data.
     */
    public void setData(JSONObject data) {
        this.data = data;
    }
}
