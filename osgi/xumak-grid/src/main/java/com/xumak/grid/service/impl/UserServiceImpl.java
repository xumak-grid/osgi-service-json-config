package com.xumak.grid.service.impl;

import com.day.cq.replication.Replicator;
import com.xumak.grid.jcr.GenericValue;
import com.xumak.grid.service.UserService;
import com.xumak.grid.util.Constants;
import com.xumak.grid.util.ErrorBean;
import com.xumak.grid.util.JSONUtils;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.security.Privilege;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by j.amorataya on 3/9/16.
 */
@Component(service = UserService.class)
public class UserServiceImpl implements UserService {

    private static final String USER_ID = "id";
    private static final String PASSWORD = "newPassword";
    private static final String USER_PASSWORD = "password";
    private static final String WITH_KEY = "with";
    private static final String ACL_KEY = "acls";
    private static final String DENY_RULE = "deny";
    private static final String ALLOW_RULE = "allow";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public List<ErrorBean> validate(HashMap<String, Object> config) {
        final List<ErrorBean> errors = new ArrayList<ErrorBean>();

        if (!config.containsKey(Constants.POLICY_CONFIG) || config.get(Constants.POLICY_CONFIG).toString().isEmpty()) {
            errors.add(ErrorBean.createError("Key 'policy' is required"));

        } else if (config.get(Constants.POLICY_CONFIG).equals(Constants.CHANGE_PASSWORD_POLICY)) {

            if (!config.containsKey(Constants.TYPE_KEY) || config.get(Constants.TYPE_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError(Constants.KEY_TYPE_IS_REQUIRED));
            }

            if (!config.containsKey(Constants.ID_KEY) || config.get(Constants.ID_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError("Key 'id' is required"));
            }

            if (!config.containsKey(Constants.NEW_PASSWORD_KEY)
                    || config.get(Constants.NEW_PASSWORD_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError("Key 'newPassword' is required"));
            }

        } else if (config.get(Constants.POLICY_CONFIG).equals(Constants.CREATE_POLICY)) {

            if (!config.containsKey(Constants.TYPE_KEY) || config.get(Constants.TYPE_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError(Constants.KEY_TYPE_IS_REQUIRED));
            }

            if (!config.containsKey(Constants.ID_KEY) || config.get(Constants.ID_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError("Key 'id' is required"));
            }

            if (!config.containsKey(Constants.PASSWORD_KEY)
                    || config.get(Constants.PASSWORD_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError("Key 'password' is required"));
            }

            if (!config.containsKey(WITH_KEY) || config.get(WITH_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError("Key 'with' is required"));
            }

            if (!config.containsKey(Constants.ACLS_KEY) || config.get(Constants.ACLS_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError("Key 'acls' is required"));
            }

        } else if (config.get(Constants.POLICY_CONFIG).equals(Constants.UPDATE_POLICY)) {

            if (!config.containsKey(Constants.TYPE_KEY) || config.get(Constants.TYPE_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError(Constants.KEY_TYPE_IS_REQUIRED));
            }

            if (!config.containsKey(Constants.ID_KEY) || config.get(Constants.ID_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError("Key 'id' is required"));
            }

            if (config.containsKey(Constants.PASSWORD_KEY) && config.get(Constants.PASSWORD_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError("Key 'password' is empty"));
            }

            if (!config.containsKey(WITH_KEY) || config.get(WITH_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError("Key 'with' is required"));
            }

            if (!config.containsKey(Constants.ACLS_KEY) || config.get(Constants.ACLS_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError("Key 'acls' is required"));
            }

        } else {
            errors.add(ErrorBean.createError("Invalid key policy"));
        }

        return errors;
    }


