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
 * Represents an issue work log.
 */
public class WorkLog extends Resource {

    @Nullable
    private User author = null;
    @Nullable
    private String comment = null;
    @Nullable
    private Date created = null;
    @Nullable
    private Date updated = null;
    @Nullable
    private User updateAuthor = null;
    @Nullable
    private Date started = null;
    @Nullable
    private String timeSpent = null;
    private int timeSpentSeconds = 0;

    /**
     * Creates a work log from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected WorkLog(RestClient restclient, @Nullable JSONObject json) {
        super(restclient);

        if (json != null)
            deserialise(json);
    }

    private void deserialise(JSONObject json) {

        self = Field.getString(((Map) json).get("self"));
        id = Field.getString(((Map) json).get("id"));
        author = Field.getResource(User.class, ((Map) json).get("author"), restclient);
        comment = Field.getString(((Map) json).get("comment"));
        created = Field.getDateTime(((Map) json).get("created"));
        updated = Field.getDateTime(((Map) json).get("updated"));
        updateAuthor = Field.getResource(User.class, ((Map) json).get("updateAuthor"), restclient);
        started = Field.getDateTime(((Map) json).get("started"));
        timeSpent = Field.getString(((Map) json).get("timeSpent"));
        timeSpentSeconds = Field.getInteger(((Map) json).get("timeSpentSeconds"));
    }

    /**
     * Retrieves the given work log record.
     *
     * @param restclient REST client instance
     * @param issue Internal JIRA ID of the associated issue
     * @param id Internal JIRA ID of the work log
     *
     * @return a work log instance
     *
     * @throws JiraException when the retrieval fails
     */
    public static WorkLog get(@NotNull RestClient restclient, String issue, String id)
        throws JiraException {

        JSON result = null;

        try {
            result = restclient.get(getBaseUri() + "issue/" + issue + "/worklog/" + id);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve work log " + id + " on issue " + issue, ex);
        }

        if (!(result instanceof JSONObject))
            throw new JiraException("JSON payload is malformed");

        return new WorkLog(restclient, (JSONObject)result);
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
    public String getComment() {
        return comment;
    }

    @Nullable
    public Date getCreatedDate() {
        return created;
    }

    @Nullable
    public User getUpdateAuthor() {
        return updateAuthor;
    }

    @Nullable
    public Date getUpdatedDate() {
        return updated;
    }

    @Nullable
    public Date getStarted(){ return started; }

    @Nullable
    public String getTimeSpent(){ return timeSpent; }

    public int getTimeSpentSeconds() {
        return timeSpentSeconds;
    }

}

