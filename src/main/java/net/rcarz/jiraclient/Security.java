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

import java.util.Map;

/**
 * Represents an issue security.
 */
public class Security extends Resource {

    @Nullable
    private String description = null;
    @Nullable
    private String name = null;

    /**
     * Creates a security from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected Security(RestClient restclient, @Nullable JSONObject json) {
        super(restclient);

        if (json != null)
            deserialise(json);
    }

    private void deserialise(JSONObject json) {

        self = Field.getString(((Map) json).get("self"));
        id = Field.getString(((Map) json).get("id"));
        description = Field.getString(((Map) json).get("description"));
        name = Field.getString(((Map) json).get("name"));
    }

    /**
     * Retrieves the given security record.
     *
     * @param restclient REST client instance
     * @param id Internal JIRA ID of the security
     *
     * @return a security instance
     *
     * @throws JiraException when the retrieval fails
     */
    public static Security get(@NotNull RestClient restclient, String id)
        throws JiraException {

        JSON result = null;

        try {
            result = restclient.get(getBaseUri() + "securitylevel/" + id);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve security " + id, ex);
        }

        if (!(result instanceof JSONObject))
            throw new JiraException("JSON payload is malformed");

        return new Security(restclient, (JSONObject)result);
    }

    @Nullable
    @Override
    public String toString() {
        return getName();
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @Nullable
    public String getName() {
        return name;
    }

}

