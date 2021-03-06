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

import net.sf.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Represents issue time tracking data.
 */
public class TimeTracking {

    @Nullable
    private String originalEstimate = null;
    @Nullable
    private String remainingEstimate = null;
    @Nullable
    private String timeSpent = null;
    @Nullable
    private Integer originalEstimateSeconds = null;
    @Nullable
    private Integer remainingEstimateSeconds = null;
    @Nullable
    private Integer timeSpentSeconds = null;

    /**
     * Creates a time tracking structure from a JSON payload.
     *
     * @param json JSON payload
     */
    protected TimeTracking(JSONObject json) {

        originalEstimate = Field.getString(((Map<?, ?>) json).get("originalEstimate"));
        remainingEstimate = Field.getString(((Map<?, ?>) json).get("remainingEstimate"));
        timeSpent = Field.getString(((Map<?, ?>) json).get("timeSpent"));
        originalEstimateSeconds = Field.getInteger(((Map<?, ?>) json).get("originalEstimateSeconds"));
        remainingEstimateSeconds = Field.getInteger(((Map<?, ?>) json).get("remainingEstimateSeconds"));
        timeSpentSeconds = Field.getInteger(((Map<?, ?>) json).get("timeSpentSeconds"));
    }

    public TimeTracking() {
    }

    public TimeTracking(TimeTracking tt) {
        this.originalEstimate = tt.originalEstimate;
        this.remainingEstimate = tt.remainingEstimate;
        this.originalEstimateSeconds = tt.originalEstimateSeconds;
        this.remainingEstimateSeconds = tt.remainingEstimateSeconds;
        this.timeSpent = tt.timeSpent;
        this.timeSpentSeconds =tt.timeSpentSeconds;
    }

    @NotNull
    protected JSONObject toJsonObject() {
        JSONObject object = new JSONObject();
        if (originalEstimate != null)
            object.put("originalEstimate", originalEstimate);

        if (remainingEstimate != null)
            object.put("remainingEstimate", remainingEstimate);

        if (originalEstimateSeconds >= 0)
            object.put("originalEstimateSeconds", originalEstimateSeconds);

        if (remainingEstimateSeconds >= 0)
            object.put("remainingEstimateSeconds", remainingEstimateSeconds);

        return object;
    }

    @Nullable
    public String getOriginalEstimate() {
        return originalEstimate;
    }

    @Nullable
    public String getRemainingEstimate() {
        return remainingEstimate;
    }

    @Nullable
    public String getTimeSpent() {
        return timeSpent;
    }

    public int getOriginalEstimateSeconds() {
        return originalEstimateSeconds;
    }

    public int getRemainingEstimateSeconds() {
        return remainingEstimateSeconds;
    }

    public void setOriginalEstimate(@Nullable String originalEstimate) {
        this.originalEstimate = originalEstimate;
    }

    public void setRemainingEstimate(@Nullable String remainingEstimate) {
        this.remainingEstimate = remainingEstimate;
    }

    public void setOrignalEstimateSeconds(int originalEstimateSeconds) {
        this.originalEstimateSeconds = originalEstimateSeconds;
    }

    public void setRemainingEstimateSeconds(int remainingEstimateSeconds) {
        this.remainingEstimateSeconds = remainingEstimateSeconds;
    }

    public int getTimeSpentSeconds() {
        return timeSpentSeconds;
    }
}
