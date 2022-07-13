package cn.alphahub.dtt.plus.framework.core;

import java.io.StringWriter;

/**
 * DTT Context
 *
 * @author weasley
 * @version 1.0
 * @date 2022/7/13
 * @since 1.0
 */
public interface DttContext<T> {

    /**
     * Parse fully qualified class name to data table structure model
     *
     * @param fullyQualifiedClassName fully qualified class name of model
     * @return Data table structure model
     */
    default ParsedModel<T> parse(String fullyQualifiedClassName) {
        return null;
    }

    /**
     * build create table statement
     *
     * @param model Data Model Analysis Results
     */
    default void create(ParsedModel<T> model) {
    }

    /**
     * execute table statement
     *
     * @param writer table statement string writer
     */
    default void execute(StringWriter writer) {

    }
}