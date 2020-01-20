package com.xumak.grid.service;

import com.xumak.grid.util.ErrorBean;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by j.amorataya on 3/9/16.
 */
public interface UserService {
    /**
     * Create Privilege.
     */
    String CREATE_PRIVILEGE = "C";
    /**
     * Read privilege.
     */
    String READ_PRIVILEGE = "R";
    /**
     * Update Privilege.
     */
    String UPDATE_PRIVILEGE = "U";
    /**
     * Delete privilege.
     */
    String DELETE_PRIVILEGE = "D";
    /**
     * Replicate privilege (crx:Replicate).
     */
    String REPLICATE_PRIVILEGE = "X";
    /**
     * READ ACL PRIVILEGE.
     */
    String READ_ACL_PRIVILEGE = "R*";
    /**
     * UPDATE ACL PRIVILEGE.
     */
    String UPDATE_ACL_PRIVILEGE = "U*";

    /**
     * Validate the action properties.
     *
     * @param config properties for the node
     * @return the list of error in json format otherwise return null
     */
    List<ErrorBean> validate(HashMap<String, Object> config);

    /**
     * Create an User.
     *
     * @param resource      resource to be used
     * @param configuration user configuration
     * @return status fo the operation
     */
    JSONObject createUser(Resource resource, HashMap<String, Object> configuration)
            throws JSONException;

    /**
     * Update an User.
     *
     * @param resource      resource to be used
     * @param configuration user configuration
     * @return status fo the operation
     */
    JSONObject updateUser(Resource resource, HashMap<String, Object> configuration)
            throws JSONException;

    /**
     * Change the password of the user.
     *
     * @param resource      resource to be used
     * @param configuration contains the elements.
     * @return status of the operation
     */
    JSONObject changePassword(Resource resource, HashMap<String, Object> configuration)
            throws JSONException;


    /**
     * Apply ACL to the user.
     *
     * @param resource       resource that contain the session.
     * @param userID         resource that contain the session.
     * @param path           path the resource to apply the ACL
     * @param privilegesList privilege List
     * @param isAllow        True for Allow/ False to Deny
     * @return result of operation
     */
    boolean addACL(Resource resource, String userID, String path, ArrayList<String> privilegesList, boolean isAllow);
}

