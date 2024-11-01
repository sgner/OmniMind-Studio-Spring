package com.ai.chat.a.utils;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.xml.sax.ContentHandler;
@RequiredArgsConstructor
public class AAIDocumentReader {
    public static final String METADATA_SOURCE = "source";
    private final AutoDetectParser parser;
    private final ContentHandler handler;
    private final Metadata metadata;
    private final ParseContext context;
    private final Resource resource;
    private final ExtractedTextFormatter textFormatter;

    public AAIDocumentReader(String resourceUrl) {
        this(resourceUrl, ExtractedTextFormatter.defaults());
    }

    public AAIDocumentReader(String resourceUrl, ExtractedTextFormatter textFormatter) {
        this((new DefaultResourceLoader()).getResource(resourceUrl), textFormatter);
    }

    public AAIDocumentReader(Resource resource) {
        this(resource, ExtractedTextFormatter.defaults());
    }

    public AAIDocumentReader(Resource resource, ExtractedTextFormatter textFormatter) {
        this(resource, new BodyContentHandler(-1), textFormatter);
    }

    public AAIDocumentReader(Resource resource, ContentHandler contentHandler, ExtractedTextFormatter textFormatter) {
        this.parser = new AutoDetectParser();
        this.handler = contentHandler;
        this.metadata = new Metadata();
        this.context = new ParseContext();
        this.resource = resource;
        this.textFormatter = textFormatter;
    }

    public List<Document> get() {
        try {
            InputStream stream = this.resource.getInputStream();

            List var2;
            try {
                this.parser.parse(stream, this.handler, this.metadata, this.context);
                var2 = List.of(this.toDocument(this.handler.toString()));
            } catch (Throwable var5) {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (Throwable var4) {
                        var5.addSuppressed(var4);
                    }
                }

                throw var5;
            }

            if (stream != null) {
                stream.close();
            }

            return var2;
        } catch (Exception var6) {
            throw new RuntimeException(var6);
        }
    }

    private Document toDocument(String docText) {
        docText = (String)Objects.requireNonNullElse(docText, "");
        docText = this.textFormatter.format(docText);
        Document doc = new Document(docText);
        doc.getMetadata().put("source", this.resourceName());
        return doc;
    }

    private String resourceName() {
        try {
            String resourceName = this.resource.getFilename();
            if (!StringUtils.hasText(resourceName)) {
                resourceName = this.resource.getURI().toString();
            }

            return resourceName;
        } catch (IOException var2) {
            return String.format("Invalid source URI: %s", var2.getMessage());
        }
    }

}
