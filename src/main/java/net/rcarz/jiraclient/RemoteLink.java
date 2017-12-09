package net.rcarz.jiraclient;

import net.sf.json.JSONObject;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class RemoteLink extends Resource {
    @Nullable
    private String remoteUrl;
    @Nullable
    private String title;

    public RemoteLink(RestClient restclient, @Nullable JSONObject json) {
        super(restclient);
        if (json != null)
            deserialise(json);
    }

    private void deserialise(JSONObject json) {
        Map map = json;

        self = Field.getString(map.get("self"));
        id = Field.getString(map.get("id"));
        
        Map object = (Map)map.get("object");
        
        remoteUrl = Field.getString(object.get("url"));
        title = Field.getString(object.get("title"));
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Nullable
    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }
}
