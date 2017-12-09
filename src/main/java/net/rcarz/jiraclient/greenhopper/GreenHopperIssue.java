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

package net.rcarz.jiraclient.greenhopper;

import net.rcarz.jiraclient.Field;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.RestClient;
import net.sf.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * A base class for GreenHopper issues.
 */
public abstract class GreenHopperIssue extends GreenHopperResource {

    @Nullable
    private String key = null;
    private boolean hidden = false;
    @Nullable
    private String summary = null;
    @Nullable
    private String typeName = null;
    @Nullable
    private String typeId = null;
    @Nullable
    private String typeUrl = null;
    @Nullable
    private String priorityUrl = null;
    @Nullable
    private String priorityName = null;
    private boolean done = false;
    @Nullable
    private String assignee = null;
    @Nullable
    private String assigneeName = null;
    @Nullable
    private String avatarUrl = null;
    @Nullable
    private String colour = null;
    @Nullable
    private String statusId = null;
    @Nullable
    private String statusName = null;
    @Nullable
    private String statusUrl = null;
    @Nullable
    private List<Integer> fixVersions = null;
    private int projectId = 0;

    /**
     * Creates an issue from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected GreenHopperIssue(RestClient restclient, @Nullable JSONObject json) {
        super(restclient);

        if (json != null)
            deserialise(json);
    }

    private void deserialise(JSONObject json) {

        id = Field.getInteger(((Map) json).get("id"));
        key = Field.getString(((Map) json).get("key"));
        hidden = Field.getBoolean(((Map) json).get("hidden"));
        summary = Field.getString(((Map) json).get("summary"));
        typeName = Field.getString(((Map) json).get("key"));
        typeId = Field.getString(((Map) json).get("typeId"));
        typeUrl = Field.getString(((Map) json).get("typeUrl"));
        priorityUrl = Field.getString(((Map) json).get("priorityUrl"));
        priorityName = Field.getString(((Map) json).get("priorityName"));
        done = Field.getBoolean(((Map) json).get("done"));
        assignee = Field.getString(((Map) json).get("assignee"));
        assigneeName = Field.getString(((Map) json).get("assigneeName"));
        avatarUrl = Field.getString(((Map) json).get("avatarUrl"));
        colour = Field.getString(((Map) json).get("color"));
        statusId = Field.getString(((Map) json).get("statusId"));
        statusName = Field.getString(((Map) json).get("statusName"));
        statusUrl = Field.getString(((Map) json).get("statusUrl"));
        fixVersions = GreenHopperField.getIntegerArray(((Map) json).get("fixVersions"));
        projectId = Field.getInteger(((Map) json).get("projectId"));
    }

    /**
     * Retrieves the full JIRA issue.
     *
     * @return an Issue
     *
     * @throws JiraException when the retrieval fails
     */
    @NotNull
    public Issue getJiraIssue() throws JiraException {
        return Issue.get(restclient, key);
    }

    @Nullable
    @Override
    public String toString() {
        return key;
    }

    @Nullable
    public String getKey() {
        return key;
    }

    public Boolean isHidden() {
        return hidden;
    }

    @Nullable
    public String getSummary() {
        return summary;
    }

    @Nullable
    public String getTypeName() {
        return typeName;
    }

    @Nullable
    public String getTypeId() {
        return typeId;
    }

    @Nullable
    public String getTypeUrl() {
        return typeUrl;
    }

    @Nullable
    public String getPriorityUrl() {
        return priorityUrl;
    }

    @Nullable
    public String getPriorityName() {
        return priorityName;
    }

    public Boolean isDone() {
        return done;
    }

    @Nullable
    public String getAssignee() {
        return assignee;
    }

    @Nullable
    public String getAssigneeName() {
        return assigneeName;
    }

    @Nullable
    public String getAvatarUrl() {
        return avatarUrl;
    }

    @Nullable
    public String getColour() {
        return colour;
    }

    @Nullable
    public String getStatusId() {
        return statusId;
    }

    @Nullable
    public String getStatusName() {
        return statusName;
    }

    @Nullable
    public String getStatusUrl() {
        return statusUrl;
    }

    @Nullable
    public List<Integer> getFixVersions() {
        return fixVersions;
    }

    public int getProjectId() {
        return projectId;
    }
}

