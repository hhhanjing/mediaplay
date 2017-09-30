package com.example.administrator.mediaplayer;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/9/13.
 */

public class Music implements Serializable {
    private Long id;//歌曲ID
    private String title;//歌曲名称
    private String artist;//歌手
    private String url;//歌曲路径
    private long duration;//歌曲时长

    public Music() {
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
