# Swagger Codegen for the FoghornRestServer library


To generate the rest server stubs,

```
mvn package
```

A single jar file will be produced in `target/FoghornRestServer-swagger-codegen-1.0.0.jar`.  You can now use that with codegen:

```
java -DdebugModels -DdebugOperations -cp target/FoghornRestServer-swagger-codegen-1.0.0.jar:swagger-codegen-cli.jar io.swagger.codegen.Codegen -l FoghornRestServer -o <output dir> -i <path to swagger.yml>
```
