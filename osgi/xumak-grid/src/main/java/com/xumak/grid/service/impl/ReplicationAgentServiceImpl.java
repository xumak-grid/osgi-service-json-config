package com.xumak.grid.service.impl;

import com.adobe.granite.crypto.CryptoException;
import com.adobe.granite.crypto.CryptoSupport;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMException;
import com.xumak.grid.service.ReplicationAgentService;
import com.xumak.grid.util.Constants;
import com.xumak.grid.util.ErrorBean;
import com.xumak.grid.util.JSONUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by j.amorataya on 3/8/16.
 */
@Component(service = ReplicationAgentService.class)
public class ReplicationAgentServiceImpl implements ReplicationAgentService {

    @Reference
    private SlingRepository repository;
    @Reference
    private CryptoSupport cryptoSupport;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String TRANSPORT_PASSWORD_KEY = "transportPassword";

    @Override
    public List<ErrorBean> validate(HashMap<String, Object> config) {
        final List<ErrorBean> errors = new ArrayList<ErrorBean>();

        if (!config.containsKey(Constants.POLICY_CONFIG) || config.get(Constants.POLICY_CONFIG).toString().isEmpty()) {
            errors.add(ErrorBean.createError("Key 'policy' is required"));

        } else if (config.get(Constants.POLICY_CONFIG).equals(Constants.SHOW_POLICY)) {

            if (!config.containsKey(Constants.TYPE_KEY) || config.get(Constants.TYPE_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError(Constants.KEY_TYPE_IS_REQUIRED));
            }

            if (config.containsKey(Constants.NAME_KEY) && config.get(Constants.NAME_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError("Key 'name' is empty"));
            }

        } else if (config.get(Constants.POLICY_CONFIG).equals(Constants.DELETE_POLICY)) {

            if (!config.containsKey(Constants.TYPE_KEY) || config.get(Constants.TYPE_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError(Constants.KEY_TYPE_IS_REQUIRED));
            }

            if (!config.containsKey(Constants.NAME_KEY) || config.get(Constants.NAME_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError("Key 'name' is required"));
            }

        } else if (config.get(Constants.POLICY_CONFIG).equals(Constants.CREATE_POLICY)) {

            if (!config.containsKey(Constants.TYPE_KEY) || config.get(Constants.TYPE_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError(Constants.KEY_TYPE_IS_REQUIRED));
            }

            if (!config.containsKey(Constants.NAME_KEY) || config.get(Constants.NAME_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError("Key 'name' is required"));
            }

            if (!config.containsKey(Constants.WITH_KEY) || config.get(Constants.WITH_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError("Key 'with' is required"));
            }

        } else if (config.get(Constants.POLICY_CONFIG).equals(Constants.UPDATE_POLICY)) {

            if (!config.containsKey(Constants.TYPE_KEY) || config.get(Constants.TYPE_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError(Constants.KEY_TYPE_IS_REQUIRED));
            }

            if (!config.containsKey(Constants.NAME_KEY) || config.get(Constants.NAME_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError("Key 'name' is required"));
            }

            if (!config.containsKey(Constants.WITH_KEY) || config.get(Constants.WITH_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError("Key 'with' is required"));
            }

        } else {
            errors.add(ErrorBean.createError("Invalid key policy"));
        }

        return errors;
    }


