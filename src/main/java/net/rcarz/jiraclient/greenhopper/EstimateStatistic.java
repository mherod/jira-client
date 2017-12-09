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
import net.sf.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * GreenHopper estimate statistics for rapid views.
 */
public class EstimateStatistic {

    @Nullable
    private String statFieldId = null;
    @Nullable
    private Double statFieldValue = 0.0;
    @Nullable
    private String statFieldText = null;

    /**
     * Creates an estimate statistic from a JSON payload.
     *
     * @param json JSON payload
     */
    protected EstimateStatistic(@NotNull JSONObject json) {

        statFieldId = Field.getString(((Map) json).get("statFieldId"));

        if (json.containsKey("statFieldValue") &&
            ((Map) json).get("statFieldValue") instanceof JSONObject) {

            Map val = (Map)json.get("statFieldValue");

            statFieldValue = Field.getDouble(val.get("value"));
            statFieldText = Field.getString(val.get("text"));
        }
    }

    @Nullable
    public String getFieldId() {
        return statFieldId;
    }

    @Nullable
    public Double getFieldValue() {
        return statFieldValue;
    }

    @Nullable
    public String getFieldText() {
        return statFieldText;
    }
}

