package net.jonathancottrill.experimental;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class JsonIdl {
	public static void main(String[] args) throws IOException {
		JsonIdl jsonIdl = new JsonIdl();

		Files.list(Paths.get("."))
			.filter(file ->
				file.getFileName().toString().endsWith(".json") &&
					Files.isRegularFile(file))
			.forEach(file -> {
				Map<String, Object> typeDefinition = jsonIdl.loadTypeDefinition(file);

				String generated = new JavaGenerator().generate("Blah", typeDefinition);
				String generatedFilename = file.getFileName().toString().replaceAll("\\.json$", ".java");

				try (
					BufferedWriter writer = Files.newBufferedWriter(Paths.get(generatedFilename))
				) {
					writer.write(generated);
				} catch (Exception e) {
					throw new RuntimeException("Failed to generate " + generatedFilename, e);
				}
			});
	}

	 private Map<String, Object> loadTypeDefinition(Path path) {
		try (
			BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)
		) {
			JSONParser parser = new JSONParser();
			Object parsed = parser.parse(reader);

			if (!(parsed instanceof Map)) {
				throw new RuntimeException("JSON file " + path + " does not describe a JSON object");
			}

			@SuppressWarnings("unchecked")
			Map<String, Object> typeDefinition = (Map<String, Object>) parsed;

			return typeDefinition;
		} catch (IOException | ParseException e) {
			throw new RuntimeException("Failed to parse " + path + " as JSON object", e);
		}
	 }
}
