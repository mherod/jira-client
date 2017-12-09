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

import java.util.Map;

/**
 * Represents a GreenHopper epic issue.
 */
public class Epic extends GreenHopperIssue {

    @Nullable
    public String epicLabel = null;
    @Nullable
    public String epicColour = null;
    @Nullable
    public EpicStats epicStats = null;

    /**
     * Creates an epic issue from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected Epic(RestClient restclient, @Nullable JSONObject json) {
        super(restclient, json);

        if (json != null)
            deserialise(json);
    }

    private void deserialise(JSONObject json) {
        Map map = json;

        epicLabel = Field.getString(map.get("epicLabel"));
        epicColour = Field.getString(map.get("epicColor"));
        epicStats = GreenHopperField.getEpicStats(map.get("epicStats"));
    }

    @Nullable
    public String getEpicLabel() {
        return epicLabel;
    }

    @Nullable
    public String getEpicColour() {
        return epicColour;
    }

    @Nullable
    public EpicStats getEpicStats() {
        return epicStats;
    }
}

