package com.foghorn.rest.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.CodegenProperty;
import io.swagger.codegen.CodegenType;
import io.swagger.codegen.DefaultCodegen;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;

public class FoghornrestserverGenerator extends DefaultCodegen implements CodegenConfig {


    // generation root packages
    protected String rootGenPackage = "foghorn/em/api/rest/generated";

    // source folder where to write the files
    protected String sourceFolder = "";
    protected String apiVersion = "1.0.0";

    /**
     * Configures the type of generator.
     *
     * @return the CodegenType for this generator
     * @see io.swagger.codegen.CodegenType
     */
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    /**
     * Configures a friendly name for the generator.  This will be used by the generator to select
     * the library with the -l flag.
     *
     * @return the friendly name for the generator
     */
    public String getName() {
        return "FoghornRestServer";
    }

    /**
     * Returns human-friendly help for the generator.  Provide the consumer with help tips,
     * parameters here
     *
     * @return A string value for the help message
     */
    public String getHelp() {
        return "Generates a FoghornRestServer client library.";
    }

    public FoghornrestserverGenerator() {
        super();

        this.typeMapping.clear();
        this.typeMapping.put("array", "std::vector");
        this.typeMapping.put("map", "std::map");
        this.typeMapping.put("boolean", "bool");
        this.typeMapping.put("string", "std::string");
        this.typeMapping.put("number", "double");
        this.typeMapping.put("integer", "int");
        this.typeMapping.put("object", "std::string");
        this.typeMapping.put("null", "nullptr");

        modelNameSuffix = "*";

        // set the output folder here
        outputFolder = "generated-code/FoghornRestServer";

        /**
         * Models.  You can write model files using the modelTemplateFiles map.
         * if you want to create one template for file, you can do so here.
         * for multiple files for model, just put another entry in the `modelTemplateFiles` with
         * a different extension
         */
        modelTemplateFiles.put("model-cc.mustache", ".cc");
        modelTemplateFiles.put("model-h.mustache", ".h");

        /**
         * Api classes.  You can write classes for each Api file with the apiTemplateFiles map.
         * as with models, add multiple entries with different extensions for multiple files per
         * class
         */
        apiTemplateFiles.put("api-cc.mustache", ".cc");
        apiTemplateFiles.put("api-h.mustache", ".h");

        /**
         * Template Location.  This is the location which templates will be read from.  The generator
         * will use the resource stream to attempt to read the templates.
         */
        templateDir = "FoghornRestServer";

        /**
         * Api Package.  Optional, if needed, this can be used in templates
         */
        apiPackage = "api";

        /**
         * Model Package.  Optional, if needed, this can be used in templates
         */
        modelPackage = "model";

        /**
         * Reserved words.  Override this with reserved words specific to your language
         */
        reservedWords = new HashSet<String>(
                Arrays.asList(
                        "sample1",  // replace with static values
                        "sample2")
        );

        /**
         * Additional Properties.  These values can be passed to the templates and
         * are available in models, apis, and supporting files
         */
        additionalProperties.put("apiVersion", apiVersion);

        /**
         * Supporting Files.  You can write single files for the generator with the
         * entire object tree available.  If the input file has a suffix of `.mustache
         * it will be processed by the template engine.  Otherwise, it will be copied
         */
//    supportingFiles.add(new SupportingFile("myFile.mustache",   // the input template or file
//      "",                                                       // the destination folder, relative `outputFolder`
//      "myFile.sample")                                          // the output file
//    );

        /**
         * Language Specific Primitives.  These types will not trigger imports by
         * the client generator
         */
//    languageSpecificPrimitives = new HashSet<String>(
//      Arrays.asList(
//        "Type1",      // replace these with your types
//        "Type2")
//    );
    }


    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {

        List<Map<String, String>> imports = (List<Map<String, String>>) objs.get("imports");
        List<Map<String, String>> modifiedImports = new ArrayList<>();
        if (imports != null) {
            for (Map<String, String> importMap : imports) {
                String importClass = importMap.get("import");
                if (importClass.endsWith("*")) {
                    Map<String, String> modifiedImport = new HashMap<>();
                    modifiedImport.put("importRelative",
                            this.rootGenPackage + "/" + importClass.replace("*", "").replace(".", "/")
                                    + ".h");
                    modifiedImports.add(modifiedImport);
                }
            }
        }
        objs.put("imports", modifiedImports);
        return objs;
    }

