package com.example.Mediaplayer;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.*;

/**
 * Created by ByelozyorovZ on 30.07.2016.
 */
public class NavigatorActivity extends ListActivity {
    private String bigPath = "/sdcard";
    private String chosenPath = null;
    Set<String> musicDirs = new HashSet<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // getActionBar().setDisplayHomeAsUpEnabled(true); // для UpNavigation - возвращает в родительскую активность

        musicDirs.add(bigPath);

        List<String> folders = new ArrayList();
        findMusicDirs(bigPath);
        folders.addAll(musicDirs);
        Collections.sort(folders);

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, folders);
        setListAdapter(adapter);
    }

    private void findMusicDirs(String path) {
        File dir = new File(path);

        String[] list = dir.list();
        if (list != null) {
            for (String fileName : list) {
                String folderPath = new String();
                folderPath = path;
                String filePath = new String();
                filePath = path;
                if (path.endsWith(File.separator)) {
                    filePath += fileName; // "/foo/bar/" + "baz.jpeg"
                } else {
                    filePath += File.separator + fileName; // "foo/bar" + "/" + "baz.jpeg"
                }

                File file = new File(filePath);
                if (isMusicFile(file)) {
                    musicDirs.add(file.getParent());
                } else if (file.isDirectory()) {
                    findMusicDirs(filePath);
                }
            }
        }
    }

    private boolean isMusicFile(File file) {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        int index = file.getName().lastIndexOf('.') + 1;
        String ext = file.getName().substring(index).toLowerCase();
        String type = mime.getMimeTypeFromExtension(ext);
        if (type != null && type.equals("audio/mpeg")) {
            return true;
        } else return false;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        chosenPath = l.getItemAtPosition(position).toString();
        Intent intent = new Intent(this, PlayListActivity.class);
        intent.putExtra("chosenPath", chosenPath);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
