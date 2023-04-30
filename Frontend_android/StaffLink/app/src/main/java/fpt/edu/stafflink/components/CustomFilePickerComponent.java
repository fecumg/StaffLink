package fpt.edu.stafflink.components;

import static android.app.Activity.RESULT_OK;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.tbruyelle.rxpermissions3.RxPermissions;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fpt.edu.stafflink.BaseActivity;
import fpt.edu.stafflink.R;
import fpt.edu.stafflink.adapters.CustomFilePickerAdapter;
import fpt.edu.stafflink.models.others.SelectedAttachment;
import fpt.edu.stafflink.utilities.ActivityUtils;
import io.reactivex.rxjava3.disposables.Disposable;

public class CustomFilePickerComponent extends LinearLayout {
    private static final String DEFAULT_MAIN_FIELD = "id";

    RecyclerView customFilePickerComponentMainElement;
    public CustomFilePickerAdapter adapter;
    ImageButton customFilePickerComponentButton;

    private boolean ableToPickFile;

    private UploadHandler uploadHandler;
    private RemoveHandler removeHandler;

    private ActivityResultLauncher<Intent> pickFileActivityResultLauncher;

    private RxPermissions rxPermissions;
    private Disposable permissionDisposal;

    public CustomFilePickerComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initView(context);
        this.setAttributes(attrs);
        this.initRxPermission();
    }

    private void initView(Context context) {
        View view = inflate(context, R.layout.component_file_picker_custom, this);
        customFilePickerComponentMainElement = view.findViewById(R.id.customFilePickerComponentMainElement);
        customFilePickerComponentButton = view.findViewById(R.id.customFilePickerComponentButton);

//        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView.LayoutManager layoutManager = new FlexboxLayoutManager(getContext(), FlexDirection.ROW, FlexWrap.WRAP);
        customFilePickerComponentMainElement.setLayoutManager(layoutManager);

        adapter = new CustomFilePickerAdapter(new ArrayList<>(), DEFAULT_MAIN_FIELD) {

            @Override
            public void onItemLongClick(View view, int position, SelectedAttachment selectedAttachment) {
                showMenuOnLongClickItem(view, position, selectedAttachment);
            }
        };
        customFilePickerComponentMainElement.setAdapter(adapter);
    }

    private void initRxPermission() {
        AppCompatActivity activity = ActivityUtils.getActivity(getContext());
        if (activity == null) {
            return;
        }
        rxPermissions = new RxPermissions(activity);
    }

    public void showMenuOnLongClickItem(View view, int position, SelectedAttachment selectedAttachment) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_file_picker, popupMenu.getMenu());
        popupMenu.getMenu().findItem(R.id.menuFilePickerDownload).setVisible(this.adapter.isDownloadable());
        popupMenu.getMenu().findItem(R.id.menuFilePickerRemove).setVisible(this.adapter.isRemovable());
        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.menuFilePickerDownload) {
                this.downloadUnderPermission(position, selectedAttachment);
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

    private void deleteAttachment(int position, SelectedAttachment selectedAttachment) {
        if (this.removeHandler != null) {
            this.removeHandler.handle(position, selectedAttachment);
        }
    }

    private void downloadUnderPermission(int position, SelectedAttachment selectedAttachment) {
        AppCompatActivity activity = ActivityUtils.getActivity(getContext());
        if (activity == null) {
            return;
        }

        permissionDisposal = rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        this.downloadAttachment(position, selectedAttachment);
                    } else {
                        Toast.makeText(activity, "Storage access denied", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void downloadAttachment(int position, SelectedAttachment selectedAttachment) {
        CustomFilePickerAdapter.ViewHolder viewHolder = (CustomFilePickerAdapter.ViewHolder) customFilePickerComponentMainElement.getChildViewHolder(customFilePickerComponentMainElement.getChildAt(position));
        ProgressBar progressBar = viewHolder.itemFilePickerProgressbar;

        progressBar.setVisibility(VISIBLE);

        ExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

//        download folder
        String downloadFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getContext().getString(R.string.download_folder);
        File downloadFolder = new File(downloadFolderPath);
        if (!downloadFolder.exists()) {
            downloadFolder.mkdirs();
        }

//        download link
        String attachmentFilesPath = getContext().getString(R.string.attachment_files_path);
        String rootUri = attachmentFilesPath.startsWith("/") ? attachmentFilesPath.substring(1) : attachmentFilesPath;

        String downloadLink = getContext().getString(R.string.default_domain) +
                rootUri +
                "/" +
                selectedAttachment.getTaskId() +
                "/" +
                selectedAttachment.getName();

        executor.execute(() -> {
            try {
                this.writeFile(downloadLink, downloadFolderPath, selectedAttachment.getName(), progressBar, handler);
            } catch (FileNotFoundException e) {
                if (getContext() instanceof BaseActivity) {
                    BaseActivity baseActivity = (BaseActivity) getContext();
                    baseActivity.runOnUiThread(() -> baseActivity.pushToast("file not found"));
                }
                e.printStackTrace();
                throw new RuntimeException(e);
            }catch (IOException e) {
                if (getContext() instanceof BaseActivity) {
                    BaseActivity baseActivity = (BaseActivity) getContext();
                    baseActivity.runOnUiThread(() -> baseActivity.pushToast(e.getMessage()));
                }
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    private void writeFile(String downloadLink, String downloadFolderPath, String filename, ProgressBar progressBar, Handler handler) throws IOException {
        URL url = new URL(downloadLink);
        URLConnection connection = url.openConnection();
        connection.connect();

        if (StringUtils.isEmpty(FilenameUtils.getExtension(filename))) {
            filename += ".txt";
        }

        try (InputStream input = connection.getInputStream();
             OutputStream output = Files.newOutputStream(Paths.get(downloadFolderPath + File.separator + filename), CREATE_NEW)) {

            System.out.println("length2 " + connection.getContentLength());

            byte[] bytes = new byte[1024];

            long total = 0;
            int count;
            while ((count = input.read(bytes)) != -1) {
                total += count;

//                publish the progress
                this.publishProgress(progressBar, (int) ((total * 100) / connection.getContentLength()));

                output.write(bytes, 0, count);
            }

            handler.postDelayed(() -> {
//                UI Thread work here
                progressBar.setVisibility(View.GONE);
            }, 2000);
        } catch (FileAlreadyExistsException e) {
            String newFilename = this.generateParallelFilename(filename);
            this.writeFile(downloadLink, downloadFolderPath, newFilename, progressBar, handler);
        }
    }

    private void publishProgress(ProgressBar progressBar, Integer... progress) {
        progressBar.setProgress(progress[0]);
    }

    private String generateParallelFilename(String filename) {
        String extension = filename.substring(filename.lastIndexOf("."));
        String filenameWithoutExtension = filename.substring(0, filename.lastIndexOf("."));
        int lastIndexOfLodash = filenameWithoutExtension.lastIndexOf("_");
        String defaultResult = filenameWithoutExtension + "_" + 1 + extension;
        if (lastIndexOfLodash < 0) {
            return defaultResult;
        } else {
            if (lastIndexOfLodash == filename.length() - 1) {
                return filenameWithoutExtension + 1 + extension;
            }
            String suffix = filenameWithoutExtension.substring(lastIndexOfLodash + 1);
            if (StringUtils.isEmpty(suffix)) {
                return defaultResult;
            }
            try {
                int suffixInt = Integer.parseInt(suffix);
                return filenameWithoutExtension.substring(0, lastIndexOfLodash + 1) + (++suffixInt) + extension;
            } catch (Exception e) {
                return defaultResult;
            }
        }
    }

    private void enablePickFile() {
        AppCompatActivity activity = ActivityUtils.getActivity(getContext());
        if (activity == null) {
            return;
        }

        customFilePickerComponentButton.setOnClickListener((view) ->
                permissionDisposal = rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
               .subscribe(granted -> {
                   if (granted) {
                       Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                       intent.setType("*/*");
                       intent.addCategory(Intent.CATEGORY_OPENABLE);
                       pickFileActivityResultLauncher.launch(Intent.createChooser(intent, "Pick a file"));
                   } else {
                       Toast.makeText(activity, "Storage access denied", Toast.LENGTH_SHORT).show();
                   }
               }));
    }

    public void registerPickFileActivityResultLauncher() {
        AppCompatActivity activity = ActivityUtils.getActivity(getContext());
        if (activity == null) {
            return;
        }
        pickFileActivityResultLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::pickFile
        );
    }

    public void registerPickFileActivityResultLauncherOnFragment(Fragment fragment) {
        pickFileActivityResultLauncher = fragment.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::pickFile
        );
    }

    public void flushPickFileActivityResultLauncher() {
        pickFileActivityResultLauncher = null;
    }

    private void pickFile(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Uri uri = result.getData().getData();

            if (this.uploadHandler != null) {
                this.uploadHandler.handle(uri);
            }

            permissionDisposal.dispose();
        }
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

    public void scrollTo(int position) {
        if (-1 < position && position < this.getObjects().size()) {
            customFilePickerComponentMainElement.smoothScrollToPosition(position);
        }
    }

    public void setUploadHandler(UploadHandler uploadHandler) {
        this.uploadHandler = uploadHandler;
    }

    public void setRemoveHandler(RemoveHandler removeHandler) {
        this.removeHandler = removeHandler;
    }

    public void setAbleToPickFile(boolean ableToPickFile) {
        this.ableToPickFile = ableToPickFile;
        if (ableToPickFile) {
            customFilePickerComponentButton.setVisibility(VISIBLE);
            this.enablePickFile();
        } else {
            customFilePickerComponentButton.setVisibility(GONE);
        }
    }

    public boolean isAbleToPickFile() {
        return this.ableToPickFile;
    }

    private void setAttributes(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CustomFilePickerComponent, 0, 0);
        try {
            boolean cancellable = typedArray.getBoolean(R.styleable.CustomFilePickerComponent_deletable, false);
            this.setCancellable(cancellable);

            boolean downloadable = typedArray.getBoolean(R.styleable.CustomFilePickerComponent_downloadable, false);
            this.setDownloadable(downloadable);

            boolean ableToPickFile = typedArray.getBoolean(R.styleable.CustomFilePickerComponent_ableToPickFile, true);
            this.setAbleToPickFile(ableToPickFile);
        } finally {
            typedArray.recycle();
        }
    }

    public interface UploadHandler {
        void handle(Uri uri);
    }

    public interface RemoveHandler {
        void handle(int position, SelectedAttachment selectedAttachment);
    }
}
