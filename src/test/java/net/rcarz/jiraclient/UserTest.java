package net.rcarz.jiraclient;

import net.sf.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;

public class UserTest {

    @NotNull
    private String username = "joseph";
    @NotNull
    private String displayName = "Joseph McCarthy";
    @NotNull
    private String email = "joseph.b.mccarthy2012@googlemail.com";
    @NotNull
    private String userID = "10";

    @Test
    public void testJSONDeserializer() throws URISyntaxException {
        User user = new User(new RestClient(null, new URI("/123/asd")), getTestJSON());
        assertEquals(user.getName(), username);
        assertEquals(user.getDisplayName(), displayName);
        assertEquals(user.getEmail(), email);
        assertEquals(user.getId(), userID);

        Map<String, String> avatars = user.getAvatarUrls();

        assertEquals("https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=16", avatars.get("16x16"));
        assertEquals("https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=24", avatars.get("24x24"));
        assertEquals("https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=32", avatars.get("32x32"));
        assertEquals("https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=48", avatars.get("48x48"));

        assertTrue(user.isActive());
    }

    @NotNull
    private JSONObject getTestJSON() {
        JSONObject json = new JSONObject();

        json.put("name", username);
        json.put("email", email);
        boolean isActive = true;
        json.put("active", isActive);
        json.put("displayName", displayName);
        String self = "https://brainbubble.atlassian.net/rest/api/2/user?username=joseph";
        json.put("self", self);

        JSONObject images = new JSONObject();
        images.put("16x16", "https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=16");
        images.put("24x24", "https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=24");
        images.put("32x32", "https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=32");
        images.put("48x48", "https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=48");

        json.put("avatarUrls", images);
        json.put("id", "10");

        return json;
    }

    @Test
    public void testStatusToString() throws URISyntaxException {
        User user = new User(new RestClient(null, new URI("/123/asd")), getTestJSON());
        assertEquals(username, user.toString());
    }

    @Test(expected = JiraException.class)
    public void testGetUserJSONError() throws Exception {

        final RestClient restClient = PowerMockito.mock(RestClient.class);
        when(restClient.get(anyString(),anyMap())).thenReturn(null);
         User.get(restClient, "username");

    }

    @Test(expected = JiraException.class)
    public void testGetUserRestError() throws Exception {

        final RestClient restClient = PowerMockito.mock(RestClient.class);
        when(restClient.get(anyString(),anyMap())).thenThrow(Exception.class);
       User.get(restClient, "username");
    }

    @Test
    public void testGetUser() throws Exception {

        final RestClient restClient = PowerMockito.mock(RestClient.class);
        when(restClient.get(anyString(),anyMap())).thenReturn(getTestJSON());
        final User user = User.get(restClient, "username");

        assertEquals(user.getName(), username);
        assertEquals(user.getDisplayName(), displayName);
        assertEquals(user.getEmail(), email);
        assertEquals(user.getId(), userID);

        Map<String, String> avatars = user.getAvatarUrls();

        assertEquals("https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=16", avatars.get("16x16"));
        assertEquals("https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=24", avatars.get("24x24"));
        assertEquals("https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=32", avatars.get("32x32"));
        assertEquals("https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=48", avatars.get("48x48"));

        assertTrue(user.isActive());
    }
}