    @Override
    public JSONObject createUser(Resource resource, HashMap<String, Object> configuration)
            throws JSONException {

        final JSONObject detail = new JSONObject();
        final UserManager userManager = resource.getResourceResolver().adaptTo(UserManager.class);
        final Session session = resource.getResourceResolver().adaptTo(Session.class);
        final String userName = (String) configuration.get(USER_ID);
        final String userPassword = (String) configuration.get(USER_PASSWORD);

        if (userManager != null) {

            try {

                if (userManager.getAuthorizable(userName) != null) {
                    detail.put(Constants.POLICY_CONFIG, Constants.CREATE_POLICY);
                    detail.put(USER_ID, userName);
                    detail.put(Constants.ERROR_KEY, "User already exists");

                } else {

                    final User newUser = userManager.createUser(userName, userPassword);
                    final ValueFactory valueFactory = session.getValueFactory();

                    if (configuration.containsKey(WITH_KEY)) {

                        final HashMap<String, Object> propertyMap =
                                (HashMap<String, Object>) JSONUtils.jsonStringObjectToMap(
                                        configuration.get(WITH_KEY).toString());

                        for (final Map.Entry<String, Object> entry : propertyMap.entrySet()) {
                            newUser.setProperty(entry.getKey(), GenericValue.getValue(valueFactory, entry.getValue()));
                        }

                    }

                    if (configuration.containsKey(ACL_KEY)) {
                        final HashMap<String, Object> aclsMap = (HashMap<String, Object>)
                                JSONUtils.jsonStringObjectToMap(configuration.get(ACL_KEY).toString());

                        if (aclsMap.containsKey(DENY_RULE)) {
                            final HashMap<String, Object> denyRules = (HashMap<String, Object>)
                                    JSONUtils.jsonStringObjectToMap(
                                            aclsMap.get(DENY_RULE).toString()
                                    );

                            for (final Map.Entry<String, Object> denyEntry : denyRules.entrySet()) {

                                addACL(resource, userName, denyEntry.getKey(), (ArrayList) denyEntry.getValue(), false);
                            }
                        }

                        if (aclsMap.containsKey(ALLOW_RULE)) {
                            final HashMap<String, Object> allowRules = (HashMap<String, Object>)
                                    JSONUtils.jsonStringObjectToMap(
                                            aclsMap.get(ALLOW_RULE).toString()
                                    );

                            for (final Map.Entry<String, Object> allowEntry : allowRules.entrySet()) {
                                addACL(
                                        resource,
                                        userName,
                                        allowEntry.getKey(),
                                        (ArrayList) allowEntry.getValue(),
                                        true
                                );

                            }
                        }
                    }

                    session.save();

                    detail.put(Constants.POLICY_CONFIG, Constants.CREATE_POLICY);
                    detail.put(USER_ID, userName);
                    detail.put(Constants.OK_KEY, "User was created");

                }

            } catch (RepositoryException e) {
                logger.error("Error, when trying to create a user, {}", e.getMessage());
            }

        }

        return detail;
    }

    @Override
    public JSONObject updateUser(Resource resource, HashMap<String, Object> configuration)
            throws JSONException {

        final JSONObject detail = new JSONObject();
        final UserManager userManager = resource.getResourceResolver().adaptTo(UserManager.class);
        final Session session = resource.getResourceResolver().adaptTo(Session.class);
        final String userName = (String) configuration.get(USER_ID);

        if (userManager != null) {

            try {

                if (userManager.getAuthorizable(userName) == null) {
                    detail.put(Constants.POLICY_CONFIG, Constants.UPDATE_POLICY);
                    detail.put(USER_ID, userName);
                    detail.put(Constants.ERROR_KEY, "User doesn't exists");

                } else {

                    final User myUser = (User) userManager.getAuthorizable(userName);
                    final ValueFactory valueFactory = session.getValueFactory();

                    if (configuration.containsKey(WITH_KEY)) {

                        final HashMap<String, Object> propertyMap =
                                (HashMap<String, Object>) JSONUtils.jsonStringObjectToMap(
                                        configuration.get(WITH_KEY).toString());

                        for (final Map.Entry<String, Object> entry : propertyMap.entrySet()) {
                            myUser.setProperty(entry.getKey(), GenericValue.getValue(valueFactory, entry.getValue()));
                        }

                    }

                    if (configuration.containsKey(ACL_KEY)) {
                        final HashMap<String, Object> aclsMap = (HashMap<String, Object>)
                                JSONUtils.jsonStringObjectToMap(configuration.get(ACL_KEY).toString());

                        if (aclsMap.containsKey(DENY_RULE)) {
                            final HashMap<String, Object> denyRules = (HashMap<String, Object>)
                                    JSONUtils.jsonStringObjectToMap(
                                            aclsMap.get(DENY_RULE).toString()
                                    );

                            for (final Map.Entry<String, Object> denyEntry : denyRules.entrySet()) {

                                addACL(resource, userName, denyEntry.getKey(), (ArrayList) denyEntry.getValue(), false);
                            }
                        }

                        if (aclsMap.containsKey(ALLOW_RULE)) {
                            final HashMap<String, Object> allowRules = (HashMap<String, Object>)
                                    JSONUtils.jsonStringObjectToMap(
                                            aclsMap.get(ALLOW_RULE).toString()
                                    );

                            for (final Map.Entry<String, Object> allowEntry : allowRules.entrySet()) {
                                addACL(
                                        resource,
                                        userName,
                                        allowEntry.getKey(),
                                        (ArrayList) allowEntry.getValue(),
                                        true
                                );

                            }
                        }
                    }

                    session.save();

                    detail.put(Constants.POLICY_CONFIG, Constants.UPDATE_POLICY);
                    detail.put(USER_ID, userName);
                    detail.put(Constants.OK_KEY, "User was updated");

                }

            } catch (RepositoryException e) {
                logger.error("Error, when trying to updated a user, {}", e.getMessage());
            }

        }

        return detail;
    }

