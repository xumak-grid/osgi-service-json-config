package com.xumak.grid.util;

import org.apache.sling.commons.json.JSONObject;

/**
 * Created by jesquivel on 10/2/17.
 */
public final class DataBean {
    private JSONObject data;

    /**
     * Private Constructor.
     * @param data request Data JSONObject object.
     */
    private DataBean(final JSONObject data) {
        this.data = data;
    }

    /***
     * Get DataBean object.
     * @return JSONObject data object.
     */
    public JSONObject getData() {
        return data;
    }

    /***
     * Set DataBean object.
     * @param data request Data JSONObject object.
     */
    public void setData(JSONObject data) {
        this.data = data;
    }

    /***
     * Static method for create an ErrorBean object.
     *
     * @param data request error message.
     * @return DataBean object.
     */
    public static DataBean createData(final JSONObject data) {
        return new DataBean(data);
    }
}
