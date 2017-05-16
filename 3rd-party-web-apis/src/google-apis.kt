package vgrechka

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.util.*

// https://console.cloud.google.com/apis/dashboard
// https://developers.google.com/apis-explorer
// https://github.com/google/google-api-java-client-samples/blob/master/drive-cmdline-sample/src/main/java/com/google/api/services/samples/drive/cmdline/DriveSample.java

class GoogleDrive(val dataStoreDirPath: String? = null) {
    val applicationName = "pepezdus"
    val readTimeoutSeconds = 1000 * 30

    val httpTransport = GoogleNetHttpTransport.newTrustedTransport()!!
    val jsonFactory = JacksonFactory.getDefaultInstance()!!
    val credential = authorize()
    val drive = Drive.Builder(httpTransport, jsonFactory, credential)
        .setHttpRequestInitializer {
            credential.initialize(it)
            it.readTimeout = readTimeoutSeconds
        }
        .setApplicationName(applicationName)
        .build()!!

    fun authorize(): Credential {
        val dataStoreDir = FilePile.ensuringDirectoryExists(dataStoreDirPath ?: "c:/tmp/GooglePile-data-store")

        val clientSecrets = GoogleClientSecrets()
        clientSecrets.installed = BigPile.saucerfulOfSecrets.gdrive.pepezdus.installed

        val dataStoreFactory = FileDataStoreFactory(dataStoreDir)

        val flow = GoogleAuthorizationCodeFlow.Builder(
            httpTransport, jsonFactory, clientSecrets,
            Collections.singleton(DriveScopes.DRIVE)).setDataStoreFactory(dataStoreFactory)
            .build()

        return AuthorizationCodeInstalledApp(flow, LocalServerReceiver()).authorize("fucker")
    }

}





