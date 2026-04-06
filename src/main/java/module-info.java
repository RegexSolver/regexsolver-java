module com.regexsolver.api {
    exports com.regexsolver.api;
    exports com.regexsolver.api.exceptions;

    requires java.net.http;
    requires java.logging;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.openapitools.jackson.nullable;

    requires static jakarta.annotation;
}
