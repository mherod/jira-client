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

import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.RestClient;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GreenHopper sprint statistics.
 */
public class SprintReport {

    @Nullable
    private RestClient restclient = null;
    @Nullable
    private Sprint sprint = null;
    @Nullable
    private List<SprintIssue> completedIssues = null;
    @Nullable
    private List<SprintIssue> incompletedIssues = null;
    @Nullable
    private List<SprintIssue> puntedIssues = null;
    @Nullable
    private EstimateSum completedIssuesEstimateSum = null;
    @Nullable
    private EstimateSum incompletedIssuesEstimateSum = null;
    @Nullable
    private EstimateSum allIssuesEstimateSum = null;
    @Nullable
    private EstimateSum puntedIssuesEstimateSum = null;
    @Nullable
    private List<String> issueKeysAddedDuringSprint = null;

    /**
     * Creates a sprint report from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected SprintReport(@Nullable RestClient restclient, @Nullable JSONObject json) {
        this.restclient = restclient;

        if (json != null)
            deserialise(json);
    }

    private void deserialise(JSONObject json) {

        sprint = GreenHopperField.getResource(Sprint.class, ((Map) json).get("sprint"), restclient);
        completedIssues = GreenHopperField.getResourceArray(
            SprintIssue.class,
            ((Map) json).get("completedIssues"),
            restclient);
        incompletedIssues = GreenHopperField.getResourceArray(
            SprintIssue.class,
            ((Map) json).get("incompletedIssues"),
            restclient);
        puntedIssues = GreenHopperField.getResourceArray(
            SprintIssue.class,
            ((Map) json).get("puntedIssues"),
            restclient);
        completedIssuesEstimateSum = GreenHopperField.getEstimateSum(
            ((Map) json).get("completedIssuesEstimateSum"));
        incompletedIssuesEstimateSum = GreenHopperField.getEstimateSum(
            ((Map) json).get("incompletedIssuesEstimateSum"));
        allIssuesEstimateSum = GreenHopperField.getEstimateSum(
            ((Map) json).get("allIssuesEstimateSum"));
        puntedIssuesEstimateSum = GreenHopperField.getEstimateSum(
            ((Map) json).get("puntedIssuesEstimateSum"));
        issueKeysAddedDuringSprint = GreenHopperField.getStringArray(
            ((Map) json).get("issueKeysAddedDuringSprint"));
    }

    /**
     * Retrieves the sprint report for the given rapid view and sprint.
     *
     * @param restclient REST client instance
     * @param rv Rapid View instance
     * @param sprint Sprint instance
     *
     * @return the sprint report
     *
     * @throws JiraException when the retrieval fails
     */
    public static SprintReport get(@NotNull RestClient restclient, RapidView rv, Sprint sprint)
        throws JiraException {

        final int rvId = rv.getId();
        final int sprintId = sprint.getId();
        JSON result = null;

        try {
            URI reporturi = restclient.buildURI(
                GreenHopperResource.RESOURCE_URI + "rapid/charts/sprintreport",
                new HashMap<String, String>() {{
                    put("rapidViewId", Integer.toString(rvId));
                    put("sprintId", Integer.toString(sprintId));
                }});
            result = restclient.get(reporturi);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve sprint report", ex);
        }

        if (!(result instanceof JSONObject))
            throw new JiraException("JSON payload is malformed");

        JSONObject jo = (JSONObject)result;

        if (!jo.containsKey("contents") || !(jo.get("contents") instanceof JSONObject))
            throw new JiraException("Sprint report content is malformed");

        return new SprintReport(restclient, (JSONObject)jo.get("contents"));
    }

    @Nullable
    public Sprint getSprint() {
        return sprint;
    }

    @Nullable
    public List<SprintIssue> getCompletedIssues() {
        return completedIssues;
    }

    @Nullable
    public List<SprintIssue> getIncompletedIssues() {
        return incompletedIssues;
    }

    @Nullable
    public List<SprintIssue> getPuntedIssues() {
        return puntedIssues;
    }

    @Nullable
    public EstimateSum getCompletedIssuesEstimateSum() {
        return completedIssuesEstimateSum;
    }

    @Nullable
    public EstimateSum getIncompletedIssuesEstimateSum() {
        return incompletedIssuesEstimateSum;
    }

    @Nullable
    public EstimateSum getAllIssuesEstimateSum() {
        return allIssuesEstimateSum;
    }

    @Nullable
    public EstimateSum getPuntedIssuesEstimateSum() {
        return puntedIssuesEstimateSum;
    }

    @Nullable
    public List<String> getIssueKeysAddedDuringSprint() {
        return issueKeysAddedDuringSprint;
    }
}


