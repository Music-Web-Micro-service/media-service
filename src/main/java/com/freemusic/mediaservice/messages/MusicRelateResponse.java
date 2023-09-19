package com.freemusic.mediaservice.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MusicRelateResponse {
    private String musicUrl;
    private String imageUrl;
}
