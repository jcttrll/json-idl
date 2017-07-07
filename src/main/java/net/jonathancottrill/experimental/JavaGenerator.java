package net.jonathancottrill.experimental;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JavaGenerator implements Generator {
	@Override
	public String generate(String typeName, Map<String, Object> typeDefinition) {
		StringBuilder builder = new StringBuilder(4096);

		List<FieldDefinition> fields = new ArrayList<>(100);
		List<String> imports = new ArrayList<>(20);
		imports.add(javax.annotation.Generated.class.getName());

		typeDefinition.forEach((fieldName, definition) -> {
			if (!(definition instanceof Map)) {
				throw new RuntimeException("Faulty definition for field " + fieldName + " in type " + typeName);
			}

			@SuppressWarnings("unchecked")
			Map<String, Object> fieldDefinition = (Map<String, Object>) definition;

			Object type = fieldDefinition.get("type");

			if (!(type instanceof String)) {
				throw new RuntimeException("Expected a String, got " + type.getClass() +
					" while determining type of field " + fieldName + " in type " + typeName);
			}

			String typeString = (String) type;
			String javaType;

			switch (typeString) {
				case "string":
					javaType = "String";
					break;
				case "int32":
					javaType = "int";
					break;
				case "int64":
					javaType = "long";
					break;
				case "float32":
					javaType = "float";
					break;
				case "float64":
					javaType = "double";
					break;
				case "real":
					javaType = "BigDecimal";
					imports.add(java.math.BigDecimal.class.getName());
					break;
				default:
					javaType = typeString;
			}

			fields.add(new FieldDefinition(fieldName, javaType));
		});

		for (String import_: imports) {
			builder.append("import ");
			builder.append(import_);
			builder.append(";\n");
		}

		builder.append('\n');
		builder.append("@Generated(\"By silly little code generator\")\n");
		builder.append("public class ");
		builder.append(typeName);
		builder.append(" {\n");

		for (FieldDefinition field: fields) {
			builder.append("\tprivate ");
			builder.append(field.type);
			builder.append(' ');
			builder.append(field.name);
			builder.append(";\n");
		}

		for (FieldDefinition field: fields) {
			builder.append("\n\tpublic ");
			builder.append(field.type);
			builder.append(" get");
			builder.append(field.name.substring(0, 1).toUpperCase());
			builder.append(field.name.substring(1));
			builder.append("() {\n\t\treturn ");
			builder.append(field.name);
			builder.append(";\n\t}\n");

			builder.append("\n\tpublic void set");
			builder.append(field.name.substring(0, 1).toUpperCase());
			builder.append(field.name.substring(1));
			builder.append("(");
			builder.append(field.type);
			builder.append(' ');
			builder.append(field.name);
			builder.append(") {\n\t\tthis.");
			builder.append(field.name);
			builder.append(" = ");
			builder.append(field.name);
			builder.append(";\n\t}\n");		}

		builder.append("}\n");

		return builder.toString();
	}

	private static class FieldDefinition {
		final String name;
		final String type;

		private FieldDefinition(String name, String type) {
			this.name = name;
			this.type = type;
		}
	}
}
