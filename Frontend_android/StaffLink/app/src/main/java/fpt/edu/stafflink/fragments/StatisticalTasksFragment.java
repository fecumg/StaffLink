package fpt.edu.stafflink.fragments;

import static fpt.edu.stafflink.TaskChartActivity.STATISTICAL_TASK_ACTION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PROJECT_ACCESS_TYPE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_STRING_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_TASK_STATUS;

import android.os.Bundle;
import android.view.View;

public class StatisticalTasksFragment extends TasksFragment{

    public StatisticalTasksFragment() {
    }

    public static StatisticalTasksFragment newInstance(String id, int accessType, int taskStatus) {
        StatisticalTasksFragment fragment = new StatisticalTasksFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_STRING_ID, id);
        args.putInt(PARAM_PROJECT_ACCESS_TYPE, accessType);
        args.putInt(PARAM_TASK_STATUS, taskStatus);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    protected void initList() {
        listTasks.setTitleField("tree");
        listTasks.setContentField("description");
        listTasks.setAction(STATISTICAL_TASK_ACTION);
        listTasks.setError(null);

        inputTextSearchTasks.setVisibility(View.GONE);
    }
}
