package io.edurt.datacap.executor.seatunnel;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.edurt.datacap.executor.ExecutorService;
import io.edurt.datacap.executor.common.RunState;
import io.edurt.datacap.executor.configure.ExecutorConfigure;
import io.edurt.datacap.executor.configure.ExecutorRequest;
import io.edurt.datacap.executor.configure.ExecutorResponse;
import io.edurt.datacap.executor.seatunnel.connector.Connector;
import io.edurt.datacap.executor.seatunnel.connector.ConnectorFactory;
import io.edurt.datacap.executor.seatunnel.connector.ConnectorType;
import io.edurt.datacap.lib.logger.LoggerExecutor;
import io.edurt.datacap.lib.logger.logback.LogbackExecutor;
import io.edurt.datacap.lib.shell.ShellCommander;
import io.edurt.datacap.lib.shell.ShellConfigure;
import io.edurt.datacap.lib.shell.ShellResponse;
import io.edurt.datacap.lib.shell.process.ProcessBuilderCommander;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

@SuppressFBWarnings(value = {"RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"})
@Slf4j
public class SeatunnelExecutorService
        implements ExecutorService
{
    @Override
    public ExecutorResponse start(ExecutorRequest request)
    {
        try {
            SeaTunnelCommander commander = new SeaTunnelCommander(
                    request.getExecutorHome() + "/bin",
                    request.getStartScript(),
                    request.getRunWay().name().toLowerCase(),
                    request.getRunMode().name().toLowerCase(),
                    String.join(File.separator, request.getWorkHome(), request.getTaskName() + ".configure"),
                    request.getTaskName(),
                    request.getRunEngine());

            LoggerExecutor loggerExecutor = new LogbackExecutor(request.getWorkHome(), request.getTaskName() + ".log");
            String result = before(request, loggerExecutor.getLogger());
            if (StringUtils.isNotEmpty(result)) {
                return new ExecutorResponse(false, false, RunState.FAILURE, result);
            }

            ShellConfigure shellConfigure = ShellConfigure.builder()
                    .directory(request.getWorkHome())
                    .loggerExecutor(loggerExecutor)
                    .command(Collections.singletonList(commander.toCommand()))
                    .timeout(request.getTimeout())
                    .username(request.getUserName())
                    .build();
            ShellCommander shellExecutor = new ProcessBuilderCommander(shellConfigure);
            ShellResponse response = shellExecutor.execute();
            log.info("Task [ {} ] executed code [ {} ]", request.getTaskName(), response.getCode());
            RunState state = response.getSuccessful() ? RunState.SUCCESS : RunState.FAILURE;
            log.info("Task [ {} ] executed state [ {} ]", request.getTaskName(), state);

            String message = null;
            if (response.getErrors() != null && !response.getSuccessful()) {
                message = String.join("\n", response.getErrors());
            }
            return new ExecutorResponse(response.isTimeout(), response.getSuccessful(), state, message);
        }
        catch (Exception exception) {
            return new ExecutorResponse(false, false, RunState.FAILURE, exception.getMessage());
        }
    }

    @Override
    public ExecutorResponse stop(ExecutorRequest request)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Writes a child element to the JSON output using the specified type, JSON generator, and executor configure.
     *
     * @param type the type of the child element
     * @param jsonGenerator the JSON generator to write the child element to
     * @param configure the executor configure to use for formatting the child element
     * @throws Exception if there is an error writing the child element
     */
    private void writeChild(String type, JsonGenerator jsonGenerator, ExecutorConfigure configure)
            throws Exception
    {
        jsonGenerator.writeFieldName(type);
        if (ObjectUtils.isNotEmpty(configure)) {
            String protocol = configure.getType();

            if (protocol == null || protocol.trim().isEmpty()) {
                protocol = "Jdbc";
            }
            else {
                try {
                    ConnectorType.valueOf(protocol);
                }
                catch (IllegalArgumentException e) {
                    protocol = configure.getType();
                }
            }

            Connector factory = ConnectorFactory.createFormatter(protocol, configure);
            for (Map.Entry<String, Object> entry : factory.formatToMap().entrySet()) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeObjectFieldStart(entry.getKey());
                if (entry.getValue() instanceof Properties) {
                    Properties props = (Properties) entry.getValue();
                    for (Map.Entry<Object, Object> property : props.entrySet()) {
                        String key = property.getKey().toString();
                        Object value = property.getValue();

                        if (value == null) {
                            jsonGenerator.writeNullField(key);
                        }
                        else if (value instanceof Boolean) {
                            jsonGenerator.writeBooleanField(key, (Boolean) value);
                        }
                        else if (value instanceof Number) {
                            if (value instanceof Integer) {
                                jsonGenerator.writeNumberField(key, (Integer) value);
                            }
                            else if (value instanceof Long) {
                                jsonGenerator.writeNumberField(key, (Long) value);
                            }
                            else if (value instanceof Double) {
                                jsonGenerator.writeNumberField(key, (Double) value);
                            }
                            else {
                                jsonGenerator.writeStringField(key, value.toString());
                            }
                        }
                        else {
                            String strValue = value.toString();
                            String[] split = strValue.split("\n");
                            if (split.length > 1 && !key.equalsIgnoreCase("sql")) {
                                jsonGenerator.writeArrayFieldStart(key);
                                for (String line : split) {
                                    jsonGenerator.writeString(line);
                                }
                                jsonGenerator.writeEndArray();
                            }
                            else {
                                jsonGenerator.writeStringField(key, strValue);
                            }
                        }
                    }
                }
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndObject();
        }
        else {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeEndObject();
        }
    }

    /**
     * Generates a configure file in JSON format based on the given ExecutorRequest.
     *
     * @param request the ExecutorRequest object containing the necessary information for generating the configure file
     * @param logger the Logger object for logging any errors that occur during the process
     * @return null if the configure file was generated successfully, or an error message if an exception occurred
     */
    private String before(ExecutorRequest request, Logger logger)
    {
        JsonFactory jsonFactory = new JsonFactory();
        String workFile = String.join(File.separator, request.getWorkHome(), request.getTaskName() + ".configure");
        try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(new File(workFile), JsonEncoding.UTF8)) {
            jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
            jsonGenerator.writeStartObject();
            this.writeChild("env", jsonGenerator, null);
            this.writeChild("source", jsonGenerator, request.getInput());
            if (request.getTransform() != null) {
                this.writeChild("transform", jsonGenerator, request.getTransform());
            }
            this.writeChild("sink", jsonGenerator, request.getOutput());
            jsonGenerator.writeEndObject();
            return null;
        }
        catch (Exception exception) {
            logger.error("Build configure file failed", exception);
            return exception.getMessage();
        }
    }
}
