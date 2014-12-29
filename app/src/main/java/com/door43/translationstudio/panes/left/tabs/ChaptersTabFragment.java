package com.door43.translationstudio.panes.left.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.door43.translationstudio.MainActivity;
import com.door43.translationstudio.R;
import com.door43.translationstudio.util.TabsFragmentAdapterNotification;
import com.door43.translationstudio.util.TranslatorBaseFragment;

/**
 * Created by joel on 8/29/2014.
 */
public class ChaptersTabFragment extends TranslatorBaseFragment implements TabsFragmentAdapterNotification{
    private ChaptersTabFragment me = this;
    private ModelItemAdapter mModelItemAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pane_left_chapters, container, false);
        ListView listView = (ListView)view.findViewById(R.id.chapters_list_view);

        // create adapter
        if(mModelItemAdapter == null) mModelItemAdapter = new ModelItemAdapter(app(), app().getSharedProjectManager().getSelectedProject().getChapters());

        // connectAsync adapter
        listView.setAdapter(mModelItemAdapter);
        listView.deferNotifyDataSetChanged();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // save changes to the current frame first
                ((MainActivity)me.getActivity()).save();
                // select the chapter
                app().getSharedProjectManager().getSelectedProject().setSelectedChapter(i);
                // reload the center pane so we don't accidently overwrite a frame
                ((MainActivity)me.getActivity()).reloadCenterPane();
                // open up the frames tab
                ((MainActivity)me.getActivity()).getLeftPane().selectTab(2);
                // let the adapter redraw itself so the selected chapter is corectly highlighted
                NotifyAdapterDataSetChanged();
            }
        });

        return view;
    }

    @Override
    public void NotifyAdapterDataSetChanged() {
        if(mModelItemAdapter != null) {
            mModelItemAdapter.changeDataSet(app().getSharedProjectManager().getSelectedProject().getChapters());
        }
    }
}
