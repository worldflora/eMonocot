package org.emonocot.portal.integration;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.emonocot.api.job.JobExecutionInfo;
import org.emonocot.api.job.JobLaunchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.integration.Message;
import org.springframework.integration.message.GenericMessage;

/**
 *
 * @author ben
 *
 */
public class JobLaunchResponseTransformer {

    /**
    *
    */
    private static Logger logger = LoggerFactory
                .getLogger(JobLaunchResponseTransformer.class);

    /**
     *
     */
    private ObjectMapper objectMapper;

    /**
     *
     * @param objectMapper Set the object mapper
     */
    public final void setObjectMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     *
     * @param message the http response
     * @return an the message
     * @throws JobExecutionException if the response is not expected
     */
    public final Message transform(final Message message)
            throws JobExecutionException {
        logger.debug("In transform (" + message + ")");
        if (message.getPayload().getClass().equals(String.class)) {
            logger.debug("Message payload is " + message.getPayload());
            try {
                JobLaunchResponse response = objectMapper.readValue(
                        (String) message.getPayload(), JobLaunchResponse.class);
                return new GenericMessage<JobExecutionInfo>(response.getJobExecution(),
                        message.getHeaders());
            } catch (IllegalArgumentException e) {
                try {
                    JobExecutionException jobExecutionException = objectMapper
                            .readValue((String) message.getPayload(),
                                    JobExecutionException.class);
                    throw jobExecutionException;
                } catch (IllegalArgumentException e1) {
                    throw new JobExecutionException("Could not parse response",
                            e1);
                } catch (JsonParseException jpe) {
                    throw new JobExecutionException("Could not parse response",
                            jpe);
                } catch (JsonMappingException jme) {
                    throw new JobExecutionException("Could not parse response",
                            jme);
                } catch (IOException ioe) {
                    throw new JobExecutionException("Could not parse response",
                            ioe);
                }
            } catch (Exception e) {
                throw new JobExecutionException("Could not parse response", e);
            }
        } else {
            logger.debug("Message is not String.class : " + message.getPayload());
            HttpStatus httpStatus = (HttpStatus) message.getPayload();
            throw new JobExecutionException(httpStatus.value() + " "
                    + httpStatus.getReasonPhrase());
        }
    }

}