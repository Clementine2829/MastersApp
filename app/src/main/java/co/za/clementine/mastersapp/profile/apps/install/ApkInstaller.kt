package co.za.clementine.mastersapp.profile.apps.install

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
import co.za.clementine.mastersapp.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class ApkInstaller(private val context: Context) {
    companion object {
        private const val CHANNEL_ID = "APK_DOWNLOAD"
        private const val NOTIFICATION_ID = 1
    }

    init {
        createNotificationChannel()
    }

    private val downloadProgressManager = DownloadProgressManager()
    private val fileName = "AirDroid App.apk"
    fun fileExistsInDownloadDirectory(fileName: String): Boolean {
        val downloadDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadDir, fileName)
        return file.exists()
    }

    suspend fun downloadAndInstall(apkUrl: String, packageManager: PackageManager) {
        delay(2000)
        if (!isPackageInstalled(packageManager) && !fileExistsInDownloadDirectory(fileName))
            downloadProgressManager.showProgressPopup(context)
        CoroutineScope(Dispatchers.IO).launch {
            val apkFile =
                if (!isPackageInstalled(packageManager) && fileExistsInDownloadDirectory(fileName))
                    getAirDroidInDownloads()
                else {
                    downloadApk(apkUrl)
                }
            apkFile?.let {
                installApk(it)
            }
        }
    }

    private suspend fun downloadApk(apkUrl: String): File? {
        var apkFile: File? = null
        withContext(Dispatchers.IO) {
            try {
                val url = URL(apkUrl)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.connect()
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    return@withContext null
                }

                val input: InputStream = BufferedInputStream(connection.inputStream)
                apkFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    fileName
                )

                val output = FileOutputStream(apkFile)
                val data = ByteArray(1024)
                var count: Int
                var totalBytes: Long = 0
                val fileLength = connection.contentLength

                while (input.read(data).also { count = it } != -1) {
                    totalBytes += count
                    output.write(data, 0, count)
                    showProgressNotification(context, totalBytes, fileLength)
                    downloadProgressManager.updateProgress(totalBytes, fileLength.toLong())
                }
                showProgressNotification(context, totalBytes, fileLength)
                output.flush()
                output.close()
                input.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return apkFile
    }

    fun getAirDroidInDownloads(): File {
        val downloadDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return File(downloadDir, fileName)
    }

    fun installApk(apkFile: File) {
        val apkUri: Uri =
            FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", apkFile)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    private fun isPackageInstalled(packageManager: PackageManager): Boolean {
        val packageName = "com.sand.airdroid";
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true // Package is installed
        } catch (e: PackageManager.NameNotFoundException) {
            false // Package is not installed
        }
    }

    private fun createNotificationChannel() {
        val name = "APK Download"
        val descriptionText = "Notification channel for APK download progress"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun showProgressNotification(context: Context, progress: Long, max: Int) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_LOW)
        if (max.toLong() == progress) {
            builder.setContentTitle("Download Complete")
                .setContentText("AirDroid downloaded successfully")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setProgress(0, 0, false) // Remove progress indicator
        } else {
            builder.setContentTitle("Downloading AirDroid")
                .setContentText("Download in progress")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setProgress(max, progress.toInt(), false)
        }

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Request the missing permissions
                return
            }
            notify(NOTIFICATION_ID, builder.build())
        }
    }
}

