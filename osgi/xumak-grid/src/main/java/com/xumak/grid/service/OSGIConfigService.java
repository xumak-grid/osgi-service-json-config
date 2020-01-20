package com.xumak.grid.service;

import com.xumak.grid.util.ErrorBean;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by j.amorataya on 3/10/16.
 */
public interface OSGIConfigService {


    /**
     * Validate the action properties.
     *
     * @param config properties for the node
     * @return the list of error in json format otherwise return null
     */
    List<ErrorBean> validate(HashMap<String, Object> config);

    /**
     * Create a OSGI configuration node.
     *
     * @param resource      resource to get the Session
     * @param configuration properties for the node
     * @return the action status in json format
     */
    JSONObject createOSGIConfiguration(Resource resource, HashMap<String, Object> configuration)
            throws JSONException;
}
