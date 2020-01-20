package com.xumak.grid.servlet;

import com.day.crx.packaging.JSONResponse;
import com.xumak.grid.service.OSGIConfigService;
import com.xumak.grid.service.ReplicationAgentService;
import com.xumak.grid.service.UserService;
import com.xumak.grid.util.Constants;
import com.xumak.grid.util.DataBean;
import com.xumak.grid.util.ErrorBean;
import com.xumak.grid.util.JSONUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
/**
 * Configuration by JSON Servlet.
 * -­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-­‐-‐
 * Change History
 * --------------------------------------------------------------------------------------
 * Version | Date       | Developer             | Changes
 * 1.0     | 2016/01/04 | J.Alejandro Morataya  | Initial Creation
 * --------------------------------------------------------------------------------------
 */

@Component(service = Servlet.class,
        property = {
                "sling.servlet.paths=/system/instanceConfiguration",
                "sling.servlet.extensions=html"
        }
)

public class ConfigurationByJSONServlet extends SlingAllMethodsServlet {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String statusCode = Constants.CODE_OK;
    private StringBuilder statusMsg;
    private List<ErrorBean> errors;
    private List<DataBean> data;

    @Reference
    private ReplicationAgentService replicationAgentService;

    @Reference
    private UserService userService;

    @Reference
    private OSGIConfigService osgiConfigurationService;