    @Override
    public JSONObject createReplicationAgent(Resource resource, HashMap<String, Object> configuration)
            throws JSONException {

        final JSONObject detail = new JSONObject();
        final PageManager pageManager = resource.getResourceResolver().adaptTo(PageManager.class);

        final String type = (String) configuration.get(Constants.TYPE_KEY);
        final String name = (String) configuration.get(Constants.NAME_KEY);

        try {

            if (pageManager
                    .getPage(Constants.REPLICATION_PATH + getReplicationType(type) + Constants.SLASH + name) != null) {

                detail.put(Constants.POLICY_CONFIG, Constants.CREATE_POLICY);
                detail.put(Constants.TYPE_KEY, type);
                detail.put(Constants.NAME_KEY, name);
                detail.put(Constants.ERROR_KEY, new JSONArray().put("Resource already exist"));

            } else {

                final Session session = resource.getResourceResolver().adaptTo(Session.class);

                if (session != null && session.isLive()) {

                    final Page replicationPage = pageManager
                            .create(Constants.REPLICATION_PATH + getReplicationType(type),
                                    name,
                                    Constants.REPLICATION_TEMPLATE,
                                    name,
                                    true
                            );

                    if (replicationPage != null) {

                        final Resource contentResource = replicationPage.getContentResource();
                        final Node pageNode = contentResource.adaptTo(Node.class);

                        final HashMap<String, Object> propertyMap =
                                (HashMap<String, Object>) JSONUtils
                                        .jsonStringObjectToMap(configuration.get(Constants.WITH_KEY).toString());

                        for (final Map.Entry<String, Object> property : propertyMap.entrySet()) {
                            final Object value = property.getValue();
                            final String propertyName = property.getKey();
                            if (value instanceof String) {
                                if (propertyName.equals(TRANSPORT_PASSWORD_KEY)) {
                                    pageNode.setProperty(propertyName, cryptoSupport.protect((String) value));

                                } else {
                                    pageNode.setProperty(propertyName, (String) value);
                                }
                            } else if (value instanceof Boolean) {
                                pageNode.setProperty(propertyName, (Boolean) value);
                            } else if (value instanceof ArrayList) {
                                final ArrayList listValues = (ArrayList) value;
                                String listString[] = new String[listValues.size()];
                                listString = (String[]) listValues.toArray(listString);
                                pageNode.setProperty(propertyName, listString);
                            } else if (value instanceof Integer) {
                                pageNode.setProperty(propertyName, ((Integer) value).intValue());
                            } else if (value instanceof Long) {
                                pageNode.setProperty(propertyName, ((Long) value).longValue());
                            }

                        }

                    }

                    session.save();
                    session.refresh(true);

                    detail.put(Constants.POLICY_CONFIG, Constants.CREATE_POLICY);
                    detail.put(Constants.TYPE_KEY, type);
                    detail.put(Constants.NAME_KEY, name);
                    detail.put(Constants.OK_KEY, "Resource has been created");

                }

            }

        } catch (RepositoryException | WCMException | CryptoException e) {
            logger.error("Error, when creating the replication node. {}", e.getMessage());
        }

        return detail;
    }

    @Override
    public JSONObject updateReplicationAgent(Resource resource, HashMap<String, Object> configuration)
            throws JSONException {

        JSONObject detail = new JSONObject();
        final PageManager pageManager = resource.getResourceResolver().adaptTo(PageManager.class);
        final String name = (String) configuration.get(Constants.NAME_KEY);
        final String type = (String) configuration.get(Constants.TYPE_KEY);
        final Page replicationPage = pageManager
                .getPage(Constants.REPLICATION_PATH + getReplicationType(type) + Constants.SLASH + name);
        final Session session = resource.getResourceResolver().adaptTo(Session.class);

        try {

            if (replicationPage == null) {

                detail = this.getResourceErrror(configuration);

            } else {

                final Resource contentResource = replicationPage.getContentResource();
                final Node pageNode = contentResource.adaptTo(Node.class);

                final HashMap<String, Object> propertyWith =
                        (HashMap<String, Object>) JSONUtils
                                .jsonStringObjectToMap(configuration
                                        .get(Constants.WITH_KEY)
                                        .toString());

                for (final Iterator<String> iter = propertyWith.keySet().iterator(); iter.hasNext(); ) {
                    final String propertyName = iter.next();
                    final Object value = propertyWith.get(propertyName);

                    if (value instanceof String) {
                        if (propertyName.equals(TRANSPORT_PASSWORD_KEY)) {
                            pageNode.setProperty(propertyName, cryptoSupport.protect((String) value));

                        } else {
                            pageNode.setProperty(propertyName, (String) value);
                        }
                    } else if (value instanceof Boolean) {
                        pageNode.setProperty(propertyName, (Boolean) value);
                    } else if (value instanceof ArrayList) {
                        final ArrayList listValues = (ArrayList) value;
                        String listString[] = new String[listValues.size()];
                        listString = (String[]) listValues.toArray(listString);
                        pageNode.setProperty(propertyName, listString);
                    } else if (value instanceof Integer) {
                        pageNode.setProperty(propertyName, ((Integer) value).intValue());
                    } else if (value instanceof Long) {
                        pageNode.setProperty(propertyName, ((Long) value).longValue());
                    }
                }

                session.save();

                detail.put(Constants.POLICY_CONFIG, Constants.UPDATE_POLICY);
                detail.put(Constants.TYPE_KEY, type);
                detail.put(Constants.NAME_KEY, name);
                detail.put(Constants.OK_KEY, "Resource has been updated");

            }


        } catch (NullPointerException | RepositoryException | CryptoException e) {
            logger.error("Error, when updating replication node. {}", e.getMessage());
        }

        return detail;
    }

