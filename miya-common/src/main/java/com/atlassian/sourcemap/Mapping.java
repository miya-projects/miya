package com.atlassian.sourcemap;

/**
 * Mapping of position from generated file to source file.
 */
public interface Mapping {
    int getGeneratedLine();

    int getGeneratedColumn();

    int getSourceLine();

    int getSourceColumn();

    String getSourceFileName();

    String getSourceSymbolName();
}
