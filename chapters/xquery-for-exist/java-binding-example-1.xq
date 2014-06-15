xquery version "1.0" encoding "UTF-8";

declare namespace javasystem = "java:java.lang.System";
declare namespace javamath = "java:java.lang.Math";

<JavaBindingExample>
    <JAVA_HOME>{javasystem:getenv("JAVA_HOME")}</JAVA_HOME>
    <SQRT2>{javamath:sqrt(2)}</SQRT2>
</JavaBindingExample>
