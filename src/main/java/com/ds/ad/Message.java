package com.ds.ad;

import java.util.Map;

public class Message {
    private String id;
    private String content;

    public Message() {}

    public Map<String, String> toMap() {
        return Map.of("id", id, "content", content);
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public String getId() {
        return id;
    }
}
