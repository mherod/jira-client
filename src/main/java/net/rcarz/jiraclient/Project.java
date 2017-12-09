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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a JIRA project.
 */
public class Project extends Resource {

    @Nullable
    private Map<String, String> avatarUrls = null;
    @Nullable
    private String key = null;
    @Nullable
    private String name = null;
    @Nullable
    private String description = null;
    @Nullable
    private User lead = null;
    @Nullable
    private String assigneeType = null;
    @Nullable
    private List<Component> components = null;
    @Nullable
    private List<IssueType> issueTypes = null;
    @Nullable
    private List<Version> versions = null;
    @Nullable
    private Map<String, String> roles = null;
    @Nullable
    private ProjectCategory category = null;
    @Nullable
    private String email = null;

    /**
     * Creates a project from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected Project(RestClient restclient, @Nullable JSONObject json) {
        super(restclient);

        if (json != null)
            deserialise(json);
    }

    private void deserialise(JSONObject json) {

        self = Field.getString(((Map) json).get("self"));
        id = Field.getString(((Map) json).get("id"));
        avatarUrls = Field.getMap(String.class, String.class, ((Map) json).get("avatarUrls"));
        key = Field.getString(((Map) json).get("key"));
        name = Field.getString(((Map) json).get("name"));
        description = Field.getString(((Map) json).get("description"));
        lead = Field.getResource(User.class, ((Map) json).get("lead"), restclient);
        assigneeType = Field.getString(((Map) json).get("assigneeType"));
        components = Field.getResourceArray(Component.class, ((Map) json).get("components"), restclient);
        issueTypes = Field.getResourceArray(
            IssueType.class,
            json.containsKey("issueTypes") ? ((Map) json).get("issueTypes") : ((Map) json).get("issuetypes"),
            restclient);
        versions = Field.getResourceArray(Version.class, ((Map) json).get("versions"), restclient);
        roles = Field.getMap(String.class, String.class, ((Map) json).get("roles"));
        category = Field.getResource(ProjectCategory.class, ((Map) json).get( "projectCategory" ), restclient);
        email = Field.getString( ((Map) json).get("email"));
    }

    /**
     * Retrieves the given project record.
     *
     * @param restclient REST client instance
     * @param key Project key
     *
     * @return a project instance
     *
     * @throws JiraException when the retrieval fails
     */
    public static Project get(@NotNull RestClient restclient, String key)
        throws JiraException {

        JSON result = null;

        try {
            result = restclient.get(getBaseUri() + "project/" + key);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve project " + key, ex);
        }

        if (!(result instanceof JSONObject))
            throw new JiraException("JSON payload is malformed");

        return new Project(restclient, (JSONObject)result);
    }

    /**
     * Retrieves all project records visible to the session user.
     *
     * @param restclient REST client instance
     *
     * @return a list of projects
     *
     * @throws JiraException when the retrieval fails
     */
    @NotNull
    public static List<Project> getAll(@NotNull RestClient restclient) throws JiraException {
        JSON result = null;

        try {
            result = restclient.get(getBaseUri() + "project");
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve projects", ex);
        }

        if (!(result instanceof JSONArray))
            throw new JiraException("JSON payload is malformed");

        return Field.getResourceArray(Project.class, result, restclient);
    }

    @NotNull
    public List<User> getAssignableUsers() throws JiraException {
        JSON result = null;

        try {			
            Map<String, String> queryParams = new HashMap<String, String>();
            queryParams.put("project", this.key);
            URI searchUri = restclient.buildURI(getBaseUri() + "user/assignable/search", queryParams);
            result = restclient.get(searchUri);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve assignable users", ex);
        }

        if (!(result instanceof JSONArray))
            throw new JiraException("JSON payload is malformed");

        return Field.getResourceArray(User.class, result, restclient);
    }

    @Nullable
    @Override
    public String toString() {
        return getName();
    }

    @Nullable
    public Map<String, String> getAvatarUrls() {
        return avatarUrls;
    }

    @Nullable
    public String getKey() {
        return key;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @Nullable
    public User getLead() {
        return lead;
    }

    @Nullable
    public String getAssigneeType() {
        return assigneeType;
    }

    @Nullable
    public List<Component> getComponents() {
        return components;
    }

    @Nullable
    public List<IssueType> getIssueTypes() {
        return issueTypes;
    }

    @Nullable
    public List<Version> getVersions() {
        return versions;
    }

    @Nullable
    public Map<String, String> getRoles() {
        return roles;
    }

    @Nullable
    public ProjectCategory getCategory() {
        return category;
    }

    @Nullable
    public String getEmail() {
        return email;
    }
}

