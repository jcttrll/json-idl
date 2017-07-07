package net.jonathancottrill.experimental;

import java.util.Map;

public interface Generator {
	String generate(String typeName, Map<String, Object> typeDefinition);
}
