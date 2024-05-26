package co.za.clementine.mastersapp.profile.apps.install

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import co.za.clementine.mastersapp.BuildConfig
import co.za.clementine.mastersapp.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    private val dismissDownloadPopUp = false;
    fun downloadAndInstall(apkUrl: String) {
        downloadProgressManager.showProgressPopup(context)
        CoroutineScope(Dispatchers.IO).launch {
            val apkFile = downloadApk(apkUrl)
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
                val fileName = "downloaded_apk.apk"
                apkFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

                val output = FileOutputStream(apkFile)
                val data = ByteArray(1024)
                var count: Int
                var totalBytes: Long = 0
                val fileLength = connection.contentLength

                while (input.read(data).also { count = it } != -1) {
                    totalBytes += count
                    output.write(data, 0, count)
//                    Log.d("ApkInstaller", "Downloaded $totalBytes / $fileLength bytes")
                    showProgressNotification(totalBytes, fileLength)
                    downloadProgressManager.updateProgress(totalBytes, fileLength.toLong())
//                    (context as MainActivity).updateProgressBar(totalBytes.toInt(), fileLength)
                }
                showProgressNotification(totalBytes, fileLength)

                output.flush()
                output.close()
                input.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return apkFile
    }

    private fun installApk(apkFile: File) {
        val apkUri: Uri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", apkFile)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
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

//    private fun showProgressNotification(progress: Long, max: Int) {
//        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
//            .setSmallIcon(android.R.drawable.stat_sys_download)
//            .setContentTitle("Downloading APK")
//            .setContentText("Download in progress")
//            .setPriority(NotificationCompat.PRIORITY_LOW)
//            .setProgress(max, progress.toInt(), false)
//        with(NotificationManagerCompat.from(context)) {
//            if (ActivityCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.POST_NOTIFICATIONS
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return
//            }
//            notify(NOTIFICATION_ID, builder.build())
//        }
//    }
    private fun showProgressNotification(progress: Long, max: Int) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        println(max.toString() + " == " + progress + " ? " + (max.toLong() == progress))
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

