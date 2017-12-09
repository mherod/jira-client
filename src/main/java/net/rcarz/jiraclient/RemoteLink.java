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

        self = Field.getString(((Map) json).get("self"));
        id = Field.getString(((Map) json).get("id"));
        
        Map object = (Map) ((Map) json).get("object");
        
        remoteUrl = Field.getString(object.get("url"));
        title = Field.getString(object.get("title"));
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    @Nullable
    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(@Nullable String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }
}
