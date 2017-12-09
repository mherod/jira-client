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

import java.util.Date;
import java.util.Map;

/**
 * Represents an issue comment.
 */
public class Comment extends Resource {

    @Nullable
    private String issueKey = null;
    @Nullable
    private User author = null;
    @Nullable
    private String body = null;
    @Nullable
    private Date created = null;
    @Nullable
    private Date updated = null;
    @Nullable
    private User updatedAuthor = null;

    @Nullable
    public Visibility getVisibility() {
        return visibility;
    }

    @Nullable
    private Visibility visibility = null;

    /**
     * Creates a comment from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected Comment(RestClient restclient, @Nullable JSONObject json, @Nullable String issueKey) {
        super(restclient);

        this.issueKey = issueKey;
        if (json != null)
            deserialise(json);
    }

    private void deserialise(JSONObject json) {

        self = Field.getString(((Map) json).get("self"));
        id = Field.getString(((Map) json).get("id"));
        author = Field.getResource(User.class, ((Map) json).get("author"), restclient);
        body = Field.getString(((Map) json).get("body"));
        created = Field.getDateTime(((Map) json).get("created"));
        updated = Field.getDateTime(((Map) json).get("updated"));
        updatedAuthor = Field.getResource(User.class, ((Map) json).get("updatedAuthor"), restclient);
        Object obj = ((Map) json).get("visibility");
        visibility = Field.getResource(Visibility.class, ((Map) json).get("visibility"),restclient);
    }

    /**
     * Retrieves the given comment record.
     *
     * @param restclient REST client instance
     * @param issue Internal JIRA ID of the associated issue
     * @param id Internal JIRA ID of the comment
     *
     * @return a comment instance
     *
     * @throws JiraException when the retrieval fails
     */
    public static Comment get(@NotNull RestClient restclient, String issue, String id)
        throws JiraException {

        JSON result = null;

        try {
            result = restclient.get(getBaseUri() + "issue/" + issue + "/comment/" + id);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve comment " + id + " on issue " + issue, ex);
        }

        if (!(result instanceof JSONObject))
            throw new JiraException("JSON payload is malformed");

        return new Comment(restclient, (JSONObject)result, issue);
    }

    /**
     * Updates the comment body.
     *
     * @param body Comment text
     *
     * @throws JiraException when the comment update fails
     */
    public void update(String body) throws JiraException {
        update(body, null, null);
    }

    /**
     * Updates the comment body with limited visibility.
     *
     * @param body Comment text
     * @param visType Target audience type (role or group)
     * @param visName Name of the role or group to limit visibility to
     *
     * @throws JiraException when the comment update fails
     */
    public void update(String body, @Nullable String visType, @Nullable String visName)
        throws JiraException {

        JSONObject req = new JSONObject();
        req.put("body", body);

        if (visType != null && visName != null) {
            JSONObject vis = new JSONObject();
            vis.put("type", visType);
            vis.put("value", visName);

            req.put("visibility", vis);
        }

        JSON result = null;

        try {
            String issueUri = getBaseUri() + "issue/" + issueKey;
            result = restclient.put(issueUri + "/comment/" + id, req);
        } catch (Exception ex) {
            throw new JiraException("Failed add update comment " + id, ex);
        }

        if (!(result instanceof JSONObject)) {
            throw new JiraException("JSON payload is malformed");
        }

        deserialise((JSONObject) result);
    }

    @Nullable
    @Override
    public String toString() {
        return created + " by " + author;
    }

    @Nullable
    public User getAuthor() {
        return author;
    }

    @Nullable
    public String getBody() {
        return body;
    }

    @Nullable
    public Date getCreatedDate() {
        return created;
    }

    @Nullable
    public User getUpdateAuthor() {
        return updatedAuthor;
    }

    @Nullable
    public Date getUpdatedDate() {
        return updated;
    }
}