    @Override
    public JSONObject changePassword(Resource resource, HashMap<String, Object> configuration)
            throws JSONException {
        final JSONObject detail = new JSONObject();
        final Session session = resource.getResourceResolver().adaptTo(Session.class);
        final UserManager userManager = resource.getResourceResolver().adaptTo(UserManager.class);
        final String userID = (String) configuration.get(USER_ID);
        final String newPassword = (String) configuration.get(PASSWORD);

        try {

            if (userManager.getAuthorizable(userID) == null) {
                detail.put(Constants.POLICY_CONFIG, Constants.CHANGE_PASSWORD_POLICY);
                detail.put(USER_ID, userID);
                detail.put(Constants.ERROR_KEY, "User doesn't exists");

            } else {

                final User myUser = (User) userManager.getAuthorizable(userID);
                myUser.changePassword(newPassword);

                session.save();

                detail.put(Constants.POLICY_CONFIG, Constants.CHANGE_PASSWORD_POLICY);
                detail.put(Constants.ID_KEY, userID);
                detail.put(Constants.OK_KEY, "User password has been changed");

            }

        } catch (RepositoryException e) {
            logger.error("Error, when changing user password");
        }

        return detail;
    }

    @Override
    public boolean addACL(Resource resource, String userID, String path,
                          ArrayList<String> privilegesList,
                          boolean isAllow) {
        final UserManager userManager = resource.getResourceResolver().adaptTo(UserManager.class);
        final Session session = resource.getResourceResolver().adaptTo(Session.class);
        boolean status = false;
        try {

            final Authorizable myUser = userManager.getAuthorizable(userID);
            final Privilege[] privileges = AccessControlUtils.privilegesFromNames(session,
                    getPrivilegesNamesFromIDs(privilegesList));

            AccessControlUtils.addAccessControlEntry(session, path,
                    myUser.getPrincipal(),
                    privileges, isAllow);
            status = true;
        } catch (RepositoryException e) {
            logger.error("something goes wrong. {}", e.getMessage());
        }

        return status;
    }

    /**
     * Get String array with privileges ID and return the JCR_Privileges Names.
     *
     * @param privilegesIds String array with privileges
     * @return return JCR Privileges names Array
     */
    private String[] getPrivilegesNamesFromIDs(ArrayList<String> privilegesIds) {
        final ArrayList<String> privilegesNamesList = new ArrayList<>();

        for (final String privilegeID : privilegesIds) {

            switch (privilegeID) {
                case CREATE_PRIVILEGE:
                    privilegesNamesList.add(Privilege.JCR_WRITE);
                    privilegesNamesList.add(Privilege.JCR_NODE_TYPE_MANAGEMENT);
                    break;
                case READ_PRIVILEGE:
                    privilegesNamesList.add(Privilege.JCR_READ);
                    break;
                case UPDATE_PRIVILEGE:
                    privilegesNamesList.add(Privilege.JCR_MODIFY_PROPERTIES);
                    privilegesNamesList.add(Privilege.JCR_LOCK_MANAGEMENT);
                    privilegesNamesList.add(Privilege.JCR_VERSION_MANAGEMENT);
                    break;
                case DELETE_PRIVILEGE:
                    privilegesNamesList.add(Privilege.JCR_REMOVE_NODE);
                    break;
                case REPLICATE_PRIVILEGE:
                    privilegesNamesList.add(Replicator.REPLICATE_PRIVILEGE);
                    break;
                case READ_ACL_PRIVILEGE:
                    privilegesNamesList.add(Privilege.JCR_READ_ACCESS_CONTROL);
                    break;
                case UPDATE_ACL_PRIVILEGE:
                    privilegesNamesList.add(Privilege.JCR_MODIFY_ACCESS_CONTROL);
                    break;
                default:
                    break;

            }

        }
        String stringList[] = new String[privilegesNamesList.size()];
        stringList = privilegesNamesList.toArray(stringList);

        return stringList;

    }
}
