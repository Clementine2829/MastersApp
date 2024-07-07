package co.za.clementine.mastersapp.profile.apps.install;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import co.za.clementine.mastersapp.R;

public class DownloadProgressManager {
    private AlertDialog dialog;
    private ProgressBar progressBar;
    private TextView progressText, progressSize, tvTimeRemaining;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public void showProgressPopup(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.progress_popup, null);

        progressBar = view.findViewById(R.id.progress_bar);
        progressText = view.findViewById(R.id.progress_text);
        progressSize = view.findViewById(R.id.progress_size);
        tvTimeRemaining = view.findViewById(R.id.time_remaining);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    public void updateProgress(long downloadedBytes, long totalBytes) {
        if (dialog != null && dialog.isShowing()) {
            int progress = (int) ((downloadedBytes * 100) / totalBytes);
            String progressPercentage = progress + "%";
            String downloadedSize = formatSize(downloadedBytes);
            String totalSize = formatSize(totalBytes);

            long startTime = 0;
            double elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0;
            double speed = downloadedBytes / elapsedTime;
            double timeRemaining = (speed > 0) ? (double) (totalBytes - downloadedBytes) / speed : 0.0; // seconds

            handler.post(() -> {
                progressBar.setProgress(progress);
                progressText.setText(progressPercentage);
                progressSize.setText(String.format("%s / %s", downloadedSize, totalSize));
                if (downloadedSize.equals(totalSize)) dialog.dismiss();
                tvTimeRemaining.setText(String.format("Estimated remaining time: %s", formatTimeRemaining(timeRemaining)));
            });
        }
        if (downloadedBytes == totalBytes) dismissProgressPopup();
    }

    private String formatTimeRemaining(double seconds) {
        long hours = TimeUnit.SECONDS.toHours((long) seconds);
        long minutes = TimeUnit.SECONDS.toMinutes((long) seconds) % TimeUnit.HOURS.toMinutes(1);
        long secs = TimeUnit.SECONDS.toSeconds((long) seconds) % TimeUnit.MINUTES.toSeconds(1);

        if (hours > 0) {
            return String.format("%d hours %d minutes %d seconds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%d minutes %d seconds", minutes, secs);
        } else {
            return String.format("%d seconds", secs);
        }
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
