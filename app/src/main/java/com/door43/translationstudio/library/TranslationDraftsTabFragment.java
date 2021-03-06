package com.door43.translationstudio.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.door43.translationstudio.R;
import com.door43.translationstudio.library.temp.LibraryTempData;
import com.door43.translationstudio.projects.Project;
import com.door43.translationstudio.projects.SourceLanguage;
import com.door43.translationstudio.tasks.DownloadLanguageTask;
import com.door43.translationstudio.util.AppContext;
import com.door43.translationstudio.util.TabsFragmentAdapterNotification;
import com.door43.translationstudio.util.TranslatorBaseFragment;
import com.door43.util.threads.TaskManager;
import com.door43.util.threads.ThreadableUI;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joel on 3/16/2015.
 */
public class TranslationDraftsTabFragment extends TranslatorBaseFragment implements TabsFragmentAdapterNotification {
    private LibraryLanguageAdapter mAdapter;
    private Project mProject;
    public static final String DOWNLOAD_DRAFT_PREFIX = "download-draft-";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_library_languages, container, false);

        if (getArguments().containsKey(ProjectLibraryDetailFragment.ARG_ITEM_ID)) {
            String id = getArguments().getString(ProjectLibraryDetailFragment.ARG_ITEM_ID);
            mProject = LibraryTempData.getProject(id);
        }

        mAdapter = new LibraryLanguageAdapter(AppContext.context(), mProject.getId(), DOWNLOAD_DRAFT_PREFIX, true);

        ListView list = (ListView)view.findViewById(R.id.listView);
        list.setAdapter(mAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(LibraryTempData.getEnableEditing()) {
                    // TODO: place this in a task
                    SourceLanguage lang = mAdapter.getItem(i);
                    AppContext.projectManager().deleteSourceLanguage(mProject.getId(), lang.getId());
                    mAdapter.notifyDataSetChanged();
                } else {
                    SourceLanguage lang = mAdapter.getItem(i);
                    connectDownloadTask(lang);
                }
            }
        });

        populateList();

        return view;
    }

    /**
     * Begins or connects to an existing download
     * @param language
     */
    private void connectDownloadTask(SourceLanguage language) {
        String taskId = DOWNLOAD_DRAFT_PREFIX +mProject.getId()+"-"+language.getId();
        DownloadLanguageTask task = (DownloadLanguageTask)TaskManager.getTask(taskId);
        if(task == null) {
            // start new download
            task = new DownloadLanguageTask(mProject, language);
            TaskManager.addTask(task, taskId);
            // NOTE: the LibraryLanguageAdapter handles the onProgress and onFinish events
            mAdapter.notifyDataSetChanged();
        }
    }

    private void populateList() {
        // filter languages
        // TODO: it would be safer to put this in the task manager
        new ThreadableUI(getActivity()) {
            List<SourceLanguage> languages = new ArrayList<>();
            @Override
            public void onStop() {

            }

            @Override
            public void run() {
                if(mProject != null) {
                    for(SourceLanguage l: mProject.getSourceLanguages()) {
                        if(l.checkingLevel() < AppContext.context().getResources().getInteger(R.integer.min_source_lang_checking_level)) {
                            languages.add(l);
                        }
                    }
                }
            }

            @Override
            public void onPostExecute() {
                if(mAdapter != null) {
                    mAdapter.changeDataSet(languages);
                }
                if(languages.size() == 0) {
                    // let the parent view know there's nothing to show
                    if(getActivity() != null) {
                        if(!(getActivity() instanceof Callbacks)) {
                            throw new IllegalStateException("Activity must implement fragment's callbacks.");
                        }
                        ((Callbacks)getActivity()).onEmptyDraftsList();
                    }
                }
            }
        }.start();
    }

    @Override
    public void NotifyAdapterDataSetChanged() {
        populateList();
    }

    public static interface Callbacks {
        public void onEmptyDraftsList();
    }
}
