package com.xumak.grid.service.impl;

import com.xumak.grid.service.OSGIConfigService;
import com.xumak.grid.util.Constants;
import com.xumak.grid.util.ErrorBean;
import com.xumak.grid.util.JSONUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by j.amorataya on 3/10/16.
 */
@Component(service = OSGIConfigService.class)
public class OSGIConfigServiceImpl implements OSGIConfigService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public List<ErrorBean> validate(HashMap<String, Object> config) {
        final List<ErrorBean> errors = new ArrayList<>();

        if (!config.containsKey(Constants.POLICY_CONFIG) || config.get(Constants.POLICY_CONFIG).toString().isEmpty()) {
            errors.add(ErrorBean.createError("Key 'policy' is required"));

        } else if (config.get(Constants.POLICY_CONFIG).equals(Constants.UPDATE_CREATE_POLICY)) {

            if (!config.containsKey(Constants.TYPE_KEY) || config.get(Constants.TYPE_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError("Key 'type' is required"));
            }

            if (!config.containsKey(Constants.ID_KEY) || config.get(Constants.ID_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError("Key 'id' is required"));
            }

            if (!config.containsKey(Constants.PATH_KEY) || config.get(Constants.PATH_KEY).toString().isEmpty()) {
                errors.add(ErrorBean.createError("Key 'path' is required"));
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
    public JSONObject createOSGIConfiguration(Resource resource, HashMap<String, Object> configuration)
            throws JSONException {

        JSONObject detail = new JSONObject();
        final ResourceResolver resourceResolver = resource.getResourceResolver();
        //Create session (Required by AEM6)
        final Session session = resourceResolver.adaptTo(Session.class);

        final String policy = (String) configuration.get(Constants.POLICY_CONFIG);
        final String path = (String) configuration.get(Constants.PATH_KEY);
        final String id = (String) configuration.get(Constants.ID_KEY);

        final HashMap<String, Object> properties = (HashMap<String, Object>) JSONUtils.
                jsonStringObjectToMap(configuration.get(Constants.WITH_KEY).toString());

        try {
            if (session.nodeExists(path)) {

                final Node baseNode = session.getNode(path);

                final Node configNode;

                if (baseNode.hasNode(id) && policy.equals(Constants.UPDATE_CREATE_POLICY)) {

                    configNode = baseNode.getNode(id);

                } else {
                    configNode = baseNode.addNode(id, Constants.OSGI_CONFIG_TYPE);
                }

                //Update properties

                final Iterator itProperties = properties.entrySet().iterator();
                while (itProperties.hasNext()) {
                    final Map.Entry pair = (Map.Entry) itProperties.next();
                    final String name = (String) pair.getKey();
                    final Object value = pair.getValue();

                    if (value instanceof String) {
                        configNode.setProperty(name, (String) value);
                    } else if (value instanceof Boolean) {
                        configNode.setProperty(name, (Boolean) value);
                    } else if (value instanceof Integer) {
                        configNode.setProperty(name, (Integer) value);
                    } else if (value instanceof ArrayList) {

                        final ArrayList listValues = (ArrayList) value;
                        String listString[] = new String[listValues.size()];
                        listString = (String[]) listValues.toArray(listString);
                        configNode.setProperty(name, listString);
                    }

                }

                session.save();

                detail.put(Constants.POLICY_CONFIG, Constants.UPDATE_CREATE_POLICY);
                detail.put(Constants.PATH_KEY, path);
                detail.put(Constants.ID_KEY, id);
                detail.put(Constants.OK_KEY, "OSGi Config has been created");

            } else {
                detail = this.getResourceErrror(configuration);
            }


        } catch (RepositoryException e) {
            logger.error("Error, when creating osgi config. {}", e.getMessage());
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
        for (final Iterator<String> iter = configuration.keySet().iterator(); iter.hasNext();) {
            final String key = iter.next();
            detail.put(key, configuration.get(key));
        }

        detail.put(Constants.ERROR_KEY, new JSONArray().put("Resource not found"));

        return detail;

    }

}
