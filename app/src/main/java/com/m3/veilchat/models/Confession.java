package com.m3.veilchat.models;

public class Confession {
    private String confessionId;
    private String content;
    private String confessorPersonaId;
    private String confessorDisplayName;
    private long timestamp;
    private int replyCount;
    private int likeCount;
    private boolean isAnonymous;
    private String moodTag;

    public Confession() {}

    public Confession(String content, String confessorPersonaId, String confessorDisplayName) {
        this.content = content;
        this.confessorPersonaId = confessorPersonaId;
        this.confessorDisplayName = confessorDisplayName;
        this.timestamp = System.currentTimeMillis();
        this.isAnonymous = true;
        this.replyCount = 0;
        this.likeCount = 0;
    }

    // Getters and Setters
    public String getConfessionId() { return confessionId; }
    public void setConfessionId(String confessionId) { this.confessionId = confessionId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getConfessorPersonaId() { return confessorPersonaId; }
    public void setConfessorPersonaId(String confessorPersonaId) { this.confessorPersonaId = confessorPersonaId; }

    public String getConfessorDisplayName() { return confessorDisplayName; }
    public void setConfessorDisplayName(String confessorDisplayName) { this.confessorDisplayName = confessorDisplayName; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getReplyCount() { return replyCount; }
    public void setReplyCount(int replyCount) { this.replyCount = replyCount; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public boolean isAnonymous() { return isAnonymous; }
    public void setAnonymous(boolean anonymous) { isAnonymous = anonymous; }

    public String getMoodTag() { return moodTag; }
    public void setMoodTag(String moodTag) { this.moodTag = moodTag; }
}