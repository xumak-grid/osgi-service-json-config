package com.xumak.grid.service;

import com.xumak.grid.util.ErrorBean;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by j.amorataya on 3/8/16.
 */

public interface ReplicationAgentService {

    /**
     * Validate the action properties.
     *
     * @param config properties for the node
     * @return the list of error in json format otherwise return null
     */
    List<ErrorBean> validate(HashMap<String, Object> config);


    /**
     * Create a replication Agent Page.
     *
     * @param resource      resource to get a PageManager
     * @param configuration configuration for the replication
     * @return status of the operation
     */
    JSONObject createReplicationAgent(Resource resource, HashMap<String, Object> configuration)
            throws JSONException;

    /**
     * Update Replication Agent.
     *
     * @param resource      Resource to get a pageManager
     * @param configuration map with the name and type of the replication page
     * @return status of the operation
     */
    JSONObject updateReplicationAgent(Resource resource, HashMap<String, Object> configuration)
            throws JSONException;

    /**
     * Delete Replication Agent.
     *
     * @param resource      Resource to get a pageManager
     * @param configuration map with the name and type of the replication page
     * @return the action status in json format
     */
    JSONObject deleteReplicationAgent(Resource resource, HashMap<String, Object> configuration)
            throws JSONException;

    /**
     * Get Replication Agent.
     *
     * @param resource      Resource to get a pageManager
     * @param configuration map with the name and type of the replication page
     * @return the action status in json format
     */
    JSONObject getReplicationAgent(Resource resource, HashMap<String, Object> configuration)
            throws JSONException;

}
