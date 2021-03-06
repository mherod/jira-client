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
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Represents an issue priority.
 */
public class Transition extends Resource {

    @Nullable
    private String name = null;
    @Nullable
    private Status toStatus = null;
    @Nullable
    private Map fields = null;

    /**
     * Creates a priority from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected Transition(RestClient restclient, @Nullable JSONObject json) {
        super(restclient);

        if (json != null)
            deserialise(json);
    }

    private void deserialise(JSONObject json) {

        self = Field.getString(((Map) json).get("self"));
        id = Field.getString(((Map) json).get("id"));
        name = Field.getString(((Map) json).get("name"));
        toStatus = Field.getResource(Status.class, ((Map) json).get(Field.TRANSITION_TO_STATUS), restclient);

        fields = (Map) ((Map) json).get("fields");
    }

    @Nullable
    @Override
    public String toString() {
        return getName();
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public Status getToStatus() {
        return toStatus;
    }

    @Nullable
    public Map getFields() {
        return fields;
    }

}

