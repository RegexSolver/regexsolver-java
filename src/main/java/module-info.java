module com.regexsolver.api {
    exports com.regexsolver.api;
    exports com.regexsolver.api.exceptions;

    // Jackson reflects over the generated DTOs to (de)serialize request and
    // response bodies; without this the package stays closed on the module path.
    opens com.regexsolver.api.generated.model to
        com.fasterxml.jackson.databind;

    requires java.net.http;
    requires java.logging;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.openapitools.jackson.nullable;

    requires static jakarta.annotation;
}
