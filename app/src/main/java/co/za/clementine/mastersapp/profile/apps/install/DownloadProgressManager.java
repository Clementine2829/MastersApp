package co.za.clementine.mastersapp.profile.apps.install;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import co.za.clementine.mastersapp.R;

public class DownloadProgressManager {
    private AlertDialog dialog;
    private ProgressBar progressBar;
    private TextView progressText;
    private TextView progressSize;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public void showProgressPopup(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.progress_popup, null);

        progressBar = view.findViewById(R.id.progress_bar);
        progressText = view.findViewById(R.id.progress_text);
        progressSize = view.findViewById(R.id.progress_size);

        builder.setView(view);
        dialog = builder.create();
        dialog.show();
    }

    public void updateProgress(long downloadedBytes, long totalBytes) {
        if (dialog != null && dialog.isShowing()) {
            int progress = (int) ((downloadedBytes * 100) / totalBytes);
            String progressPercentage = progress + "%";
            String downloadedSize = formatSize(downloadedBytes);
            String totalSize = formatSize(totalBytes);

            handler.post(() -> {
                progressBar.setProgress(progress);
                progressText.setText(progressPercentage);
                progressSize.setText(String.format("%s / %s", downloadedSize, totalSize));
            });
        }
        if(downloadedBytes == totalBytes) dismissProgressPopup();
    }

    public void dismissProgressPopup() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private String formatSize(long bytes) {
        double size = bytes;
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", size, units[unitIndex]);
    }
}
