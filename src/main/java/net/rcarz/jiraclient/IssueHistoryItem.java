package net.rcarz.jiraclient;

import net.sf.json.JSONObject;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class IssueHistoryItem extends Resource {

    @Nullable
    private String field;
    @Nullable
    private String from;
    @Nullable
    private String to;
    @Nullable
    private String fromStr;
    @Nullable
    private String toStr;

    public IssueHistoryItem(RestClient restclient) {
        super(restclient);
    }

    public IssueHistoryItem(RestClient restclient, @Nullable JSONObject json) {
        this(restclient);
        if (json != null) {
            deserialise(restclient,json);
        }
    }

    private void deserialise(RestClient restclient, JSONObject json) {
        Map map = json;
        self = Field.getString(map.get("self"));
        id = Field.getString(map.get("id"));
        field = Field.getString(map.get("field"));
        from = Field.getString(map.get("from"));
        to = Field.getString(map.get("to"));
        fromStr = Field.getString(map.get("fromString"));
        toStr = Field.getString(map.get("toString"));
    }

    @Nullable
    public String getField() {
        return field;
    }

    @Nullable
    public String getFrom() {
        return from;
    }

    @Nullable
    public String getTo() {
        return to;
    }

    @Nullable
    public String getFromStr() {
        return fromStr;
    }

    @Nullable
    public String getToStr() {
        return toStr;
    }
    
}