    @Override
    public JSONObject deleteReplicationAgent(Resource resource, HashMap<String, Object> configuration)
            throws JSONException {

        JSONObject detail = new JSONObject();
        final String name = (String) configuration.get(Constants.NAME_KEY);
        final String type = (String) configuration.get(Constants.TYPE_KEY);
        final String path = Constants.REPLICATION_PATH + getReplicationType(type) + Constants.SLASH + name;

        final Session session = resource.getResourceResolver().adaptTo(Session.class);

        try {

            final Node node = JcrUtils.getNodeIfExists(path, session);

            if (node != null) {

                node.remove();
                session.save();

                detail.put(Constants.POLICY_CONFIG, Constants.DELETE_POLICY);
                detail.put(Constants.NAME_KEY, name);
                detail.put(Constants.TYPE_KEY, type);
                detail.put(Constants.OK_KEY, "Resource has been deleted");

            } else {
                detail = this.getResourceErrror(configuration);
            }

        } catch (RepositoryException | JSONException e) {
            logger.error("Error, when deleting replication node. {}", e.getMessage());
        }

        return detail;

    }

    @Override
    public JSONObject getReplicationAgent(Resource resource, HashMap<String, Object> configuration)
            throws JSONException {

        JSONObject detail = new JSONObject();
        final String type = (String) configuration.get(Constants.TYPE_KEY);

        String name = null;

        if (configuration.containsKey(Constants.NAME_KEY)) {
            name = (String) configuration.get(Constants.NAME_KEY);
        }

        final Session session = resource.getResourceResolver().adaptTo(Session.class);

        try {

            if (name != null) {

                final Node node = JcrUtils
                        .getNodeIfExists(
                                Constants.REPLICATION_PATH + getReplicationType(type) + Constants.SLASH + name,
                                session
                        );

                if (node != null && node.hasNode(Node.JCR_CONTENT)) {

                    detail = this.addPagePropertiesToJSON(node.getNode(Node.JCR_CONTENT));

                } else {
                    detail = this.getResourceErrror(configuration);
                }

            } else {

                if (getReplicationType(type).isEmpty()) {
                    detail = this.getResourceErrror(configuration);

                } else {

                    final Node node = JcrUtils
                            .getNodeIfExists(Constants.REPLICATION_PATH + getReplicationType(type), session);

                    if (node != null && node.hasNodes()) {

                        final JSONArray jsonArray = new JSONArray();

                        for (final Iterator<Node> iter = JcrUtils.getChildNodes(node).iterator(); iter.hasNext(); ) {
                            final Node item = iter.next();
                            if (item != null && item.hasNode(Node.JCR_CONTENT)) {
                                jsonArray.put(this.addPagePropertiesToJSON(item.getNode(Node.JCR_CONTENT)));
                            }
                        }

                        detail.put(getReplicationType(type), jsonArray);

                    } else {
                        detail = this.getResourceErrror(configuration);
                    }

                }

            }

        } catch (RepositoryException | JSONException e) {
            logger.error("Error, when creating json object. {}", e.getMessage());
        }

        return detail;

    }

    /**
     * Return the page of the replication type publish or author.
     *
     * @param replicationType replication type
     * @return the replication Page name
     */
    private String getReplicationType(String replicationType) {
        String type = "";
        if (replicationType.equals(Constants.AUTHOR_TYPE)) {
            type = Constants.AUTHOR_PAGE;
        } else if (replicationType.equals(Constants.PUBLISH_TYPE)) {
            type = Constants.PUBLIC_PAGE;
        }
        return type;
    }

    /***
     * Get Replication agent properties to show.
     * @param node request Node
     * @return Replication agent properties in a Json Object
     */
    private JSONObject addPagePropertiesToJSON(final Node node) throws JSONException, RepositoryException {

        final JSONObject detail = new JSONObject();
        detail.put(Constants.NAME_KEY, node.getParent().getName());

        for (final Iterator<Property> iter = JcrUtils.getProperties(node).iterator(); iter.hasNext(); ) {
            final Property key = iter.next();

            if (!key.isMultiple()) {

                if (key.getValue().getType() == PropertyType.LONG) {
                    detail.put(key.getName(), key.getValue().getLong());
                } else if (key.getValue().getType() == PropertyType.BOOLEAN) {
                    detail.put(key.getName(), key.getValue().getBoolean());
                } else {
                    detail.put(key.getName(), key.getValue().toString());
                }

            } else {
                final JSONArray array = new JSONArray();
                for (final Value value : key.getValues()) {
                    array.put(value.getString());
                }
                detail.put(key.getName(), array);
            }
        }

        return detail;

    }

    /***
     * Get resource errors from configuration.
     * @param configuration request HashMap
     * @return all errors found in a Json Object
     */
    private JSONObject getResourceErrror(final HashMap<String, Object> configuration) throws JSONException {
        final JSONObject detail = new JSONObject();
        for (final Iterator<String> iter = configuration.keySet().iterator(); iter.hasNext(); ) {
            final String key = iter.next();
            detail.put(key, configuration.get(key));
        }

        detail.put(Constants.ERROR_KEY, new JSONArray().put("Resource not found"));

        return detail;

    }

}
