package net.rcarz.jiraclient;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class IssueHistory extends Resource {

    private static final long serialVersionUID = 1L;
    private User user;
    private ArrayList<IssueHistoryItem> changes;
    @Nullable
    private Date created;

    /**
     * Creates an issue history record from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected IssueHistory(RestClient restclient, @Nullable JSONObject json) {
        super(restclient);

        if (json != null) {
            deserialise(restclient,json);
        }
    }

    public IssueHistory(IssueHistory record, ArrayList<IssueHistoryItem> changes) {
        super(record.restclient);
        user = record.user;
        id = record.id;
        self = record.self;
        created = record.created;
        this.changes = changes;
    }

    private void deserialise(RestClient restclient, JSONObject json) {
        self = Field.getString(((Map) json).get("self"));
        id = Field.getString(((Map) json).get("id"));
        user = new User(restclient,(JSONObject) ((Map) json).get("author"));
        created = Field.getDateTime(((Map) json).get("created"));
        JSONArray items = JSONArray.fromObject(((Map) json).get("items"));
        changes = new ArrayList<IssueHistoryItem>(items.size());
        for (int i = 0; i < items.size(); i++) {
            JSONObject p = items.getJSONObject(i);
            changes.add(new IssueHistoryItem(restclient, p));
        }
    }

    public User getUser() {
        return user;
    }

    public ArrayList<IssueHistoryItem> getChanges() {
        return changes;
    }

    @Nullable
    public Date getCreated() {
        return created;
    }

}
