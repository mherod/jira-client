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
import net.rcarz.jiraclient.RestClient;
import net.sf.json.JSONObject;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a GreenHopper sprint.
 */
public class Sprint extends GreenHopperResource {

    @Nullable
    private String name = null;
    private boolean closed = false;
    @Nullable
    private DateTime startDate = null;
    @Nullable
    private DateTime endDate = null;
    @Nullable
    private DateTime completeDate = null;
    @Nullable
    private List<Integer> issuesIds = null;
    @Nullable
    private List<SprintIssue> issues = null;

    /**
     * Creates a sprint from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected Sprint(RestClient restclient, @Nullable JSONObject json) {
        super(restclient);

        if (json != null)
            deserialise(json);
    }

    private void deserialise(JSONObject json) {

        id = Field.getInteger(((Map) json).get("id"));
        name = Field.getString(((Map) json).get("name"));
        closed = json.containsValue("CLOSED");
        startDate = GreenHopperField.getDateTime(((Map) json).get("startDate"));
        endDate = GreenHopperField.getDateTime(((Map) json).get("endDate"));
        completeDate = GreenHopperField.getDateTime(((Map) json).get("completeDate"));
        issuesIds = GreenHopperField.getIntegerArray(((Map) json).get("issuesIds"));
    }

    @Nullable
    @Override
    public String toString() {
        return name;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public Boolean isClosed() {
        return closed;
    }

    @Nullable
    public DateTime getStartDate() {
        return startDate;
    }

    @Nullable
    public DateTime getEndDate() {
        return endDate;
    }

    @Nullable
    public DateTime getCompleteDate() {
        return completeDate;
    }

    @Nullable
    public List<SprintIssue> getIssues(){
        if(issues == null){
            issues = new ArrayList<SprintIssue>();
        }
        return issues;
    }

    @Nullable
    public List<Integer> getIssuesIds() {
        return issuesIds;
    }
}

