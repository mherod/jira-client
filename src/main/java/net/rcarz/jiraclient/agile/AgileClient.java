/**
 * jira-client - a simple JIRA REST client
 * Copyright (c) 2013 Bob Carroll (bob.carroll@alum.rit.edu)
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.rcarz.jiraclient.agile;

import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.RestClient;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * An Agile extension to the JIRA client.
 *
 * @author pldupont
 * @see "https://docs.atlassian.com/jira-software/REST/cloud/"
 */
public class AgileClient {

    @Nullable
    private RestClient restClient;

    /**
     * Creates an Agile client.
     *
     * @param jira JIRA client
     */
    public AgileClient(JiraClient jira) {
        restClient = jira.getRestClient();
    }

    /**
     * Retrieves the board with the given ID.
     *
     * @param id Board ID
     * @return a Board instance
     * @throws JiraException when something goes wrong
     */
    public Board getBoard(long id) throws JiraException {
        return Board.get(restClient, id);
    }

    /**
     * Retrieves all boards visible to the session user.
     *
     * @return a list of boards
     * @throws JiraException when something goes wrong
     */
    public List<Board> getBoards() throws JiraException {
        return Board.getAll(restClient);
    }

    /**
     * Retrieves the sprint with the given ID.
     *
     * @param id Sprint ID
     * @return a Sprint instance
     * @throws JiraException when something goes wrong
     */
    public Sprint getSprint(long id) throws JiraException {
        return Sprint.get(restClient, id);
    }

    /**
     * Retrieves the issue with the given ID.
     *
     * @param id Issue ID
     * @return an Issue instance
     * @throws JiraException when something goes wrong
     */
    public Issue getIssue(long id) throws JiraException {
        return Issue.get(restClient, id);
    }

    /**
     * Retrieves the issue with the given Key.
     *
     * @param key Issue Key
     * @return an Issue instance
     * @throws JiraException when something goes wrong
     */
    public Issue getIssue(String key) throws JiraException {
        return Issue.get(restClient, key);
    }

    /**
     * Retrieves the epic with the given ID.
     *
     * @param id Epic ID
     * @return an Epic instance
     * @throws JiraException when something goes wrong
     */
    public Epic getEpic(long id) throws JiraException {
        return Epic.get(restClient, id);
    }

    @Nullable
    public RestClient getRestClient() {
        return restClient;
    }
}

