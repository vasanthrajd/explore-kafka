package com.explore.kafka.explore_kafka.producer.configuration;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AvroSchemaLoader {
    public static Schema loadSchema(String schemaPath) {
        try (InputStream is =
                     AvroSchemaLoader.class
                             .getClassLoader()
                             .getResourceAsStream(schemaPath)) {

            if (is == null) {
                throw new RuntimeException("Schema file not found");
            }

            return new Schema.Parser().parse(is);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load schema", e);
        }
    }

    public static byte[] serializeAvro(GenericRecord record, Schema schema)
            throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryEncoder encoder =
                EncoderFactory.get().binaryEncoder(out, null);

        DatumWriter<GenericRecord> writer =
                new GenericDatumWriter<>(schema);

        writer.write(record, encoder);
        encoder.flush();

        return out.toByteArray();
    }
}
