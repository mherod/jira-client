/**
 * jira-client - a simple JIRA REST client
 * Copyright (c) 2013 Bob Carroll (bob.carroll@alum.rit.edu)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.rcarz.jiraclient;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Represents an issue component.
 */
public class Component extends Resource {
    
    /**
     * Creates a new JIRA component.
     *
     * @param restclient REST client instance
     * @param project Key of the project to create the component in
     *
     * @return a fluent create instance
     */
    @NotNull
    public static FluentCreate create(RestClient restclient, String project) {
        return new FluentCreate(restclient, project);
    }

    @Nullable
    private String name = null;
    @Nullable
    private String description = null;
    private boolean isAssigneeTypeValid = false;

    /**
     * Creates a component from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected Component(RestClient restclient, @Nullable JSONObject json) {
        super(restclient);

        if (json != null)
            deserialise(json);
    }

    private void deserialise(JSONObject json) {

        self = Field.getString(((Map) json).get("self"));
        id = Field.getString(((Map) json).get("id"));
        name = Field.getString(((Map) json).get("name"));
        description = Field.getString(((Map) json).get("description"));
        isAssigneeTypeValid = Field.getBoolean(((Map) json).get("isAssigneeTypeValid"));
    }

    /**
     * Retrieves the given component record.
     *
     * @param restclient REST client instance
     * @param id Internal JIRA ID of the component
     *
     * @return a component instance
     *
     * @throws JiraException when the retrieval fails
     */
    public static Component get(@NotNull RestClient restclient, String id)
        throws JiraException {

        JSON result = null;

        try {
            result = restclient.get(getRestUri(id));
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve component " + id, ex);
        }

        if (!(result instanceof JSONObject))
            throw new JiraException("JSON payload is malformed");

        return new Component(restclient, (JSONObject)result);
    }

    @Nullable
    @Override
    public String toString() {
        return getName();
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public boolean isAssigneeTypeValid() {
        return isAssigneeTypeValid;
    }
    
    private static String getRestUri(@Nullable String id) {
        return getBaseUri() + "component/" + (id != null ? id : "");
    }

    /**
     * Used to chain fields to a create action.
     */
    public static final class FluentCreate {
        /**
         * The Jira rest client.
         */
        @Nullable
        RestClient restclient = null;

        /**
         * The JSON request that will be built incrementally as fluent methods
         * are invoked.
         */
        @NotNull
        final
        JSONObject req = new JSONObject();

        /**
         * Creates a new fluent.
         * @param restclient the REST client
         * @param project the project key
         */
        private FluentCreate(@Nullable RestClient restclient, String project) {
            this.restclient = restclient;
            req.put("project", project);
        }

        /**
         * Sets the name of the component.
         * @param name the name
         * @return <code>this</code>
         */
        @NotNull
        public FluentCreate name(String name) {
            req.put("name", name);
            return this;
        }

        /**
         * Sets the description of the component.
         * @param description the description
         * @return <code>this</code>
         */
        @NotNull
        public FluentCreate description(String description) {
            req.put("description", description);
            return this;
        }

        /**
         * Sets the lead user name.
         * @param leadUserName the lead user name
         * @return <code>this</code>
         */
        @NotNull
        public FluentCreate leadUserName(String leadUserName) {
            req.put("leadUserName", leadUserName);
            return this;
        }

        /**
         * Sets the assignee type.
         * @param assigneeType the assignee type
         * @return <code>this</code>
         */
        @NotNull
        public FluentCreate assigneeType(String assigneeType) {
            req.put("assigneeType", assigneeType);
            return this;
        }

        /**
         * Sets whether the assignee type is valid.
         * @param assigneeTypeValid is the assignee type valid?
         * @return <code>this</code>
         */
        @NotNull
        public FluentCreate assigneeTypeValue(boolean assigneeTypeValid) {
            req.put("isAssigneeTypeValid", assigneeTypeValid);
            return this;
        }

        /**
         * Executes the create action.
         * @return the created component
         *
         * @throws JiraException when the create fails
         */
        public Component execute() throws JiraException {
            JSON result = null;

            try {
                result = restclient.post(getRestUri(null), req);
            } catch (Exception ex) {
                throw new JiraException("Failed to create issue", ex);
            }

            if (!(result instanceof JSONObject) || !((JSONObject) result).containsKey("id")
                    || !(((JSONObject) result).get("id") instanceof String)) {
                throw new JiraException("Unexpected result on create component");
            }

            return new Component(restclient, (JSONObject) result);
        }
    }

    /**
     * Deletes a component from a project.
     * 
     * @throws JiraException failed to delete the component
     */
    public void delete() throws JiraException {
        try {
            restclient.delete(getRestUri(id));
        } catch (Exception ex) {
            throw new JiraException("Failed to delete component " + id, ex);
        }
    }
}

