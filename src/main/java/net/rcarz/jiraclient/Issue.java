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

import net.rcarz.utils.WorklogUtils;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Represents a JIRA issue.
 */
public class Issue extends Resource {

    /**
     * count issues with the given query.
     *
     * @param restclient REST client instance
     *
     * @param jql JQL statement
     *
     * @return the count
     *
     * @throws JiraException when the search fails
     */
    public static int count(@NotNull RestClient restclient, String jql) throws JiraException {
        JSON result = null;
        try {
            Map<String, String> queryParams = new HashMap<String, String>();
            queryParams.put("jql", jql);
            queryParams.put("maxResults", "1");
            URI searchUri = restclient.buildURI(getBaseUri() + "search", queryParams);
            result = restclient.get(searchUri);
        } catch (Exception ex) {
            throw new JiraException("Failed to search issues", ex);
        }

        if (!(result instanceof JSONObject)) {
            throw new JiraException("JSON payload is malformed");
        }
        Map map = (Map) result;
        return Field.getInteger(map.get("total"));
    }


    /**
     * Used to {@link #create() create} remote links. Provide at least the {@link #url(String)} or
     * {@link #globalId(String) global id} and the {@link #title(String) title}.
     */
    public static final class FluentRemoteLink {

        final private RestClient restclient;
        final private String key;
        @NotNull
        final private JSONObject request;
        @NotNull
        final private JSONObject object;


        private FluentRemoteLink(final RestClient restclient, String key) {
            this.restclient = restclient;
            this.key = key;
            request = new JSONObject();
            object = new JSONObject();
        }


        /**
         * A globally unique identifier which uniquely identifies the remote application and the remote object within
         * the remote system. The maximum length is 255 characters. This call sets also the {@link #url(String) url}.
         *
         * @param globalId the global id
         * @return this instance
         */
        @NotNull
        public FluentRemoteLink globalId(final String globalId) {
            request.put("globalId", globalId);
            url(globalId);
            return this;
        }


        /**
         * A hyperlink to the object in the remote system.
         * @param url A hyperlink to the object in the remote system.
         * @return this instance
         */
        @NotNull
        public FluentRemoteLink url(final String url) {
            object.put("url", url);
            return this;
        }


        /**
         * The title of the remote object.
         * @param title The title of the remote object.
         * @return this instance
         */
        @NotNull
        public FluentRemoteLink title(final String title) {
            object.put("title", title);
            return this;
        }


        /**
         * Provide an icon for the remote link.
         * @param url A 16x16 icon representing the type of the object in the remote system.
         * @param title Text for the tooltip of the main icon describing the type of the object in the remote system.
         * @return this instance
         */
        @NotNull
        public FluentRemoteLink icon(final String url, final String title) {
            final JSONObject icon = new JSONObject();
            icon.put("url16x16", url);
            icon.put("title", title);
            object.put("icon", icon);
            return this;
        }


        /**
         * The status in the remote system.
         * @param resolved if {@code true} the link to the issue will be in a strike through font.
         * @param title Text for the tooltip of the main icon describing the type of the object in the remote system.
         * @param iconUrl Text for the tooltip of the main icon describing the type of the object in the remote system.
         * @param statusUrl A hyperlink for the tooltip of the the status icon.
         * @return this instance
         */
        @NotNull
        public FluentRemoteLink status(final boolean resolved, @Nullable final String iconUrl, final String title, @Nullable final String statusUrl) {
            final JSONObject status = new JSONObject();
            status.put("resolved", Boolean.toString(resolved));
            final JSONObject icon = new JSONObject();
            icon.put("title", title);
            if (iconUrl != null) {
                icon.put("url16x16", iconUrl);
            }
            if (statusUrl != null) {
                icon.put("link", statusUrl);
            }
            status.put("icon", icon);
            object.put("status", status);
            return this;
        }


        /**
         * Textual summary of the remote object.
         * @param summary Textual summary of the remote object.
         * @return this instance
         */
        @NotNull
        public FluentRemoteLink summary(final String summary) {
            object.put("summary", summary);
            return this;
        }


        /**
         * Relationship between the remote object and the JIRA issue. This can be a verb or a noun.
         * It is used to group together links in the UI.
         * @param relationship Relationship between the remote object and the JIRA issue.
         * @return this instance
         */
        @NotNull
        public FluentRemoteLink relationship(final String relationship) {
            request.put("relationship", relationship);
            return this;
        }


        /**
         * The application for this remote link. Links are grouped on the type and name in the UI. The name-spaced
         * type of the application. It is not displayed to the user. Renderering plugins can register to render a
         * certain type of application.
         * @param type The name-spaced type of the application.
         * @param name The human-readable name of the remote application instance that stores the remote object.
         * @return this instance
         */
        @NotNull
        public FluentRemoteLink application(@Nullable final String type, final String name) {
            final JSONObject application = new JSONObject();
            if (type != null) {
                application.put("type", type);
            }
            application.put("name", name);
            request.put("application", application);
            return this;
        }


        /**
         * Creates or updates the remote link if a {@link #globalId(String) global id} is given and there is already
         * a remote link for the specified global id.
         * @throws JiraException when the remote link creation fails
         */
        public void create() throws JiraException {
            try {
                request.put("object", object);
                restclient.post(getRestUri(key) + "/remotelink", request);
            } catch (Exception ex) {
                throw new JiraException("Failed add remote link to issue " + key, ex);
            }
        }

    }

    public static JSONObject getCreateMetadata(
            @NotNull RestClient restclient, String project, String issueType) throws JiraException {

        JSON result = null;

        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("expand", "projects.issuetypes.fields");
            params.put("projectKeys", project);
            params.put("issuetypeNames", issueType);
            URI createuri = restclient.buildURI(
                getBaseUri() + "issue/createmeta",
                params);
            result = restclient.get(createuri);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve issue metadata", ex);
        }

