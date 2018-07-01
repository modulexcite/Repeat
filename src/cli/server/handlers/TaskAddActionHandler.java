package cli.server.handlers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;

import argo.jdom.JsonNode;
import cli.messages.TaskAddMessage;
import cli.server.CliRpcCodec;
import core.userDefinedTask.TaskGroup;
import utilities.FileUtility;

public class TaskAddActionHandler extends TaskActionHandler {

	private static final Logger LOGGER = Logger.getLogger(TaskAddActionHandler.class.getName());

	@Override
	protected Void handleTaskActionWithBackend(HttpExchange exchange, JsonNode request) throws IOException {
		TaskAddMessage message = TaskAddMessage.parseJSON(request);
		if (message.getTaskIdentifier() == null) {
			return CliRpcCodec.prepareResponse(exchange, 400, "Missing task identifier.");
		}

		if (message.getTaskIdentifier().getTask().getName().isEmpty()) {
			return CliRpcCodec.prepareResponse(exchange, 400, "Empty task name.");
		}

		if (message.getFilePath().isEmpty()) {
			return CliRpcCodec.prepareResponse(exchange, 400, "No source file.");
		}

		TaskGroup group = getGroup(message.getTaskIdentifier());
		if (group == null) {
			return CliRpcCodec.prepareResponse(exchange, 400, "Unable to identify task group.");
		}


		Path path = Paths.get(message.getFilePath());
		if (!Files.isRegularFile(path)) {
			return CliRpcCodec.prepareResponse(exchange, 400, "Path " + path + " is not a file.");
		}

		String source = FileUtility.readFromFile(path.toFile()).toString();
		if (source == null) {
			return CliRpcCodec.prepareResponse(exchange, 500, "Unable to read source file.");
		}

		if (!backEndHolder.compileSource(source)) {
			return CliRpcCodec.prepareResponse(exchange, 500, "Unable to compile source file.");
		}

		backEndHolder.addCurrentTask(group);
		LOGGER.info("Added new task from file " + path.toAbsolutePath());
		return CliRpcCodec.prepareResponse(exchange, 200, "Successfully added new task.");
	}
}