    @Override
    public String toDefaultValue(Property p) {
        if (p instanceof ObjectProperty) {
            return "\"\"";
        } else if (p instanceof StringProperty) {
            StringProperty sp = (StringProperty) p;
            return null == sp.getDefault() ? "\"\"" : sp.getDefault();
        } else if (p instanceof BooleanProperty) {
            BooleanProperty bp = (BooleanProperty) p;
            return bp.getDefault() != null ? bp.getDefault().toString() : "false";
        } else if (p instanceof DoubleProperty) {
            DoubleProperty dp3 = (DoubleProperty) p;
            return dp3.getDefault() != null ? dp3.getDefault().toString() : "0.0";
        } else if (p instanceof FloatProperty) {
            FloatProperty dp2 = (FloatProperty) p;
            return dp2.getDefault() != null ? dp2.getDefault().toString() : "0.0";
        } else if (p instanceof IntegerProperty) {
            IntegerProperty dp1 = (IntegerProperty) p;
            return dp1.getDefault() != null ? dp1.getDefault().toString() : "0";
        } else if (p instanceof LongProperty) {
            LongProperty dp = (LongProperty) p;
            return dp.getDefault() != null ? dp.getDefault().toString() : "0";
        } else if (p instanceof ArrayProperty) {
            ArrayProperty ap = (ArrayProperty) p;
            return "{}";
        } else {
            return "nullptr";
        }
    }

    @Override
    public CodegenProperty fromProperty(String name, Property p) {
        CodegenProperty property = super.fromProperty(name, p);
        property.vendorExtensions.put("jsonType", toJsonType(p));
        return property;
    }

    public String toJsonType(Property p) {
        if (p instanceof ObjectProperty) {
            return "string";
        } else if (p instanceof StringProperty) {
            return "string";
        } else if (p instanceof BooleanProperty) {
            return "boolean";
        } else if (p instanceof DoubleProperty) {
            return "number";
        } else if (p instanceof FloatProperty) {
            return "number";
        } else if (p instanceof IntegerProperty) {
            return "number";
        } else if (p instanceof LongProperty) {
            return "number";
        } else if (p instanceof ArrayProperty) {
            return "array";
        } else {
            return "null";
        }
    }

    /**
     * Escapes a reserved word as defined in the `reservedWords` array. Handle escaping those terms
     * here.  This logic is only called if a variable matches the reseved words
     *
     * @return the escaped term
     */
    @Override
    public String escapeReservedWord(String name) {
        return "_" + name;  // add an underscore to the name
    }

    /**
     * Location to write model files.  You can use the modelPackage() as defined when the class is
     * instantiated
     */
    public String modelFileFolder() {
        return outputFolder + "/" + sourceFolder + "/" + modelPackage().replace('.', File.separatorChar);
    }

    /**
     * Location to write api files.  You can use the apiPackage() as defined when the class is
     * instantiated
     */
    @Override
    public String apiFileFolder() {
        return outputFolder + "/" + sourceFolder + "/" + apiPackage().replace('.', File.separatorChar);
    }

    /**
     * Optional - type declaration.  This is a String which is used by the templates to instantiate
     * your types.  There is typically special handling for different property types
     *
     * @return a string value used as the `dataType` field for model templates, `returnType` for api
     * templates
     */
    @Override
    public String getTypeDeclaration(Property p) {
        if (p instanceof ArrayProperty) {
            ArrayProperty ap = (ArrayProperty) p;
            Property inner = ap.getItems();
            return getSwaggerType(p) + "<" + getTypeDeclaration(inner) + ">";
        } else if (p instanceof MapProperty) {
            MapProperty mp = (MapProperty) p;
            Property inner = mp.getAdditionalProperties();
            return getSwaggerType(p) + "[String, " + getTypeDeclaration(inner) + "]";
        }
        return super.getTypeDeclaration(p);
    }

    /**
     * Optional - swagger type conversion.  This is used to map swagger types in a `Property` into
     * either language specific types via `typeMapping` or into complex models if there is not a
     * mapping.
     *
     * @return a string value of the type or complex model for this property
     * @see io.swagger.models.properties.Property
     */
    @Override
    public String getSwaggerType(Property p) {
        String swaggerType = super.getSwaggerType(p);
        String type = null;
        if (typeMapping.containsKey(swaggerType)) {
            return typeMapping.get(swaggerType);
        }
        return toModelName(swaggerType);
    }
}