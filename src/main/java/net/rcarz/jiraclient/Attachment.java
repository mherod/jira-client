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
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

/**
 * Represents an issue attachment.
 */
public class Attachment extends Resource {

    @Nullable
    private User author = null;
    @Nullable
    private String filename = null;
    @Nullable
    private Date created = null;
    private int size = 0;
    @Nullable
    private String mimeType = null;
    @Nullable
    private String content = null;

    /**
     * Creates an attachment from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected Attachment(RestClient restclient, @Nullable JSONObject json) {
        super(restclient);

        if (json != null)
            deserialise(json);
    }

    private void deserialise(JSONObject json) {

        self = Field.getString(((Map) json).get("self"));
        id = Field.getString(((Map) json).get("id"));
        author = Field.getResource(User.class, ((Map) json).get("author"), restclient);
        filename = Field.getString(((Map) json).get("filename"));
        created = Field.getDate(((Map) json).get("created"));
        size = Field.getInteger(((Map) json).get("size"));
        mimeType = Field.getString(((Map) json).get("mimeType"));
        content = Field.getString(((Map) json).get("content"));
    }

    /**
     * Retrieves the given attachment record.
     *
     * @param restclient REST client instance
     * @param id Internal JIRA ID of the attachment
     *
     * @return an attachment instance
     *
     * @throws JiraException when the retrieval fails
     */
    public static Attachment get(@NotNull RestClient restclient, String id)
        throws JiraException {

        JSON result = null;

        try {
            result = restclient.get(getBaseUri() + "attachment/" + id);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve attachment " + id, ex);
        }

        if (!(result instanceof JSONObject))
            throw new JiraException("JSON payload is malformed");

        return new Attachment(restclient, (JSONObject)result);
    }
    
    /**
     * Downloads attachment to byte array
     *
     * @return a byte[]
     *
     * @throws JiraException when the download fails
     */
    public byte[] download() 
    	throws JiraException{
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	try{
        	HttpGet get = new HttpGet(content);
        	HttpResponse response = restclient.getHttpClient().execute(get);
        	HttpEntity entity = response.getEntity();
        	if (entity != null) {
        	    InputStream inputStream = entity.getContent();
        	    int next = inputStream.read();
        	    while (next > -1) {
        	        bos.write(next);
        	        next = inputStream.read();
        	    }
        	    bos.flush();
        	}
    	}catch(IOException e){
    		  throw new JiraException(String.format("Failed downloading attachment from %s: %s", this.content, e.getMessage()));
    	}
    	return bos.toByteArray();
    }

    @Nullable
    @Override
    public String toString() {
        return getContentUrl();
    }

    @Nullable
    public User getAuthor() {
        return author;
    }

    @Nullable
    public Date getCreatedDate() {
        return created;
    }

    @Nullable
    public String getContentUrl() {
        return content;
    }

    @Nullable
    public String getFileName() {
        return filename;
    }

    @Nullable
    public String getMimeType() {
        return mimeType;
    }

    public int getSize() {
        return size;
    }
}

