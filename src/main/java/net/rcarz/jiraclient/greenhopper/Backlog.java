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
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.RestClient;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GreenHopper backlog data.
 */
public class Backlog {

    @Nullable
    private RestClient restclient = null;
    @Nullable
    private List<SprintIssue> issues = null;
    @Nullable
    private List<SprintIssue> backlogIssues = null;
    private int rankCustomFieldId = 0;
    @Nullable
    private List<Sprint> sprints = null;
    @Nullable
    private List<RapidViewProject> projects = null;
    @Nullable
    private List<Marker> markers = null;
    @Nullable
    private List<Epic> epics = null;
    private boolean canEditEpics = false;
    private boolean canManageSprints = false;
    private boolean maxIssuesExceeded = false;
    private int queryResultLimit = 0;
    @Nullable
    private Map<String, List<RapidViewVersion>> versionsPerProject = null;

    /**
     * Creates the backlog from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected Backlog(@Nullable RestClient restclient, @Nullable JSONObject json) {
        this.restclient = restclient;

        if (json != null)
            deserialise(json);
    }

    private void deserialise(JSONObject json) {

        issues = GreenHopperField.getResourceArray(
            SprintIssue.class,
            ((Map) json).get("issues"),
            restclient);
        rankCustomFieldId = Field.getInteger(((Map) json).get("rankCustomFieldId"));
        sprints = GreenHopperField.getResourceArray(
            Sprint.class,
            ((Map) json).get("sprints"),
            restclient);
        projects = GreenHopperField.getResourceArray(
            RapidViewProject.class,
            ((Map) json).get("projects"),
            restclient);
        markers = GreenHopperField.getResourceArray(
            Marker.class,
            ((Map) json).get("markers"),
            restclient);
        canManageSprints = Field.getBoolean(((Map) json).get("canManageSprints"));
        maxIssuesExceeded = Field.getBoolean(((Map) json).get("maxIssuesExceeded"));
        queryResultLimit = Field.getInteger(((Map) json).get("queryResultLimit"));

        if (json.containsKey("epicData") && ((Map) json).get("epicData") instanceof JSONObject) {
            Map epicData = (Map) ((Map) json).get("epicData");

            epics = GreenHopperField.getResourceArray(Epic.class, epicData.get("epics"), restclient);
            canEditEpics = Field.getBoolean(epicData.get("canEditEpics"));
        }

        if (json.containsKey("versionData") && ((Map) json).get("versionData") instanceof JSONObject) {
            Map verData = (JSONObject) ((Map) json).get("versionData");

            if (verData.containsKey("versionsPerProject") &&
                verData.get("versionsPerProject") instanceof JSONObject) {

                Map verMap = (Map)verData.get("versionsPerProject");
                versionsPerProject = new HashMap<String, List<RapidViewVersion>>();

                for (Map.Entry<String, Object> kvp : 
                     (Iterable<Map.Entry<String, Object>>)verMap.entrySet()) {

                    if (!(kvp.getValue() instanceof JSONArray))
                        continue;

                    List<RapidViewVersion> versions = new ArrayList<RapidViewVersion>();

                    for (Object item : (JSONArray)kvp.getValue()) {
                        if (!(item instanceof JSONObject))
                            continue;

                        RapidViewVersion ver = new RapidViewVersion(restclient, (JSONObject)item);
                        versions.add(ver);
                    }

                    versionsPerProject.put(kvp.getKey(), versions);
                }
            }
        }

        //determining which issues are actually in the backlog vs the sprints
        //fill in the issues into the single sprints and the backlog issue list respectively
        for(SprintIssue issue : issues){
            boolean addedToSprint = false;
            for(Sprint sprint : sprints){
                if(sprint.getIssuesIds().contains(issue.getId())){
                    sprint.getIssues().add(issue);
                    addedToSprint = true;
                }
            }
            if(!addedToSprint){
                if(backlogIssues == null){
                    backlogIssues = new ArrayList<SprintIssue>();
                }
                backlogIssues.add(issue);
            }
        }

    }

    /**
     * Retrieves the backlog data for the given rapid view.
     *
     * @param restclient REST client instance
     * @param rv Rapid View instance
     *
     * @return the backlog
     *
     * @throws JiraException when the retrieval fails
     */
    public static Backlog get(@NotNull RestClient restclient, RapidView rv)
        throws JiraException {

        final int rvId = rv.getId();
        JSON result = null;

        try {
            URI reporturi = restclient.buildURI(
                GreenHopperResource.RESOURCE_URI + "xboard/plan/backlog/data",
                new HashMap<String, String>() {{
                    put("rapidViewId", Integer.toString(rvId));
                }});
            result = restclient.get(reporturi);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve backlog data", ex);
        }

        if (!(result instanceof JSONObject))
            throw new JiraException("JSON payload is malformed");

        return new Backlog(restclient, (JSONObject)result);
    }

    @Nullable
    public List<SprintIssue> getIssues() {
        return issues;
    }

    @Nullable
    public List<SprintIssue> getBacklogIssues() {
        return backlogIssues;
    }

    public int getRankCustomFieldId() {
        return rankCustomFieldId;
    }

    @Nullable
    public List<Sprint> getSprints() {
        return sprints;
    }

    @Nullable
    public List<RapidViewProject> getProjects() {
        return projects;
    }

    @Nullable
    public List<Marker> getMarkers() {
        return markers;
    }

    @Nullable
    public List<Epic> getEpics() {
        return epics;
    }

    public boolean canEditEpics() {
        return canEditEpics;
    }

    public boolean canManageSprints() {
        return canManageSprints;
    }

    public boolean maxIssuesExceeded() {
        return maxIssuesExceeded;
    }

    public int queryResultLimit() {
        return queryResultLimit;
    }

    @Nullable
    public Map<String, List<RapidViewVersion>> getVersionsPerProject() {
        return versionsPerProject;
    }
}

