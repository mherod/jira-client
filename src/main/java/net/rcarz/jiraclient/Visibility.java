package net.rcarz.jiraclient;

import net.sf.json.JSONObject;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Created by dgigon on 14/09/16.
 */
public class Visibility extends Resource {
    @Nullable
    private String type;
    @Nullable
    private String value;

    @Nullable
    public String getValue() {
        return value;
    }

    @Nullable
    public String getType() {
        return type;
    }

    protected Visibility(RestClient restclient, @Nullable JSONObject json) {
        super(restclient);

        if (json != null)
            deserialise(json);
    }

    private void deserialise(JSONObject json) {
        Map map = json;

        type = Field.getString(map.get("type"));
        value = Field.getString(map.get("value"));
    }
}
