package com.atlassian.sourcemap;

import java.util.List;

/**
 * Reads, writes and combines source maps.
 */
public interface SourceMap {
    /**
     * Add mapping.
     */
    void addMapping(int generatedLine, int generatedColumn, int sourceLine, int sourceColumn, String sourceFileName);

    /**
     * Add mapping.
     */
    void addMapping(int generatedLine, int generatedColumn, int sourceLine, int sourceColumn, String sourceFileName, String sourceSymbolName);

    /**
     * Add mapping.
     */
    void addMapping(Mapping mapping);

    /**
     * Get mapping for line and column in generated file.
     */
    Mapping getMapping(int lineNumber, int column);

    /**
     * Generate source map JSON.
     */
    String generate();

    /**
     * Generate source map in format easily read by humans, for debug purposes.
     */
    String generateForHumans();

    /**
     * Get list of source file names.
     */
    List<String> getSourceFileNames();

    interface EachMappingCallback {
        void apply(Mapping mapping);
    }

    /**
     * Iterate over mappings.
     */
    void eachMapping(EachMappingCallback callback);
}
