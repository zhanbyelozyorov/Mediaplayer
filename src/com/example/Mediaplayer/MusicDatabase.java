package com.example.Mediaplayer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ByelozyorovZ on 30.07.2016.
 */
public class MusicDatabase implements Serializable {
    public Map<String, List<AudioFileEntry>> dirs = new HashMap<String, List<AudioFileEntry>>();
}
