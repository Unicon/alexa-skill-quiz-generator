package org.unicon.lex.services.external;

import com.amazonaws.services.athena.AmazonAthena;
import com.amazonaws.services.athena.model.ColumnInfo;
import com.amazonaws.services.athena.model.Datum;
import com.amazonaws.services.athena.model.GetQueryExecutionRequest;
import com.amazonaws.services.athena.model.GetQueryExecutionResult;
import com.amazonaws.services.athena.model.GetQueryResultsRequest;
import com.amazonaws.services.athena.model.GetQueryResultsResult;
import com.amazonaws.services.athena.model.QueryExecutionContext;
import com.amazonaws.services.athena.model.QueryExecutionState;
import com.amazonaws.services.athena.model.ResultConfiguration;
import com.amazonaws.services.athena.model.Row;
import com.amazonaws.services.athena.model.StartQueryExecutionRequest;
import com.amazonaws.services.athena.model.StartQueryExecutionResult;
import com.amazonaws.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.unicon.lex.AwsFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class AthenaServiceImpl implements AthenaService {
    final Logger log = LogManager.getLogger(getClass());
    private static final String DATABASE_KEY = "athena.database";
    private static final String OUTPUT_BUCKET_KEY = "athena.output.bucket";
    private static final String SLEEP_AMOUNT_KEY = "athena.sleep.amount.in.millis";

    private Properties properties;
    private AmazonAthena client;

    public AthenaServiceImpl(Properties properties) {
        this.properties = properties;
        AwsFactory awsFactory = new AwsFactory(properties);
        client = awsFactory.getAthenaClient();
    }

    @Override
    public List<String> getLdaAnswer(String subject, String word) {
        String baseQuery = "select related as result from lda";
        String whereClause = String.format("where subject = '%s' and word = '%s'", subject, word);
        String query = baseQuery + " " + whereClause;
        List<String> definitions = execute(query);
        return definitions;
    }

    @Override
    public List<String> getQuizQuestions(String subject, String quizName, String numQuest) {
        log.debug(subject);
        log.debug(quizName);
        String baseQuery = "select quizdata as result from quiz";
        String whereClause = String.format("where subject = '%s' and quizName = '%s'", subject, quizName);
        String query = baseQuery + " " + whereClause;
        List<String> quizResults = execute(query);

        int numQ = Integer.parseInt(numQuest);
        Random rand = new Random();
        List<String> quizzes = new ArrayList<>();
        int max = numQ > quizResults.size() ? quizResults.size() : numQ;
        for (int i = 0; i < max; i++) {
            int randomNumber = rand.nextInt(quizResults.size());
            quizzes.add(quizResults.get(randomNumber));
            quizResults.remove(randomNumber);
        }
        return quizzes;
    }

    @Override
    public String getMediaURL(String subject, String word) {
        String baseQuery = "select url as result from media";
        String whereClause = String.format("where key = '%s' and word = '%s'", subject, word);
        String query = baseQuery + " " + whereClause;
        List<String> mediaURL = execute(query);
        log.debug("mediaURL [{}], size: [{}]", mediaURL, mediaURL.size());
        if (mediaURL.isEmpty()) {
            return null;
        }
        return mediaURL.get(0);
    }

    public String getGlossaryDefinition(String subject, String word, String table) {
        String baseQuery = "select key, word, description as result from " + table;
        String whereClause = String.format("where key = '%s' and word = '%s'", subject, word);
        String query = baseQuery + " " + whereClause;
        List<String> answer = execute(query);
        log.debug("answer [" + answer + "], size: [" + answer.size() + "]");
        if (answer.size() < 1) { // ignore the first row, it's the column headers
            return null;
        }
        String result = String.join(",",  answer);
        return result;
    }

    public String getGlossaryDefinitionMode(String subject, String word) {
        String baseQuery = "select key, word, mode as result from custom_glossary";
        String whereClause = String.format("where key = '%s' and word = '%s'", subject, word);
        String query = baseQuery + " " + whereClause;
        List<String> mode = execute(query);
        log.debug("mode [" + mode + "], size: [" + mode.size() + "]");
        if (!mode.isEmpty()) {
            return mode.get(0);
        }
        //default mode
        return "ADD_FIRST";
    }

    @Override
    public List<String> getGlossaryAnswer(String subject, String word) {
        List<String> definitions = new ArrayList<>();
        String customDefinition = getGlossaryDefinition(subject, word, "custom_glossary");
        String mode = getGlossaryDefinitionMode(subject, word);
        String definition = getGlossaryDefinition(subject, word, "glossary");

        //assume the default mode of adding custom the definitions first
        if (customDefinition != null) {
            definitions.add(customDefinition);
        }
        if (definition != null && !"OVERRIDE".equals(mode)) {
            definitions.add(definition);
        }

        if (definitions.isEmpty()) {
            definitions.add(String.format("No definitions found for [%s] in subject [%s]", word, subject));
        } else if ("ADD_LAST".equals(mode)) {
            Collections.reverse(definitions);
        }
        return definitions;
    }

    @Override
    public List<String> getEnrollments(String userId) {
        String baseQuery = "select subject as result from enrollment";
        String whereClause = String.format("where userid = '%s'", userId);
        String query = baseQuery + " " + whereClause;
        List<String> definitions = execute(query);
        return definitions;
    }

    private List<String> execute(String query)  {
        // execute the passed query
        // return a list of the values in the resultColumnName field
        log.debug("query {}",  query);
        String queryExecutionId = submitAthenaQuery(query);
        List<String> rows = new ArrayList<>();
        try {
            waitForQueryToComplete(queryExecutionId);
            rows = processResultRows(queryExecutionId);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return rows;
    }

    /**
     * Submits a sample query to Athena and returns the execution ID of the query.
     */
    public String submitAthenaQuery(String query) {
        String database = properties.getProperty(DATABASE_KEY);
        // The QueryExecutionContext allows us to set the Database.
        QueryExecutionContext queryExecutionContext = new QueryExecutionContext()
                .withDatabase(database);

        // The result configuration specifies where the results of the query should go in S3 and encryption options
        String outputBucket = properties.getProperty(OUTPUT_BUCKET_KEY);
        ResultConfiguration resultConfiguration = new ResultConfiguration()
                // You can provide encryption options for the output that is written.
                // .withEncryptionConfiguration(encryptionConfiguration)
                .withOutputLocation(outputBucket);

        // Create the StartQueryExecutionRequest to send to Athena which will start the query.
        StartQueryExecutionRequest startQueryExecutionRequest = new StartQueryExecutionRequest()
                .withQueryString(query)
                .withQueryExecutionContext(queryExecutionContext)
                .withResultConfiguration(resultConfiguration);

        StartQueryExecutionResult startQueryExecutionResult = client.startQueryExecution(startQueryExecutionRequest);
        return startQueryExecutionResult.getQueryExecutionId();
    }

    /**
     * Wait for an Athena query to complete, fail or to be cancelled. This is done by polling Athena over an
     * interval of time. If a query fails or is cancelled, then it will throw an exception.
     */
    public void waitForQueryToComplete(String queryExecutionId)
            throws InterruptedException {
        String sleepAmountInMillisStr = properties.getProperty(SLEEP_AMOUNT_KEY);
        int sleepAmountInMillis = Integer.parseInt(sleepAmountInMillisStr);
        GetQueryExecutionRequest getQueryExecutionRequest = new GetQueryExecutionRequest()
                .withQueryExecutionId(queryExecutionId);

        GetQueryExecutionResult getQueryExecutionResult = null;
        boolean isQueryStillRunning = true;
        while (isQueryStillRunning) {
            getQueryExecutionResult = client.getQueryExecution(getQueryExecutionRequest);
            String queryState = getQueryExecutionResult.getQueryExecution().getStatus().getState();
            if (queryState.equals(QueryExecutionState.FAILED.toString())) {
                throw new RuntimeException("Query Failed to run with Error Message: " + getQueryExecutionResult.getQueryExecution().getStatus().getStateChangeReason());
            }
            else if (queryState.equals(QueryExecutionState.CANCELLED.toString())) {
                throw new RuntimeException("Query was cancelled.");
            }
            else if (queryState.equals(QueryExecutionState.SUCCEEDED.toString())) {
                isQueryStillRunning = false;
            }
            else {
                Thread.sleep(sleepAmountInMillis);
            }
            log.error("Current Status is: " + queryState);
        }
    }

    /**
     * This code calls Athena and retrieves the results of a query.
     * The query must be in a completed state before the results can be retrieved and
     * paginated. The first row of results are the column headers.
     */

    public List<String> processResultRows(String queryExecutionId) {
        GetQueryResultsRequest getQueryResultsRequest = new GetQueryResultsRequest()
                // Max Results can be set but if its not set,
                // it will choose the maximum page size
                // As of the writing of this code, the maximum value is 1000
                // .withMaxResults(1000)
                .withQueryExecutionId(queryExecutionId);

        List<String> definitions = new ArrayList<>();
        GetQueryResultsResult getQueryResultsResult = client.getQueryResults(getQueryResultsRequest);
        List<ColumnInfo> columnInfoList = getQueryResultsResult.getResultSet().getResultSetMetadata().getColumnInfo();

        int descriptionColumnIndex = getColumnIndexByName(columnInfoList, "result");

        // if we can't find the correct column for results, don't bother looking ata results
        if (descriptionColumnIndex == -1) {
            log.error("Cannot find the description column, ignoring");
            return definitions;
        }

        while (true) {
            List<Row> results = getQueryResultsResult.getResultSet().getRows();
            log.debug("query returned [{}] rows", results.size());
            // ignore the first row, since that is the column header row
            for (int i = 1; i < results.size(); i++) {
                Row row = results.get(i);
                // Process the row. The first row of the first page holds the column names.
                Datum data = row.getData().get(descriptionColumnIndex); // todo Shouldn't be hard coded
                log.debug("data [{}]",  data.getVarCharValue());
                if (!StringUtils.isNullOrEmpty(data.getVarCharValue())) {
                    definitions.add(data.getVarCharValue());
                }
            }
            // If nextToken is null, there are no more pages to read. Break out of the loop.
            if (getQueryResultsResult.getNextToken() == null) {
                break;
            }
            getQueryResultsResult = client.getQueryResults(
                    getQueryResultsRequest.withNextToken(getQueryResultsResult.getNextToken()));
        }
        return definitions;
    }

    private int getColumnIndexByName(List<ColumnInfo> columnInfoList, String columnName) {
        for (int i = 0; i < columnInfoList.size(); ++i) {
            if (columnInfoList.get(i).getName().equals(columnName)) {
                return i;
            }
        }
        return -1;
    }
}
