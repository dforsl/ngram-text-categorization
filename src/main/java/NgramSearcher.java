import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.BigqueryScopes;
import com.google.api.services.bigquery.model.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Created by daniel on 2014-12-08.
 */
public class NgramSearcher {
    private static NgramSearcher instance;

    // Enter your Google Developer Project number or string id.
    private static final String PROJECT_ID = "studsstatistik";

    // Use a Google APIs Client standard client_secrets.json OAuth 2.0 parameters file.
    private static final String CLIENTSECRETS_LOCATION = "client_secrets.json";

    // Objects for handling HTTP transport and JSON formatting of API calls.
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();

    private Credential credential;
    private Bigquery bigQuery;

    public static NgramSearcher getInstance() {
        if(instance == null) {
            try {
                instance = new NgramSearcher();
            } catch (IOException e) {
                System.out.println("ERROR; couldn't instantiate NgramSearcher.");
                e.printStackTrace();

                return null;
            }
        }

        return instance;
    }

    private NgramSearcher() throws IOException {
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(new JacksonFactory(),
                new InputStreamReader(NgramSearcher.class.getResourceAsStream(CLIENTSECRETS_LOCATION)));

        credential = getCredentials(clientSecrets, new Scanner(System.in));
        bigQuery = new Bigquery(HTTP_TRANSPORT, JSON_FACTORY, credential);
    }

    private static Credential getCredentials(GoogleClientSecrets clientSecrets, Scanner scanner)
            throws IOException {
        String authorizeUrl = new GoogleAuthorizationCodeRequestUrl(
                clientSecrets, clientSecrets.getInstalled().getRedirectUris().get(0),
                Collections.singleton(BigqueryScopes.BIGQUERY)).build();
        System.out.println(
                "Paste this URL into a web browser to authorize BigQuery Access:\n" + authorizeUrl);
        System.out.println("... and paste the code you received here: ");
        String authorizationCode = scanner.nextLine();

        // Exchange the auth code for an access token.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, Arrays.asList(BigqueryScopes.BIGQUERY))
                .build();
        GoogleTokenResponse response = flow.newTokenRequest(authorizationCode)
                .setRedirectUri(clientSecrets.getInstalled().getRedirectUris().get(0)).execute();
        return flow.createAndStoreCredential(response, null);
    }

    public void search(Ngram ngram) throws IOException {
        String query = buildQuery(ngram);

        QueryRequest queryRequest = new QueryRequest().setQuery(query);
        QueryResponse queryResponse = bigQuery.jobs().query(PROJECT_ID, queryRequest).execute();
        if (queryResponse.getJobComplete()) {
            addData(ngram, queryResponse.getRows());
            if (null == queryResponse.getPageToken()) {
                System.out.println("page token is null");
                return;
            }
        }

        // This loop polls until results are present, then loops over result pages.
        String pageToken = null;
        while (true) {
            GetQueryResultsResponse queryResults = bigQuery.jobs()
                    .getQueryResults(PROJECT_ID, queryResponse.getJobReference().getJobId())
                    .setPageToken(pageToken).execute();
            if (queryResults.getJobComplete()) {
                addData(ngram, queryResults.getRows());
                pageToken = queryResults.getPageToken();
                if (null == pageToken) {
                    return;
                }
            }
        }
    }

    private String buildQuery(Ngram ngram) {
        StringBuilder sb = new StringBuilder("SELECT * FROM [publicdata.samples.trigrams] WHERE first = \"");
        sb.append(ngram.words.get(0));
        sb.append("\" AND second = \"");
        sb.append(ngram.words.get(1));
        sb.append("\" AND third = \"");
        sb.append(ngram.words.get(2));
        sb.append("\" LIMIT 1000;");

        return sb.toString();
    }

    private void addData(Ngram ngramm, List<TableRow> rows) {
        if(rows == null) {
            System.out.println("null rows");
            return;
        }

        for(TableRow row : rows) {
            for(TableCell cell : row.getF()) {
                /*if(Data.isNull(cell)) {
                    return;
                }*/

                //TODO insert data
                System.out.println(cell);
            }
        }
    }
}
