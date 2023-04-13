package fpt.edu.stafflink.components;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.adapters.CustomFilePickerAdapter;
import fpt.edu.stafflink.models.others.SelectedAttachment;

public class CustomFilePickerComponent extends LinearLayout {
    private static final String DEFAULT_MAIN_FIELD = "id";

    RecyclerView customFilePickerComponentMainElement;
    public CustomFilePickerAdapter adapter;

    private DownloadHandler downloadHandler;
    private RemoveHandler removeHandler;


    public CustomFilePickerComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initView(context);
        this.setAttributes(attrs);
    }

    private void initView(Context context) {
        View view = inflate(context, R.layout.component_selected_list_custom, this);
        customFilePickerComponentMainElement = view.findViewById(R.id.customSelectedListComponentMainElement);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        customFilePickerComponentMainElement.setLayoutManager(layoutManager);

        adapter = new CustomFilePickerAdapter(new ArrayList<>(), DEFAULT_MAIN_FIELD) {

            @Override
            public void onItemLongClick(View view, int position, SelectedAttachment selectedAttachment) {
                showMenuOnLongClickItem(view, position, selectedAttachment);
            }
        };
        customFilePickerComponentMainElement.setAdapter(adapter);
    }

    public void showMenuOnLongClickItem(View view, int position, SelectedAttachment selectedAttachment) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_file_picker, popupMenu.getMenu());

        popupMenu.getMenu().findItem(R.id.menuFilePickerDownload).setEnabled(this.adapter.isDownloadable());
        popupMenu.getMenu().findItem(R.id.menuFilePickerRemove).setEnabled(this.adapter.isDownloadable());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.menuFilePickerDownload) {
                this.downloadAttachment(position, selectedAttachment);
            } else if (menuItem.getItemId() == R.id.menuFilePickerRemove) {
                this.onClickRemove(position, selectedAttachment);
            }
            return true;
        });
    }

    private void onClickRemove(int position, SelectedAttachment selectedAttachment) {
        AlertDialog.Builder alert = new AlertDialog.Builder(
                getContext());
        alert.setTitle("Alert!!");
        alert.setMessage("Are you sure to delete this attachment?");
        alert.setPositiveButton("YES", (dialogInterface, i) -> {
            this.deleteAttachment(position, selectedAttachment);
            dialogInterface.dismiss();
        });
        alert.setNegativeButton("NO", (dialogInterface, i) -> dialogInterface.dismiss());
        alert.show();
    }

    public void deleteAttachment(int position, SelectedAttachment selectedAttachment) {
        if (removeHandler != null) {
            removeHandler.handle(position, selectedAttachment);
        }
    }

    public void downloadAttachment(int position, SelectedAttachment selectedAttachment) {
        if (downloadHandler != null) {
            downloadHandler.handle(position, selectedAttachment);
        }
//        CustomFilePickerAdapter.ViewHolder viewHolder = (CustomFilePickerAdapter.ViewHolder) customFilePickerComponentMainElement.getChildViewHolder(customFilePickerComponentMainElement.getChildAt(position));
//        ProgressBar progressBar = viewHolder.itemFilePickerProgressbar;
//
//        progressBar.setVisibility(VISIBLE);
//
//        ExecutorService executor = Executors.newSingleThreadScheduledExecutor();
//        Handler handler = new Handler(Looper.getMainLooper());
//
//        executor.execute(() -> {
//            try {
//                StringBuilder stringBuilder = new StringBuilder();
//                stringBuilder.append(getContext().getString(R.string.default_domain));
//                stringBuilder.append(getContext().getString(R.string.attachment_files_path));
//                stringBuilder.append("/");
//                stringBuilder.append(selectedAttachment.getTaskId());
//                stringBuilder.append("/");
//                stringBuilder.append(selectedAttachment.getName());
//
//                URL url = new URL(stringBuilder.toString());
//                URLConnection connection = url.openConnection();
//                connection.connect();
//
//                int lengthOfFile = connection.getContentLength();
//
////                download file
//                InputStream input = connection.getInputStream();
//                OutputStream output = Files.newOutputStream(Paths.get(Environment.getExternalStorageDirectory().toString() + File.separator + selectedAttachment));
//
//                byte[] bytes = new byte[1024];
//
//                long total = 0;
//                int count;
//                while ((count = input.read(bytes)) != -1) {
//                    total += count;
//
////                    publish the progress
//                    this.publishProgress(progressBar, (int) ((total * 100) / lengthOfFile));
//
//                    output.write(bytes, 0, count);
//                }
////                flushing output
//                output.flush();
//
////                closing streams
//                output.close();
//                input.close();
//
//                handler.post(() -> {
////                    UI Thread work here
//                    progressBar.setVisibility(View.GONE);
//                });
//
//
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
    }

    private void publishProgress(ProgressBar progressBar, Integer... progress) {
        progressBar.setProgress(progress[0]);
    }

    public void setData(List<SelectedAttachment> objects, String mainField) {
        this.adapter.setData(objects,  mainField);
    }

    public void setObjects(List<SelectedAttachment> objects) {
        this.adapter.setObjects(objects);
    }

    public List<SelectedAttachment> getObjects() {
        return this.adapter.getObjects();
    }

    public void addNewItem(SelectedAttachment object) {
        this.adapter.addNewItem(object);
    }

    public void setMainField(String mainField) {
        this.adapter.setMainField(mainField);
    }

    public String getMainField() {
        return this.adapter.getMainField();
    }

    public void setAction(String action) {
        this.adapter.setAction(action);
    }

    public String getAction() {
        return this.adapter.getAction();
    }

    public void setCancellable(boolean cancellable) {
        this.adapter.setRemovable(cancellable);
    }

    public boolean isCancellable() {
        return this.adapter.isRemovable();
    }

    public void setDownloadable(boolean downloadable) {
        this.adapter.setDownloadable(downloadable);
    }

    public boolean getDownloadable() {
        return this.adapter.isDownloadable();
    }

    public void setDownloadHandler(DownloadHandler downloadHandler) {
        this.downloadHandler = downloadHandler;
    }

    public void setRemoveHandler(RemoveHandler removeHandler) {
        this.removeHandler = removeHandler;
    }

    private void setAttributes(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CustomFilePickerComponent, 0, 0);
        try {
            boolean cancellable = typedArray.getBoolean(R.styleable.CustomFilePickerComponent_deletable, false);
            this.setCancellable(cancellable);

            boolean downloadable = typedArray.getBoolean(R.styleable.CustomFilePickerComponent_downloadable, false);
            this.setDownloadable(downloadable);
        } finally {
            typedArray.recycle();
        }
    }

    public interface DownloadHandler {
        void handle(int position, SelectedAttachment selectedAttachment);
    }

    public interface RemoveHandler {
        void handle(int position, SelectedAttachment selectedAttachment);
    }
}
