package com.example.Mediaplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public final class AudioFileEntryAdapter extends ArrayAdapter<AudioFileEntry> {
    private final int audioFileItemLayoutResource;

    public AudioFileEntryAdapter(final Context context, final int audioFileItemLayoutResource) {
        super(context, 0);
        this.audioFileItemLayoutResource = audioFileItemLayoutResource;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        // We need to get the best view (re-used if possible) and then
        // retrieve its corresponding ViewHolder, which optimizes lookup efficiency
        final View view = getWorkingView(convertView);
        final ViewHolder viewHolder = getViewHolder(view);
        final AudioFileEntry entry = getItem(position);
        // Setting the title view is straightforward
        viewHolder.titleView.setText(entry.getTitle());
        // Setting the subTitle view requires a tiny bit of formatting
        final String formattedSubTitle = String.format("%s - %s, %d",
                entry.getArtist(), entry.getAlbum(), entry.getRunningTime());
        viewHolder.subTitleView.setText(formattedSubTitle);
        // Setting image view is also simple
        if (PlayListActivity.currentSongIndex == position) {
            viewHolder.imageView.setImageResource(R.drawable.ic_play_arrow_black_48dp);
        } else {
            viewHolder.imageView.setImageResource(R.drawable.ic_audiotrack_black_48dp);
        }
        return view;
    }

    View getWorkingView(final View convertView) {
        // The workingView is basically just the convertView re-used if possible
        // or inflated new if not possible
        View workingView = null;
        if (null == convertView) {
            final Context context = getContext();
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            workingView = inflater.inflate(audioFileItemLayoutResource, null);
        } else {
            workingView = convertView;
        }
        return workingView;
    }

    ViewHolder getViewHolder(final View workingView) {
        // The viewHolder allows us to avoid re-looking up view references
        // Since views are recycled, these references will never change
        final Object tag = workingView.getTag();
        ViewHolder viewHolder = null;
        if (null == tag || !(tag instanceof ViewHolder)) {
            viewHolder = new ViewHolder();
            viewHolder.titleView = (TextView) workingView.findViewById(R.id.audio_file_entry_title);
            viewHolder.subTitleView = (TextView) workingView.findViewById(R.id.audio_file_entry_subtitle);
            viewHolder.imageView = (ImageView) workingView.findViewById(R.id.audio_file_entry_icon);
            workingView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) tag;
        }
        return viewHolder;
    }

    /**
     * ViewHolder allows us to avoid re-looking up view references
     * Since views are recycled, these references will never change
     */
    static class ViewHolder {
        public TextView titleView;
        public TextView subTitleView;
        public ImageView imageView;
    }
}

