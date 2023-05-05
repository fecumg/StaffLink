package fpt.edu.stafflink;

import static fpt.edu.stafflink.constants.AdapterActionParam.DEFAULT_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_POSITION;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_PROJECT_ACCESS_TYPE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_STRING_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_TASK_NAME;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_TITLE;
import static fpt.edu.stafflink.constants.AdapterActionParam.PROJECT_ACCESS_TYPE_OBSERVABLE;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import fpt.edu.stafflink.enums.TaskStatus;
import fpt.edu.stafflink.fragments.BaseFragment;
import fpt.edu.stafflink.fragments.StatisticalTasksFragment;
import fpt.edu.stafflink.models.responseDtos.TaskStatisticResponse;
import fpt.edu.stafflink.retrofit.RetrofitServiceManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TaskChartActivity extends BaseActivity {
    private static final String ERROR_TAG = "TaskChartActivity";
    public static final String STATISTICAL_TASK_ACTION = "statisticalTaskAction";

    ImageButton buttonRefreshChart;
    TextView textViewError;
    PieChart pieChartTasks;

    TextView textViewSubTitle;

    LinearLayout layoutTaskStatistic;
    TextView textViewInitiatedAmount;
    TextView textViewInProgressAmount;
    TextView textViewOverdueAmount;

    FrameLayout fragmentTaskStatistic;

    StatisticalTasksFragment fragment;

    ActivityResultLauncher<Intent> formActivityResultLauncher;

    @Override
    protected void onSubCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_task_chart);

        buttonRefreshChart = findViewById(R.id.buttonRefreshChart);
        textViewError = findViewById(R.id.textViewError);
        pieChartTasks = findViewById(R.id.pieChartTasks);
        textViewSubTitle = findViewById(R.id.textViewSubTitle);

        layoutTaskStatistic = findViewById(R.id.layoutTaskStatistic);
        textViewInitiatedAmount = findViewById(R.id.textViewInitiatedAmount);
        textViewInProgressAmount = findViewById(R.id.textViewInProgressAmount);
        textViewOverdueAmount = findViewById(R.id.textViewOverdueAmount);

        fragmentTaskStatistic = findViewById(R.id.fragmentTaskStatistic);

        initPieChart();
        fetchTaskStatistic();

        this.setFormActivityResultLauncher();

        buttonRefreshChart.setOnClickListener(view -> refresh());

        this.listenToAdapterOnClick();
    }

    private void initPieChart() {
        pieChartTasks.setRotationEnabled(true);
        pieChartTasks.setHoleRadius(35f);
        pieChartTasks.setTransparentCircleAlpha(0);
        pieChartTasks.setCenterTextSize(13);
        pieChartTasks.setDrawEntryLabels(true);

        Description description = new Description();
        description.setText("Ongoing task status chart");
        pieChartTasks.setDescription(description);

        textViewSubTitle.setText(getString(R.string.task_statistic_sub_title));
        layoutTaskStatistic.setVisibility(View.VISIBLE);
        fragmentTaskStatistic.setVisibility(View.GONE);
    }

    private void fetchTaskStatistic() {
        Disposable disposable = RetrofitServiceManager.getTaskService(this)
                .getTaskStatistic()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->
                                super.handleGenericResponse(
                                        response,
                                        taskStatisticResponse -> {
                                            textViewError.setText(null);
                                            bindPieChartData(taskStatisticResponse);
                                        },
                                        errorApiResponse -> textViewError.setText(errorApiResponse.getMessage())
                                ),
                        error -> {
                            Log.e(ERROR_TAG, "fetchTaskStatistic: " + error.getMessage(), error);
                            super.pushToast(error.getMessage());
                            textViewError.setText(error.getMessage());
                        }
                );

        compositeDisposable.add(disposable);
    }

    private void refresh() {
        layoutTaskStatistic.setVisibility(View.VISIBLE);
        fragmentTaskStatistic.setVisibility(View.GONE);
        textViewSubTitle.setText(getString(R.string.task_statistic_sub_title));
        pieChartTasks.highlightValue(null);
        fetchTaskStatistic();
    }

    private void bindPieChartData(TaskStatisticResponse taskStatisticResponse) {
        pieChartTasks.setCenterText(taskStatisticResponse.getInitiatedTaskAmount() + taskStatisticResponse.getInProgressTaskAmount() + taskStatisticResponse.getOverdueTaskAmount() + " tasks");

        textViewInitiatedAmount.setText(String.valueOf(taskStatisticResponse.getInitiatedTaskAmount()));
        textViewInProgressAmount.setText(String.valueOf(taskStatisticResponse.getInProgressTaskAmount()));
        textViewOverdueAmount.setText(String.valueOf(taskStatisticResponse.getOverdueTaskAmount()));

        List<PieEntry> PieEntries = new ArrayList<>();
        if (taskStatisticResponse.getInitiatedTaskAmount() > 0) {
            PieEntries.add(new PieEntry(taskStatisticResponse.getInitiatedTaskAmount(), "initiated"));
        }
        if (taskStatisticResponse.getInProgressTaskAmount() > 0) {
            PieEntries.add(new PieEntry(taskStatisticResponse.getInProgressTaskAmount(), "in progress"));
        }
        if (taskStatisticResponse.getOverdueTaskAmount() > 0) {
            PieEntries.add(new PieEntry(taskStatisticResponse.getOverdueTaskAmount(), "overdue"));
        }

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(this, R.color.primary));
        colors.add(ContextCompat.getColor(this, R.color.warning));
        colors.add(ContextCompat.getColor(this, R.color.danger));

        PieDataSet pieDataSet = new PieDataSet(PieEntries, "");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(12);
        pieDataSet.setColors(colors);
        pieDataSet.setValueTextColor(ContextCompat.getColor(this, R.color.light));
        pieDataSet.setValueFormatter(new DefaultValueFormatter(0));


        PieData pieData = new PieData(pieDataSet);
        pieChartTasks.setData(pieData);
        pieChartTasks.animateXY(500, 500);
        pieChartTasks.invalidate();

        pieChartTasks.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                layoutTaskStatistic.setVisibility(View.GONE);
                fragmentTaskStatistic.setVisibility(View.VISIBLE);
                if (h.getX() == 0) {
                    textViewSubTitle.setText(getString(R.string.task_statistic_initiated_title));
                    showTasks(TaskStatus.INITIATED.getCode());
                } else if (h.getX() == 1) {
                    textViewSubTitle.setText(getString(R.string.task_statistic_in_progress_title));
                    showTasks(TaskStatus.IN_PROGRESS.getCode());
                } else if (h.getX() == 2) {
                    textViewSubTitle.setText(getString(R.string.task_statistic_overdue_title));
                    showTasks(TaskStatus.OVERDUE.getCode());
                }
            }

            @Override
            public void onNothingSelected() {
                layoutTaskStatistic.setVisibility(View.VISIBLE);
                fragmentTaskStatistic.setVisibility(View.GONE);
                textViewSubTitle.setText(getString(R.string.task_statistic_sub_title));
            }
        });
    }

    private void showTasks(int status) {
        fragment = StatisticalTasksFragment.newInstance("", PROJECT_ACCESS_TYPE_OBSERVABLE, status);
        this.replaceFragment(fragment);
    }

    private void replaceFragment(BaseFragment fragment) {
        fragment.onAttach(getBaseContext());
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentTaskStatistic, fragment);
        fragmentTransaction.commit();
    }

    private void listenToAdapterOnClick() {
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String objectId = intent.getStringExtra(PARAM_STRING_ID);
                        int objectPosition = intent.getIntExtra(PARAM_POSITION, DEFAULT_POSITION);
                        String title = intent.getStringExtra(PARAM_TITLE);
                        if (StringUtils.isNotEmpty(objectId)) {
                            Intent taskIntend = new Intent(TaskChartActivity.this, TaskAccessActivity.class);
                            taskIntend.putExtra(PARAM_STRING_ID, objectId);
                            taskIntend.putExtra(PARAM_POSITION, objectPosition);

                            taskIntend.putExtra(PARAM_TASK_NAME, title);

                            taskIntend.putExtra(PARAM_PROJECT_ACCESS_TYPE, PROJECT_ACCESS_TYPE_OBSERVABLE);

                            if (formActivityResultLauncher != null) {
                                formActivityResultLauncher.launch(taskIntend);
                            }
                        }
                    }
                }, new IntentFilter(STATISTICAL_TASK_ACTION));
    }

    private void setFormActivityResultLauncher() {
        formActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::doOnBackFromForm);
    }

    private void doOnBackFromForm(ActivityResult result) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        formActivityResultLauncher = null;
    }
}