    @Override
    protected void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws IOException {

        //Get JSON input stream
        //Convert to JSON
        final JSONObject params = JSONUtils.getJsonFromReader(request.getReader());

        if (params.length() == Constants.JSON_IS_EMPTY) {
            response.setContentType(JSONResponse.APPLICATION_JSON_UTF8);
            response.getWriter().print(JSONUtils.getStatus(Constants.CODE_ERROR, "Invalid json", null));
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        this.statusMsg = new StringBuilder();
        this.data = new ArrayList<DataBean>();

        //select each element of the JSON

        for (final Iterator<String> iter = params.keys(); iter.hasNext();) {
            final String instance = iter.next();

            try {
                final HashMap<String, Object> instanceHash =
                        (HashMap<String, Object>) JSONUtils.jsonStringObjectToMap(params.get(instance).toString());

                /**
                 * Execute configuration
                 * OSGI Configuration
                 */

                if (instanceHash.containsKey(Constants.CONFIGURATION_NODE)) {

                    final ArrayList<JSONObject> configurationArray =
                            (ArrayList) instanceHash.get(Constants.CONFIGURATION_NODE);
                    final JSONArray array = new JSONArray();

                    for (final JSONObject osgiObject : configurationArray) {

                        final HashMap<String, Object> osgiConfiguration =
                                (HashMap<String, Object>) JSONUtils.jsonStringObjectToMap(osgiObject.toString());

                        /*
                            Validate option required properties.
                         */

                        errors = osgiConfigurationService.validate(osgiConfiguration);

                        if (!errors.isEmpty()) {
                            array.put(this.getErrorsFromValidation(osgiConfiguration));
                            continue;
                        }

                        array.put(osgiConfigurationService.createOSGIConfiguration(
                                request.getResource(),
                                osgiConfiguration)
                        );
                    }

                    data.add(DataBean.createData(new JSONObject().put(Constants.CONFIGURATION_NODE, array)));
                    this.statusMsg.append(String.format(
                            "Total: %s(%s)",
                            new String[]{Constants.CONFIGURATION_NODE, Integer.toString(configurationArray.size())})
                    );

                }

                /**
                 * User and ACLS configurations.
                 */

                if (instanceHash.containsKey(Constants.USERS_NODE)) {

                    final ArrayList<JSONObject> userArray = (ArrayList) instanceHash.get(Constants.USERS_NODE);
                    final JSONArray array = new JSONArray();

                    for (final JSONObject userJsonObject : userArray) {
                        final HashMap<String, Object> userConfig =
                                (HashMap<String, Object>) JSONUtils.jsonStringObjectToMap(userJsonObject.toString());


                        errors = userService.validate(userConfig);

                        if (!errors.isEmpty()) {

                            array.put(this.getErrorsFromValidation(userConfig));
                            continue;

                        }

                        final String policy = (String) userConfig.get(Constants.POLICY_CONFIG);

                        if (policy.equals(Constants.CHANGE_PASSWORD_POLICY)) {
                            array.put(userService.changePassword(request.getResource(), userConfig));

                        } else if (policy.equals(Constants.CREATE_POLICY)) {
                            array.put(userService.createUser(request.getResource(), userConfig));

                        } else if (policy.equals(Constants.UPDATE_POLICY)) {
                            array.put(userService.updateUser(request.getResource(), userConfig));
                        }

                    }

                    data.add(DataBean.createData(new JSONObject().put(Constants.USERS_NODE, array)));
                    this.statusMsg.append(String.format(
                            " %s(%s)",
                            new String[]{Constants.USERS_NODE, Integer.toString(userArray.size())})
                    );

                }

                /**
                 * END
                 * User and ACLS configurations.
                 *
                 */

                /**
                 * Replication Agent
                 * Begin
                 */

                if (instanceHash.containsKey(Constants.REPLICATION_NODE)) {

                    final ArrayList<JSONObject> replicationArray =
                            (ArrayList) instanceHash.get(Constants.REPLICATION_NODE);
                    final JSONArray array = new JSONArray();


                    for (final JSONObject replicationJson : replicationArray) {

                        final HashMap<String, Object> replicationConfig =
                                (HashMap<String, Object>) JSONUtils.jsonStringObjectToMap(replicationJson.toString());

                        /*
                            Validate option required properties.
                         */

                        errors = replicationAgentService.validate(replicationConfig);

                        if (!errors.isEmpty()) {

                            array.put(this.getErrorsFromValidation(replicationConfig));
                            continue;

                        }

                        final String policy = (String) replicationConfig.get(Constants.POLICY_CONFIG);

                        if (policy.equals(Constants.CREATE_POLICY)) {
                            array.put(replicationAgentService
                                    .createReplicationAgent(request.getResource(), replicationConfig));

                        } else if (policy.equals(Constants.UPDATE_POLICY)) {
                            array.put(replicationAgentService
                                    .updateReplicationAgent(request.getResource(), replicationConfig));

                        } else if (policy.equals(Constants.DELETE_POLICY)) {
                            array.put(replicationAgentService
                                    .deleteReplicationAgent(request.getResource(), replicationConfig));

                        } else if (policy.equals(Constants.SHOW_POLICY)) {
                            array.put(replicationAgentService
                                    .getReplicationAgent(request.getResource(), replicationConfig));

                        }

                    }

                    data.add(DataBean.createData(new JSONObject().put(Constants.REPLICATION_NODE, array)));

                    this.statusMsg.append(String.format(
                            " %s(%s)",
                            new String[]{Constants.REPLICATION_NODE, Integer.toString(replicationArray.size())})
                    );

                }

                /**
                 * Replication Agent
                 * End
                 */

            } catch (JSONException e) {
                logger.error("Error, configuration service: {}", e.getMessage());
                this.statusCode = Constants.CODE_ERROR;
                this.statusMsg = new StringBuilder(e.getMessage());
            }

        }

        response.setContentType(JSONResponse.APPLICATION_JSON_UTF8);
        response.getWriter().print(JSONUtils.getStatus(this.statusCode, this.statusMsg.toString(), data));
        response.setStatus(this.getHTTPCode(this.statusCode));

    }

    /***
     * Get the http code of the request.
     * @param statusCode request string
     * @return int http code
     */
    private int getHTTPCode(final String statusCode) {
        int value = HttpServletResponse.SC_OK;

        if (!statusCode.equals(Constants.CODE_OK)) {
            value = HttpServletResponse.SC_BAD_REQUEST;
        }

        return value;
    }

    /***
     * Get validation errors list.
     * @param replicationConfig request HashMap
     * @return parsed Json Object
     */
    private JSONObject getErrorsFromValidation(final HashMap<String, Object> replicationConfig) throws JSONException {
        final JSONObject jsonError = new JSONObject();

        for (final Iterator<String> iter = replicationConfig.keySet().iterator(); iter.hasNext();) {
            final String key = iter.next();
            jsonError.put(key, replicationConfig.get(key));
        }

        final JSONArray errorArray = new JSONArray();
        for (final ErrorBean item : errors) {
            errorArray.put(item.getError());
        }
        jsonError.put(Constants.ERROR_KEY, errorArray);

        return jsonError;
    }

}
