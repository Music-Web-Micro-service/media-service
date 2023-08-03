package com.freemusic.mediaservice.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MusicInfoRequest {
    private int bannedOrArtistId;
    private boolean isBanned;
    private String title;

    private String lyric;

    private int albumId;

    private List<Integer> genreIds;

    @Override
    public String toString() {
        return "MusicInfoRequest{" +
                "title='" + title + '\'' +
                ", isBanned=" + isBanned +
                ", bannedOrArtistId=" + bannedOrArtistId +
                // other fields...
                '}';
    }
}
