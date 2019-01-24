package goodr0ne;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

public class GapeFixer {
  private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final String TOKENS_DIRECTORY_PATH = "tokens";
  private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
  private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

  private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
    InputStream in = GapeFixer.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build();
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
  }

  public static void main(String[] args) {
    System.out.println("Hello World!");

    try {
      final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
      Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
              .setApplicationName(APPLICATION_NAME)
              .build();
      Drive.Children.List firstRequest = service.children().list("")
              .setQ("mimeType='application/pdf' and modifiedDate <= '2016-03-07T19:17:54.759Z'");
      do {
        ChildList result = firstRequest.execute();
        for (ChildReference child : result.getItems()) {
          File file = service.files().get(child.getId()).execute();
          System.out.printf("Found file: %s (%s): %s\n", file.getTitle(), file.getId(), file.getCreatedDate());
        }
        firstRequest.setPageToken(result.getNextPageToken());
      } while (firstRequest.getPageToken() != null && firstRequest.getPageToken().length() > 0);
    } catch (Exception e) {
      System.out.println("Don't worry, shit happens!");
    }

    System.out.println("Hi again!");
  }
}