        if (!(result instanceof JSONObject))
            throw new JiraException("JSON payload is malformed");

        JSONObject jo = (JSONObject)result;

        if (jo.isNullObject() || !jo.containsKey("projects") ||
                !(jo.get("projects") instanceof JSONArray))
            throw new JiraException("Create metadata is malformed");

        List<Project> projects = Field.getResourceArray(
            Project.class,
                jo.get("projects"),
            restclient);

        if (projects.isEmpty() || projects.get(0).getIssueTypes().isEmpty())
            throw new JiraException("Project '"+ project + "'  or issue type '" + issueType +
                    "' missing from create metadata. Do you have enough permissions?");

        return projects.get(0).getIssueTypes().get(0).getFields();
    }

    /**
     * Search for issues with the given query and specify which fields to
     * retrieve. If the total results is bigger than the maximum returned
     * results, then further calls can be made using different values for
     * the <code>startAt</code> field to obtain all the results.
     *
     * @param restclient REST client instance
     *
     * @param jql JQL statement
     *
     * @param includedFields Specifies which issue fields will be included in
     * the result.
     * <br>Some examples how this parameter works:
     * <ul>
     * <li>*all - include all fields</li>
     * <li>*navigable - include just navigable fields</li>
     * <li>summary,comment - include just the summary and comments</li>
     * <li>*all,-comment - include all fields</li>
     * </ul>
     *
     * @param maxResults if non-<code>null</code>, defines the maximum number of
     * results that can be returned
     *
     * @param startAt if non-<code>null</code>, defines the first issue to
     * return
     *
     * @param expandFields fields to expand when obtaining the issue
     *
     * @return a search result structure with results
     *
     * @throws JiraException when the search fails
     */
    public static SearchResult search(RestClient restclient, String jql,
            String includedFields, String expandFields, Integer maxResults,
            Integer startAt) {

        return new SearchResult(
            restclient,
            jql,
            includedFields,
            expandFields,
            maxResults,
            startAt
        );
    }

    /**
     * Creates the URI to execute a jql search.
     *
     * @param restclient
     * @param jql
     * @param includedFields
     * @param expandFields
     * @param maxResults
     * @param startAt
     * @return the URI to execute a jql search.
     * @throws URISyntaxException
     */
    private static URI createSearchURI(@NotNull RestClient restclient, String jql,
                                       @Nullable String includedFields, @Nullable String expandFields, @Nullable Integer maxResults,
                                       @Nullable Integer startAt) throws URISyntaxException {
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("jql", jql);
        if(maxResults != null){
            queryParams.put("maxResults", String.valueOf(maxResults));
        }
        if (includedFields != null) {
            queryParams.put("fields", includedFields);
        }
        if (expandFields != null) {
            queryParams.put("expand", expandFields);
        }
        if (startAt != null) {
            queryParams.put("startAt", String.valueOf(startAt));
        }

        return restclient.buildURI(getBaseUri() + "search", queryParams);
    }

    private void deserialise(JSONObject json) {

        id = Field.getString(((Map) json).get("id"));
        self = Field.getString(((Map) json).get("self"));
        key = Field.getString(((Map) json).get("key"));

        fields = (Map) ((Map) json).get("fields");
        if (fields == null)
            return;

        assignee = Field.getResource(User.class, fields.get(Field.ASSIGNEE), restclient);
        attachments = Field.getResourceArray(Attachment.class, fields.get(Field.ATTACHMENT), restclient);
        changeLog = Field.getResource(ChangeLog.class, ((Map) json).get(Field.CHANGE_LOG), restclient);
        comments = Field.getComments(fields.get(Field.COMMENT), restclient, key);
        components = Field.getResourceArray(Component.class, fields.get(Field.COMPONENTS), restclient);
        description = Field.getString(fields.get(Field.DESCRIPTION));
        dueDate = Field.getDate(fields.get(Field.DUE_DATE));
        fixVersions = Field.getResourceArray(Version.class, fields.get(Field.FIX_VERSIONS), restclient);
        issueLinks = Field.getResourceArray(IssueLink.class, fields.get(Field.ISSUE_LINKS), restclient);
        issueType = Field.getResource(IssueType.class, fields.get(Field.ISSUE_TYPE), restclient);
        labels = Field.getStringArray(fields.get(Field.LABELS));
        parent = Field.getResource(Issue.class, fields.get(Field.PARENT), restclient);
        priority = Field.getResource(Priority.class, fields.get(Field.PRIORITY), restclient);
        project = Field.getResource(Project.class, fields.get(Field.PROJECT), restclient);
        reporter = Field.getResource(User.class, fields.get(Field.REPORTER), restclient);
        resolution = Field.getResource(Resolution.class, fields.get(Field.RESOLUTION), restclient);
        resolutionDate = Field.getDateTime(fields.get(Field.RESOLUTION_DATE));
        status = Field.getResource(Status.class, fields.get(Field.STATUS), restclient);
        subtasks = Field.getResourceArray(Issue.class, fields.get(Field.SUBTASKS), restclient);
        summary = Field.getString(fields.get(Field.SUMMARY));
        timeTracking = Field.getTimeTracking(fields.get(Field.TIME_TRACKING));
        versions = Field.getResourceArray(Version.class, fields.get(Field.VERSIONS), restclient);
        votes = Field.getResource(Votes.class, fields.get(Field.VOTES), restclient);
        watches = Field.getResource(Watches.class, fields.get(Field.WATCHES), restclient);
        workLogs = Field.getWorkLogs(fields.get(Field.WORKLOG), restclient);
        timeEstimate = Field.getInteger(fields.get(Field.TIME_ESTIMATE));
        timeSpent = Field.getInteger(fields.get(Field.TIME_SPENT));
        createdDate = Field.getDateTime(fields.get(Field.CREATED_DATE));
        updatedDate = Field.getDateTime(fields.get(Field.UPDATED_DATE));
        security = Field.getResource(Security.class, fields.get(Field.SECURITY), restclient);
    }
    
    /**
     * Removes a watcher to the issue.
     *
     * @param username Username of the watcher to remove
     *
     * @throws JiraException when the operation fails
     */
    public void deleteWatcher(String username) throws JiraException {

        try {
            Map<String, String> connectionParams = new HashMap<String, String>();
            connectionParams.put("username", username);
            URI uri = restclient.buildURI(
                getRestUri(key) + "/watchers", connectionParams);
            restclient.delete(uri);
        } catch (Exception ex) {
            throw new JiraException(
                "Failed to remove watch (" + username + ") from issue " + key, ex
            );
        }
    }

    public static final class NewAttachment {

        @Nullable
        private final String filename;
        @Nullable
        private final Object content;

        public NewAttachment(@NotNull File content) {
            this(content.getName(), content);
        }

        public NewAttachment(String filename, File content) {
            this.filename = requireFilename(filename);
            this.content = requireContent(content);
        }

        public NewAttachment(String filename, InputStream content) {
            this.filename = requireFilename(filename);
            this.content = requireContent(content);
        }

        public NewAttachment(String filename, byte[] content) {
            this.filename = requireFilename(filename);
            this.content = requireContent(content);
        }

        @Nullable
        String getFilename() {
            return filename;
        }

        @Nullable
        Object getContent() {
            return content;
        }

        @Nullable
        private static String requireFilename(@Nullable String filename) {
            if (filename == null) {
                throw new NullPointerException("filename may not be null");
            }
            if (filename.length() == 0) {
                throw new IllegalArgumentException("filename may not be empty");
            }
            return filename;
        }

        @Nullable
        private static Object requireContent(@Nullable Object content) {
            if (content == null) {
                throw new NullPointerException("content may not be null");
            }
            return content;
        }

    }

    @Nullable
    private String key = null;
    @Nullable
    private Map fields = null;

    /* system fields */
    @Nullable
    private User assignee = null;
    @Nullable
    private List<Attachment> attachments = null;
    @Nullable
    private ChangeLog changeLog = null;
    @Nullable
    private List<Comment> comments = null;
    @Nullable
    private List<Component> components = null;
    @Nullable
    private String description = null;
    @Nullable
    private Date dueDate = null;
    @Nullable
    private List<Version> fixVersions = null;
    @Nullable
    private List<IssueLink> issueLinks = null;
    @Nullable
    private IssueType issueType = null;
    @Nullable
    private List<String> labels = null;
    @Nullable
    private Issue parent = null;
    @Nullable
    private Priority priority = null;
    @Nullable
    private Project project = null;
    @Nullable
    private User reporter = null;
    @Nullable
    private Resolution resolution = null;
    @Nullable
    private Date resolutionDate = null;
    @Nullable
    private Status status = null;
    @Nullable
    private List<Issue> subtasks = null;
    @Nullable
    private String summary = null;
    @Nullable
    private TimeTracking timeTracking = null;
    @Nullable
    private List<Version> versions = null;
    @Nullable
    private Votes votes = null;
    @Nullable
    private Watches watches = null;
    @Nullable
    private List<WorkLog> workLogs = null;
    @Nullable
    private Integer timeEstimate = null;
    @Nullable
    private Integer timeSpent = null;
    @Nullable
    private Date createdDate = null;
    @Nullable
    private Date updatedDate = null;
    @Nullable
    private Security security = null;

    /**
     * Creates an issue from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected Issue(RestClient restclient, @Nullable JSONObject json) {
        super(restclient);

        if (json != null)
            deserialise(json);
    }

    /**
     * Used to chain fields to a create action.
     */
    public static final class FluentCreate {

        @NotNull
        final
        Map<String, Object> fields = new HashMap<String, Object>();
        @Nullable
        RestClient restclient = null;
        @Nullable
        JSONObject createmeta = null;

        private FluentCreate(@Nullable RestClient restclient, @Nullable JSONObject createmeta) {
            this.restclient = restclient;
            this.createmeta = createmeta;
        }

        /**
         * Executes the create action (issue includes all fields).
         *
         * @throws JiraException when the create fails
         */
        public Issue execute() throws JiraException {
            return executeCreate(null);
        }

        /**
         * Executes the create action and specify which fields to retrieve.
         *
         * @param includedFields Specifies which issue fields will be included
         * in the result.
         * <br>Some examples how this parameter works:
         * <ul>
         * <li>*all - include all fields</li>
         * <li>*navigable - include just navigable fields</li>
         * <li>summary,comment - include just the summary and comments</li>
         * <li>*all,-comment - include all fields</li>
         * </ul>
         *
         * @throws JiraException when the create fails
         */
        public Issue execute(String includedFields) throws JiraException {
            return executeCreate(includedFields);
        }

        /**
         * Executes the create action and specify which fields to retrieve.
         *
         * @param includedFields Specifies which issue fields will be included
         * in the result.
         * <br>Some examples how this parameter works:
         * <ul>
         * <li>*all - include all fields</li>
         * <li>*navigable - include just navigable fields</li>
         * <li>summary,comment - include just the summary and comments</li>
         * <li>*all,-comment - include all fields</li>
         * </ul>
         *
         * @throws JiraException when the create fails
         */
        private Issue executeCreate(@Nullable String includedFields) throws JiraException {
            JSONObject fieldmap = new JSONObject();

            if (fields.size() == 0) {
                throw new JiraException("No fields were given for create");
            }

            for (Map.Entry<String, Object> ent : fields.entrySet()) {
                Object newval = Field.toJson(ent.getKey(), ent.getValue(), createmeta);
                fieldmap.put(ent.getKey(), newval);
            }

            JSONObject req = new JSONObject();
            req.put("fields", fieldmap);

            JSON result = null;

            try {
                result = restclient.post(getRestUri(null), req);
            } catch (Exception ex) {
                throw new JiraException("Failed to create issue", ex);
            }

            if (!(result instanceof JSONObject) || !((JSONObject) result).containsKey("key")
                    || !(((JSONObject) result).get("key") instanceof String)) {
                throw new JiraException("Unexpected result on create issue");
            }

            if (includedFields != null) {
                return Issue.get(restclient, (String) ((JSONObject) result).get("key"), includedFields);
            } else {
                return Issue.get(restclient, (String) ((JSONObject) result).get("key"));
            }
        }

        /**
         * Appends a field to the update action.
         *
         * @param name Name of the field
         * @param value New field value
         *
         * @return the current fluent update instance
         */
        @NotNull
        public FluentCreate field(String name, Object value) {
            fields.put(name, value);
            return this;
        }
    }

    private static String getRestUri(@Nullable String key) {
        return getBaseUri() + "issue/" + (key != null ? key : "");
    }

    /**
     * Iterates over all issues in the query by getting the next page of
     * issues when the iterator reaches the last of the current page.
     */
    private static class IssueIterator implements Iterator<Issue> {
        private Iterator<Issue> currentPage;
        private final RestClient restclient;
        @Nullable
        private Issue nextIssue;
        private Integer maxResults = -1;
        private final String jql;
        private final String includedFields;
        private final String expandFields;
        private Integer startAt;
        private List<Issue> issues;
        private int total;

        public IssueIterator(RestClient restclient, String jql, String includedFields,
                             String expandFields, Integer maxResults, Integer startAt) {
            this.restclient = restclient;
            this.jql = jql;
            this.includedFields = includedFields;
            this.expandFields = expandFields;
            this.maxResults = maxResults;
            this.startAt = startAt;
        }

        @Override
        public boolean hasNext() {
            if (nextIssue != null) {
                return true;
            }
            try {
                nextIssue = getNextIssue();
            } catch (JiraException e) {
                throw new RuntimeException(e);
            }
            return nextIssue != null;
        }

        @Nullable
        @Override
        public Issue next() {
            if (! hasNext()) {
                throw new NoSuchElementException();
            }
            Issue result = nextIssue;
            nextIssue = null;
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Method remove() not support for class " +
                                                    this.getClass().getName());
        }

        /**
         * Gets the next issue, returning null if none more available
         * Will ask the next set of issues from the server if the end
         * of the current list of issues is reached.
         *
         * @return the next issue, null if none more available
         * @throws JiraException
         */
        private Issue getNextIssue() throws JiraException {
            // first call
            if (currentPage == null) {
                currentPage = getNextIssues().iterator();
                if (currentPage == null || !currentPage.hasNext()) {
                    return null;
                } else {
                    return currentPage.next();
                }
            }

            // check if we need to get the next set of issues
            if (! currentPage.hasNext()) {
                currentPage = getNextIssues().iterator();
            }

            // return the next item if available
            if (currentPage.hasNext()) {
                return currentPage.next();
            } else {
                return null;
            }
        }

        /**
         * Execute the query to get the next set of issues.
         * Also sets the startAt, maxMresults, total and issues fields,
         * so that the SearchResult can access them.
         *
         * @return the next set of issues.
         * @throws JiraException
         */
        private List<Issue> getNextIssues() throws JiraException {
            if (issues == null && startAt == null) {
                startAt = 0;
            } else if (issues != null) {
                startAt = startAt + issues.size();
            }

            JSON result = null;

            try {
                URI searchUri = createSearchURI(restclient, jql, includedFields,
                        expandFields, maxResults, startAt);
                result = restclient.get(searchUri);
            } catch (Exception ex) {
                throw new JiraException("Failed to search issues", ex);
            }

            if (!(result instanceof JSONObject)) {
                throw new JiraException("JSON payload is malformed");
            }


            Map map = (Map) result;

            this.startAt = Field.getInteger(map.get("startAt"));
            this.maxResults = Field.getInteger(map.get("maxResults"));
            this.total = Field.getInteger(map.get("total"));
            this.issues = Field.getResourceArray(Issue.class, map.get("issues"), restclient);
            return issues;
        }
    }

    @NotNull
    private JSONObject getEditMetadata() throws JiraException {
        JSON result = null;

        try {
            result = restclient.get(getRestUri(key) + "/editmeta");
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve issue metadata", ex);
        }

        if (!(result instanceof JSONObject))
            throw new JiraException("JSON payload is malformed");

        JSONObject jo = (JSONObject)result;

        if (jo.isNullObject() || !jo.containsKey("fields") ||
                !(jo.get("fields") instanceof JSONObject))
            throw new JiraException("Edit metadata is malformed");

        return (JSONObject)jo.get("fields");
    }

    @NotNull
    public List<Transition> getTransitions() throws JiraException {
        JSON result = null;

        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("expand", "transitions.fields");
            URI transuri = restclient.buildURI(
                getRestUri(key) + "/transitions",params);
            result = restclient.get(transuri);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve transitions", ex);
        }

        JSONObject jo = (JSONObject)result;

        if (jo.isNullObject() || !jo.containsKey("transitions") ||
                !(jo.get("transitions") instanceof JSONArray))
            throw new JiraException("Transition metadata is missing.");

        JSONArray transitions = (JSONArray) jo.get("transitions");

        List<Transition> trans = new ArrayList<Transition>();
        for(Object obj: transitions){
            JSONObject ob = (JSONObject) obj;
            trans.add(new Transition(restclient, ob));
        }

        return trans;
    }

    /**
     * Adds an attachment to this issue.
     *
     * @param file java.io.File
     *
     * @throws JiraException when the attachment creation fails
     */
    public void addAttachment(File file) throws JiraException {
        try {
            restclient.post(getRestUri(key) + "/attachments", file);
        } catch (Exception ex) {
            throw new JiraException("Failed add attachment to issue " + key, ex);
        }
    }

    /**
     * Adds a remote link to this issue.
     *
     * @param url Url of the remote link
     * @param title Title of the remote link
     * @param summary Summary of the remote link
     *
     * @throws JiraException when the link creation fails
     * @see #remoteLink()
     */
    public void addRemoteLink(String url, String title, String summary) throws JiraException {
        remoteLink().url(url).title(title).summary(summary).create();
    }


    /**
     * Adds a remote link to this issue. At least set the
     * {@link FluentRemoteLink#url(String) url} or
     * {@link FluentRemoteLink#globalId(String) globalId} and
     * {@link FluentRemoteLink#title(String) title} before
     * {@link FluentRemoteLink#create() creating} the link.
     *
     * @return a fluent remote link instance
     */
    @NotNull
    public FluentRemoteLink remoteLink() {
        return new FluentRemoteLink(restclient, getKey());
    }

    /**
     * Adds an attachments to this issue.
     *
     * @param attachments  the attachments to add
     *
     * @throws JiraException when the attachments creation fails
     */
    public void addAttachments(@Nullable NewAttachment... attachments) throws JiraException {
        if (attachments == null) {
            throw new NullPointerException("attachments may not be null");
        }
        if (attachments.length == 0) {
            return;
        }
        try {
            restclient.post(getRestUri(key) + "/attachments", attachments);
        } catch (Exception ex) {
            throw new JiraException("Failed add attachment to issue " + key, ex);
        }
    }

    /**
     * Removes an attachments.
     *
     * @param attachmentId attachment id to remove
     *
     * @throws JiraException when the attachment removal fails
     */
    public void removeAttachment(@Nullable String attachmentId) throws JiraException {
    
        if (attachmentId == null) {
            throw new NullPointerException("attachmentId may not be null");
        }
    
        try {
            restclient.delete(getBaseUri() + "attachment/" + attachmentId);
        } catch (Exception ex) {
            throw new JiraException("Failed remove attachment " + attachmentId, ex);
        }
    }

    /**
     * Adds a comment to this issue.
     *
     * @param body Comment text
     *
     * @throws JiraException when the comment creation fails
     */
    @Nullable
    public Comment addComment(String body) throws JiraException {
        return addComment(body, null, null);
    }

    /**
     * Adds a comment to this issue with limited visibility.
     *
     * @param body Comment text
     * @param visType Target audience type (role or group)
     * @param visName Name of the role or group to limit visibility to
     *
     * @throws JiraException when the comment creation fails
     */
    @Nullable
    public Comment addComment(String body, @Nullable String visType, @Nullable String visName)
        throws JiraException {

        JSONObject req = new JSONObject();
        req.put("body", body);

        if (visType != null && visName != null) {
            JSONObject vis = new JSONObject();
            vis.put("type", visType);
            vis.put("value", visName);

            req.put("visibility", vis);
        }

        JSON result = null;

        try {
            result = restclient.post(getRestUri(key) + "/comment", req);
        } catch (Exception ex) {
            throw new JiraException("Failed add comment to issue " + key, ex);
        }

        if (!(result instanceof JSONObject)) {
            throw new JiraException("JSON payload is malformed");
        }

        return new Comment(restclient, (JSONObject) result, key);
    }

  /**
   * Adds {@link WorkLog} to this issue
   * @param comment provided comment
   * @param startDate provided start date
   * @param timeSpentSeconds provided time spent. This cannot be lower than 1m inute
   * @return
   * @throws JiraException when worklog creation fails
   */
    @NotNull
    public WorkLog addWorkLog(@Nullable String comment, @Nullable DateTime startDate, long timeSpentSeconds) throws JiraException {
        try {
            if (comment == null)
                throw new IllegalArgumentException("Invalid comment.");
            if (startDate == null)
                throw new IllegalArgumentException("Invalid start time.");
            if (timeSpentSeconds < 60) // We do not add a worklog that duration is below a minute
                throw new IllegalArgumentException("Time spent cannot be lower than 1 minute.");

            JSONObject req = new JSONObject();
            req.put("comment", comment);
            req.put("started", DateTimeFormat.forPattern(Field.DATETIME_FORMAT).print(startDate.getMillis()));
            req.put("timeSpent", WorklogUtils.formatDurationFromSeconds(timeSpentSeconds));

            JSON result = restclient.post(getRestUri(key) + "/worklog", req);
            JSONObject jo = (JSONObject) result;
            return new WorkLog(restclient, jo);
        } catch (Exception ex) {
            throw new JiraException("Failed add worklog to issue " + key, ex);
        }
    }

    /**
     * Links this issue with another issue.
     *
     * @param issue Other issue key
     * @param type Link type name
     *
     * @throws JiraException when the link creation fails
     */
    public void link(String issue, String type) throws JiraException {
        link(issue, type, null, null, null);
    }

    /**
     * Links this issue with another issue and adds a comment.
     *
     * @param issue Other issue key
     * @param type Link type name
     * @param body Comment text
     *
     * @throws JiraException when the link creation fails
     */
    public void link(String issue, String type, String body) throws JiraException {
        link(issue, type, body, null, null);
    }

    /**
     * Links this issue with another issue and adds a comment with limited visibility.
     *
     * @param issue Other issue key
     * @param type Link type name
     * @param body Comment text
     * @param visType Target audience type (role or group)
     * @param visName Name of the role or group to limit visibility to
     *
     * @throws JiraException when the link creation fails
     */
    public void link(String issue, String type, @Nullable String body, @Nullable String visType, @Nullable String visName)
        throws JiraException {

        JSONObject req = new JSONObject();

        JSONObject t = new JSONObject();
        t.put("name", type);
        req.put("type", t);

        JSONObject inward = new JSONObject();
        inward.put("key", key);
        req.put("inwardIssue", inward);

        JSONObject outward = new JSONObject();
        outward.put("key", issue);
        req.put("outwardIssue", outward);

        if (body != null) {
            JSONObject comment = new JSONObject();
            comment.put("body", body);

            if (visType != null && visName != null) {
                JSONObject vis = new JSONObject();
                vis.put("type", visType);
                vis.put("value", visName);

                comment.put("visibility", vis);
            }

            req.put("comment", comment);
        }

        try {
            restclient.post(getBaseUri() + "issueLink", req);
        } catch (Exception ex) {
            throw new JiraException("Failed to link issue " + key + " with issue " + issue, ex);
        }
    }

    /**
     * Creates a new JIRA issue.
     *
     * @param restclient REST client instance
     * @param project Key of the project to create the issue in
     * @param issueType Name of the issue type to create
     *
     * @return a fluent create instance
     *
     * @throws JiraException when the client fails to retrieve issue metadata
     */
    @NotNull
    public static FluentCreate create(@NotNull RestClient restclient, String project, String issueType)
        throws JiraException {

        FluentCreate fc = new FluentCreate(
            restclient,
            getCreateMetadata(restclient, project, issueType));

        return fc
            .field(Field.PROJECT, project)
            .field(Field.ISSUE_TYPE, issueType);
    }

    /**
     * Creates a new sub-task.
     *
     * @return a fluent create instance
     *
     * @throws JiraException when the client fails to retrieve issue metadata
     */
    @NotNull
    public FluentCreate createSubtask() throws JiraException {
        return Issue.create(restclient, getProject().getKey(), "Sub-task")
                .field(Field.PARENT, getKey());
    }

    @NotNull
    private static JSONObject realGet(@NotNull RestClient restclient, String key, Map<String, String> queryParams)
            throws JiraException {

        JSON result = null;

        try {
            URI uri = restclient.buildURI(getBaseUri() + "issue/" + key, queryParams);
            result = restclient.get(uri);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve issue " + key, ex);
        }

        if (!(result instanceof JSONObject)) {
            throw new JiraException("JSON payload is malformed");
        }

        return (JSONObject) result;
    }

    /**
     * Retrieves the given issue record.
     *
     * @param restclient REST client instance
     * @param key Issue key (PROJECT-123)
     *
     * @return an issue instance (issue includes all navigable fields)
     *
     * @throws JiraException when the retrieval fails
     */
    public static Issue get(@NotNull RestClient restclient, String key)
            throws JiraException {

        return new Issue(restclient, realGet(restclient, key, new HashMap<String, String>()));
    }

    /**
     * Retrieves the given issue record.
     *
     * @param restclient REST client instance
     *
     * @param key Issue key (PROJECT-123)
     *
     * @param includedFields Specifies which issue fields will be included in
     * the result.
     * <br>Some examples how this parameter works:
     * <ul>
     * <li>*all - include all fields</li>
     * <li>*navigable - include just navigable fields</li>
     * <li>summary,comment - include just the summary and comments</li>
     * <li>*all,-comment - include all fields</li>
     * </ul>
     *
     * @return an issue instance
     *
     * @throws JiraException when the retrieval fails
     */
    public static Issue get(@NotNull RestClient restclient, String key, final String includedFields)
            throws JiraException {

        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("fields", includedFields);
        return new Issue(restclient, realGet(restclient, key, queryParams));
    }

    /**
     * Retrieves the given issue record.
     *
     * @param restclient REST client instance
     *
     * @param key Issue key (PROJECT-123)
     *
     * @param includedFields Specifies which issue fields will be included in
     * the result.
     * <br>Some examples how this parameter works:
     * <ul>
     * <li>*all - include all fields</li>
     * <li>*navigable - include just navigable fields</li>
     * <li>summary,comment - include just the summary and comments</li>
     * <li>*all,-comment - include all fields</li>
     * </ul>
     *
     * @param expand fields to expand when obtaining the issue
     *
     * @return an issue instance
     *
     * @throws JiraException when the retrieval fails
     */
    public static Issue get(@NotNull RestClient restclient, String key, final String includedFields,
                            @Nullable final String expand) throws JiraException {

        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("fields", includedFields);
        if (expand != null) {
            queryParams.put("expand", expand);
        }
        return new Issue(restclient, realGet(restclient, key, queryParams));
    }

    /**
     * Issue search results structure.
     *
     * The issues of the result can be retrived from this class in 2 ways.
     *
     * The first is to access the issues field directly. This is a list of Issue instances.
     * Note however that this will only contain the issues fetched in the initial search,
     * so its size will be the same as the max result value or below.
     *
     * The second way is to use the iterator methods. This will return an Iterator instance,
     * that will iterate over every result of the search, even if there is more than the max
     * result value. The price for this, is that the call to next has none determistic performence,
     * as it sometimes need to fetch a new batch of issues from Jira.
     */
    public static class SearchResult {
        public int start = 0;
        public int max = 0;
        public int total = 0;
        @Nullable
        public List<Issue> issues = null;
        private final IssueIterator issueIterator;

        public SearchResult(RestClient restclient, String jql, String includedFields,
                            String expandFields, Integer maxResults, Integer startAt) {
            this.issueIterator = new IssueIterator(
                restclient,
                jql,
                includedFields,
                expandFields,
                maxResults,
                startAt
            );
            /* backwards compatibility shim - first page only */
            this.issueIterator.hasNext();
            this.max = issueIterator.maxResults;
            this.start = issueIterator.startAt;
            this.issues = issueIterator.issues;
            this.total = issueIterator.total;
        }

        /**
         * All issues found.
         *
         * @return All issues found.
         */
        public Iterator<Issue> iterator() {
            return issueIterator;
        }
    }

    /**
     * Used to chain fields to an update action.
     */
    public final class FluentUpdate {

        @NotNull
        final
        Map<String, Object> fields = new HashMap<String, Object>();
        @NotNull
        final
        Map<String, List> fieldOpers = new HashMap<String, List>();
        @Nullable
        JSONObject editmeta = null;

        private FluentUpdate(@Nullable JSONObject editmeta) {
            this.editmeta = editmeta;
        }

        /**
         * Executes the update action.
         *
         * @throws JiraException when the update fails
         */
        public void execute() throws JiraException {
            JSONObject fieldmap = new JSONObject();
            JSONObject updatemap = new JSONObject();

            if (fields.size() == 0 && fieldOpers.size() == 0)
                throw new JiraException("No fields were given for update");

            for (Map.Entry<String, Object> ent : fields.entrySet()) {
                Object newval = Field.toJson(ent.getKey(), ent.getValue(), editmeta);
                fieldmap.put(ent.getKey(), newval);
            }

            for (Map.Entry<String, List> ent : fieldOpers.entrySet()) {
                Object newval = Field.toJson(ent.getKey(), ent.getValue(), editmeta);
                updatemap.put(ent.getKey(), newval);
            }

            JSONObject req = new JSONObject();

            if (fieldmap.size() > 0)
                req.put("fields", fieldmap);

            if (updatemap.size() > 0)
                req.put("update", updatemap);

            try {
                restclient.put(getRestUri(key), req);
            } catch (Exception ex) {
                throw new JiraException("Failed to update issue " + key, ex);
            }
        }

        /**
         * Appends a field to the update action.
         *
         * @param name Name of the field
         * @param value New field value
         *
         * @return the current fluent update instance
         */
        @NotNull
        public FluentUpdate field(String name, Object value) {
            fields.put(name, value);
            return this;
        }

        @NotNull
        private FluentUpdate fieldOperation(String oper, String name, Object value) {
            if (!fieldOpers.containsKey(name))
                fieldOpers.put(name, new ArrayList());

            fieldOpers.get(name).add(new Field.Operation(oper, value));
            return this;
        }

        /**
         *  Adds a field value to the existing value set.
         *
         *  @param name Name of the field
         *  @param value Field value to append
         *
         *  @return the current fluent update instance
         */
        @NotNull
        public FluentUpdate fieldAdd(String name, Object value) {
            return fieldOperation("add", name, value);
        }

        /**
         *  Removes a field value from the existing value set.
         *
         *  @param name Name of the field
         *  @param value Field value to remove
         *
         *  @return the current fluent update instance
         */
        @NotNull
        public FluentUpdate fieldRemove(String name, Object value) {
            return fieldOperation("remove", name, value);
        }
    }

    /**
     * Reloads issue data from the JIRA server (issue includes all navigable
     * fields).
     *
     * @throws JiraException when the retrieval fails
     */
    public void refresh() throws JiraException {
        JSONObject result = realGet(restclient, key, new HashMap<String, String>());
        deserialise(result);
    }

    /**
     * Reloads issue data from the JIRA server and specify which fields to
     * retrieve.
     *
     * @param includedFields Specifies which issue fields will be included in
     * the result.
     * <br>Some examples how this parameter works:
     * <ul>
     * <li>*all - include all fields</li>
     * <li>*navigable - include just navigable fields</li>
     * <li>summary,comment - include just the summary and comments</li>
     * <li>*all,-comment - include all fields</li>
     * </ul>
     *
     * @throws JiraException when the retrieval fails
     */
    public void refresh(final String includedFields) throws JiraException {

        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("fields", includedFields);
        JSONObject result = realGet(restclient, key, queryParams);
        deserialise(result);
    }

    /**
     * Gets an arbitrary field by its name.
     *
     * @param name Name of the field to retrieve
     *
     * @return the field value or null if not found
     */
    @Nullable
    public Object getField(String name) {

        return fields != null ? fields.get(name) : null;
    }

    /**
     * Begins a transition field chain.
     *
     * @return a fluent transition instance
     *
     * @throws JiraException when the client fails to retrieve issue metadata
     */
    @NotNull
    public FluentTransition transition() throws JiraException {
        return new FluentTransition(getTransitions());
    }

    /**
     * Begins an update field chain.
     *
     * @return a fluent update instance
     *
     * @throws JiraException when the client fails to retrieve issue metadata
     */
    @NotNull
    public FluentUpdate update() throws JiraException {
        return new FluentUpdate(getEditMetadata());
    }

    /**
     * Casts a vote in favour of an issue.
     *
     * @throws JiraException when the voting fails
     */
    public void vote() throws JiraException {

        try {
            restclient.post(getRestUri(key) + "/votes");
        } catch (Exception ex) {
            throw new JiraException("Failed to vote on issue " + key, ex);
        }
    }

    /**
     * Removes the current user's vote from the issue.
     *
     * @throws JiraException when the voting fails
     */
    public void unvote() throws JiraException {

        try {
            restclient.delete(getRestUri(key) + "/votes");
        } catch (Exception ex) {
            throw new JiraException("Failed to unvote on issue " + key, ex);
        }
    }

    /**
     * Adds a watcher to the issue.
     *
     * @param username Username of the watcher to add
     *
     * @throws JiraException when the operation fails
     */
    public void addWatcher(String username) throws JiraException {

        try {
            URI uri = restclient.buildURI(getRestUri(key) + "/watchers");
            restclient.post(uri, username);
        } catch (Exception ex) {
            throw new JiraException(
                "Failed to add watcher (" + username + ") to issue " + key, ex
            );
        }
    }

    /**
     * Used to chain fields to a transition action.
     */
    public final class FluentTransition {

        @NotNull
        final
        Map<String, Object> fields = new HashMap<String, Object>();
        @Nullable
        List<Transition> transitions = null;

        private FluentTransition(@Nullable List<Transition> transitions) {
            this.transitions = transitions;
        }

        @Nullable
        private Transition getTransition(@NotNull String id, boolean isName) throws JiraException {
            Transition result = null;

            for (Transition transition : transitions) {
                if((isName && id.equals(transition.getName())
                || (!isName && id.equals(transition.getId())))){
                    result = transition;
                }
            }

            if (result == null) {
                final String allTransitionNames = Arrays.toString(transitions.toArray());
                throw new JiraException("Transition '" + id + "' was not found. Known transitions are:" + allTransitionNames);
            }

            return result;
        }

        private void realExecute(@Nullable Transition trans) throws JiraException {

            if (trans == null || trans.getFields() == null)
                throw new JiraException("Transition is missing fields");

            JSONObject fieldmap = new JSONObject();

            for (Map.Entry<String, Object> ent : fields.entrySet()) {
                fieldmap.put(ent.getKey(), ent.getValue());
            }

            JSONObject req = new JSONObject();

            if (fieldmap.size() > 0)
                req.put("fields", fieldmap);

            JSONObject t = new JSONObject();
            t.put("id", Field.getString(trans.getId()));

            req.put("transition", t);

            try {
                restclient.post(getRestUri(key) + "/transitions", req);
            } catch (Exception ex) {
                throw new JiraException("Failed to transition issue " + key, ex);
            }
        }

        /**
         * Executes the transition action.
         *
         * @param id Internal transition ID
         *
         * @throws JiraException when the transition fails
         */
        public void execute(int id) throws JiraException {
            realExecute(getTransition(Integer.toString(id), false));
        }

        /**
         * Executes the transition action.
         *
         * @param transition Transition
         *
         * @throws JiraException when the transition fails
         */
        public void execute(Transition transition) throws JiraException {
            realExecute(transition);
        }

        /**
         * Executes the transition action.
         *
         * @param name Transition name
         *
         * @throws JiraException when the transition fails
         */
        public void execute(@NotNull String name) throws JiraException {
            realExecute(getTransition(name, true));
        }

        /**
         * Appends a field to the transition action.
         *
         * @param name Name of the field
         * @param value New field value
         *
         * @return the current fluent transition instance
         */
        @NotNull
        public FluentTransition field(String name, Object value) {
            fields.put(name, value);
            return this;
        }
    }

    @Nullable
    @Override
    public String toString() {
        return getKey();
    }

    @Nullable
    public ChangeLog getChangeLog() {
        return changeLog;
    }

    @Nullable
    public String getKey() {
        return key;
    }

    @Nullable
    public User getAssignee() {
        return assignee;
    }

    @Nullable
    public List<Attachment> getAttachments() {
        return attachments;
    }

    @Nullable
    public List<Comment> getComments() {
        return comments;
    }

    @Nullable
    public List<Component> getComponents() {
        return components;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @Nullable
    public Date getDueDate() {
        return dueDate;
    }

    @Nullable
    public List<Version> getFixVersions() {
        return fixVersions;
    }

    @Nullable
    public List<IssueLink> getIssueLinks() {
        return issueLinks;
    }

    @Nullable
    public IssueType getIssueType() {
        return issueType;
    }

    @Nullable
    public List<String> getLabels() {
        return labels;
    }

    @Nullable
    public Issue getParent() {
        return parent;
    }

    @Nullable
    public Priority getPriority() {
        return priority;
    }

    @Nullable
    public Project getProject() {
        return project;
    }

    @Nullable
    public User getReporter() {
        return reporter;
    }

    @NotNull
    public List<RemoteLink> getRemoteLinks() throws JiraException {
        JSONArray obj;
        try {
            URI uri = restclient.buildURI(getRestUri(key) + "/remotelink");
            JSON json = restclient.get(uri);
            obj = (JSONArray) json;
        } catch (Exception ex) {
            throw new JiraException("Failed to get remote links for issue "
                    + key, ex);
        }

        return Field.getRemoteLinks(obj, restclient);
    }

    @Nullable
    public Resolution getResolution() {
        return resolution;
    }

    @Nullable
    public Date getResolutionDate() {
        return resolutionDate;
    }

    @Nullable
    public Status getStatus() {
        return status;
    }

    @Nullable
    public List<Issue> getSubtasks() {
        return subtasks;
    }

    @Nullable
    public String getSummary() {
        return summary;
    }

    @Nullable
    public TimeTracking getTimeTracking() {
        return timeTracking;
    }

    @Nullable
    public List<Version> getVersions() {
        return versions;
    }

    @Nullable
    public Votes getVotes() {
        return votes;
    }

    @Nullable
    public Watches getWatches() {
        return watches;
    }

    @Nullable
    public List<WorkLog> getWorkLogs() {
        return workLogs;
    }

    @NotNull
    public List<WorkLog> getAllWorkLogs() throws JiraException {
        JSONObject obj;
        try {
            URI uri = restclient.buildURI(getRestUri(key) + "/worklog");
            JSON json = restclient.get(uri);
            obj = (JSONObject) json;
        } catch (Exception ex) {
            throw new JiraException("Failed to get worklog for issue "
                    + key, ex);
        }

        return Field.getWorkLogs(obj, restclient);
    }

    @Nullable
    public Integer getTimeSpent() {
        return timeSpent;
    }

    @Nullable
    public Integer getTimeEstimate() {
        return timeEstimate;
    }

    @Nullable
    public Date getCreatedDate() {
        return createdDate;
    }

    @Nullable
    public Date getUpdatedDate() {
        return updatedDate;
    }

    @Nullable
    public Security getSecurity() {
        return security;
    }

    public boolean delete(final boolean deleteSubtasks) throws JiraException {
        boolean result;
        try {
                URI uri = restclient.buildURI(getBaseUri() + "issue/" + this.key, new HashMap<String, String>() {{
                put("deleteSubtasks", String.valueOf(deleteSubtasks));
            }});
            result = (restclient.delete(uri) == null);
        } catch (Exception ex) {
            throw new JiraException("Failed to delete issue " + key, ex);
        }
        return result;
    }

}

