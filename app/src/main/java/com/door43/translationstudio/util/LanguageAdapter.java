package com.door43.translationstudio.util;

import android.content.Context;
import android.media.Image;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.door43.translationstudio.R;
import com.door43.translationstudio.projects.Language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by joel on 10/7/2014.
 */
public class LanguageAdapter extends ArrayAdapter<Language> implements Filterable {
    private List<Language> mLanguageList;
    private List<Language> mOrigLanguageList = new ArrayList<Language>();
    private Context mContext;
    private LanguageFilter mLanguageFilter;
    private final Boolean mIsSourceLanguages;

    public LanguageAdapter(final List<Language> languageList, Context context, Boolean isSourceLanguages) {
        super(context, R.layout.fragment_target_language_list_item, languageList);
        mIsSourceLanguages = isSourceLanguages;
        mContext = context;
        mOrigLanguageList = languageList;
        mLanguageList = languageList;
        // place translated languages at the top of the list
        if(!isSourceLanguages) {
            Handler handler = new Handler();
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    List<Language> tempList = new ArrayList<Language>();

                    // sort target languages placing those with progress on top.
                    ListIterator<Language> li = languageList.listIterator();
                    int translatedIndex = 0;
                    while(li.hasNext()) {
                        Language l = li.next();
                        if(l.isTranslating(((TranslatorBaseActivity)mContext).app().getSharedProjectManager().getSelectedProject())) {
                            tempList.add(translatedIndex, l);
                            translatedIndex ++;
                        } else {
                            tempList.add(l);
                        }
                    }
                    mLanguageList = tempList;
                    mOrigLanguageList = tempList;
                    LanguageAdapter.this.notifyDataSetChanged();
                }
            };
            handler.postDelayed(r, 100);
        }
    }

    @Override
    public int getCount() {
        return mLanguageList.size();
    }

    public Language getItem(int position) {
        return mLanguageList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        LanguageHolder holder = new LanguageHolder();
        Language l = mLanguageList.get(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.fragment_target_language_list_item, null);
            TextView nameText = (TextView) v.findViewById(R.id.target_language_name);
            TextView idText = (TextView) v.findViewById(R.id.target_language_id);
            ImageView wrenchImage = (ImageView) v.findViewById(R.id.translationStatusIcon);
            holder.languageNameView = nameText;
            holder.languageIdView = idText;
            holder.translationStatusIcon = wrenchImage;
            v.setTag(holder);
        } else {
            holder = (LanguageHolder)v.getTag();
        }


        holder.languageNameView.setText(l.getName());
        holder.languageIdView.setText(l.getId());
        if(!mIsSourceLanguages && l.isTranslating(((TranslatorBaseActivity)mContext).app().getSharedProjectManager().getSelectedProject())) {
            holder.translationStatusIcon.setVisibility(View.VISIBLE);
        } else {
            holder.translationStatusIcon.setVisibility(View.GONE);
        }
        return v;
    }

    public void resetData() {
        mLanguageList = mOrigLanguageList;
    }

    /**
     * Improves performance
     */
    private static class LanguageHolder {
        public TextView languageNameView;
        public TextView languageIdView;
        public ImageView translationStatusIcon;
    }

    @Override
    public Filter getFilter() {
        if(mLanguageFilter == null) {
            mLanguageFilter = new LanguageFilter();
        }
        return mLanguageFilter;
    }

    /**
     * Custom filter
     */
    private class LanguageFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();
            if(charSequence == null || charSequence.length() == 0) {
                // no filter
                results.values =mOrigLanguageList;
                results.count = mOrigLanguageList.size();
            } else {
                // perform filter
                List<Language> filteredLanguageList = new ArrayList<Language>();
                for(Language l : mLanguageList) {
                    if(l.getId().toUpperCase().startsWith(charSequence.toString().toUpperCase()) || l.getName().toUpperCase().startsWith(charSequence.toString().toUpperCase())) {
                        filteredLanguageList.add(l);
                    }
                }
                results.values = filteredLanguageList;
                results.count = filteredLanguageList.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            if(filterResults.count == 0) {
                notifyDataSetInvalidated();
            } else {
                mLanguageList = (List<Language>) filterResults.values;
                notifyDataSetChanged();
            }
        }
    }
}
