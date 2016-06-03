package com.foghorn.rest.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.CodegenModel;
import io.swagger.codegen.CodegenModelType;
import io.swagger.codegen.CodegenModelFactory;
import io.swagger.codegen.CodegenProperty;
import io.swagger.codegen.CodegenParameter;
import io.swagger.codegen.CodegenType;
import io.swagger.codegen.DefaultCodegen;
import io.swagger.util.Json;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.models.parameters.Parameter;

public class FoghornrestserverGenerator extends DefaultCodegen implements CodegenConfig {

    public static class FoghornCodegenModel extends CodegenModel {
       public String validatorClass;
       public List<Map<String,Object>> validators;
    }

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
        this.typeMapping.put("object", "Json");
        this.typeMapping.put("null", "nullptr");

	this.importMapping.clear();
        this.importMapping.put("Error", null);

	CodegenModelFactory.setTypeMapping(CodegenModelType.MODEL, FoghornCodegenModel.class);

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
        apiTemplateFiles.put("proxy-cc.mustache", "Proxy.cc");
        apiTemplateFiles.put("proxy-h.mustache", "Proxy.h");

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
         * Additional Properties.  These values can be passed to the templates and
         * are available in models, apis, and supporting files
         */
        additionalProperties.put("apiVersion", apiVersion);

        /**
         * Language Specific Primitives.  These types will not trigger imports by
         * the client generator
         */
        languageSpecificPrimitives = new HashSet<String>(
                Arrays.asList(
                        "std::string",
                        "bool",
                        "int",
                        "float",
                        "double",
		        "Json",
                        "std::map",
                        "std::vector"
		 )
        );
    }

    @Override
    public String toModelImport(String name) {
       return importMapping.containsKey(name) ? importMapping.get(name) : name;
    }

    @Override
    public Map<String, Object> postProcessModels(Map<String, Object> objs) {
       for (Object _mo : (List<Object>) objs.get("models")) {
          Map<String, Object> mo = (Map<String, Object>) _mo;
          FoghornCodegenModel fhcm = (FoghornCodegenModel) mo.get("model");
	  for (String suffix : (new String[] { "New", "Edit" })) {
             if (fhcm.name.endsWith(suffix)) {
                fhcm.validatorClass = fhcm.name.substring(0, fhcm.name.length() - suffix.length());
	     }
	  }
       }
       return objs;
    }

    @Override
    public Map<String, Object> postProcessAllModels(Map<String, Object> objs) {
       // Index all CodegenModels by model name.
       Map<String, FoghornCodegenModel> allModels = new HashMap<String, FoghornCodegenModel>();
       for (Map.Entry<String, Object> entry : objs.entrySet()) {
           String modelName = toModelName(entry.getKey());
           Map<String, Object> inner = (Map<String, Object>) entry.getValue();
           List<Map<String, Object>> models = (List<Map<String, Object>>) inner.get("models");
           for (Map<String, Object> mo : models) {
              FoghornCodegenModel cm = (FoghornCodegenModel) mo.get("model");
	      if (cm.validatorClass == null) {
	         cm.validators = new ArrayList<Map<String,Object>>();
	      }
              allModels.put(modelName, cm);
           }
       }

       // Each "validator" class registers itself with its target
       for (FoghornCodegenModel model : allModels.values()) {
          if (model.validatorClass != null) {
             FoghornCodegenModel targetClass = allModels.get(model.validatorClass);
	     Map<String,Object> validator = new HashMap<String,Object>();
	     String suffix = model.name.substring(targetClass.name.length());
             validator.put("validatorClass", suffix);
             validator.put("validatorParam", camelize(model.name, true));

	     // The class "targetClass" will have a "fromFoo" method that should
	     // copy over all fields shared by the two classes

             // Index all model's CodegenProperties by property name
             Map<String, CodegenProperty> modelVars = new HashMap<String, CodegenProperty>();
             for (CodegenProperty property : model.vars) {
	        modelVars.put(property.baseName, property);
	     }
	     // Each of targetClass's CodegenProperties, if it's also a Model property,
	     // with generate a setter call in the "fromFoo" method.
	     List<Map<String,String>> varsToCopy = new ArrayList<Map<String,String>>();
             for (CodegenProperty targetProperty : targetClass.vars) {
	        CodegenProperty sourceProperty = modelVars.get(targetProperty.baseName);
		if (sourceProperty != null) {
		   Map<String,String> varToCopy = new HashMap<String,String>();
		   varToCopy.put("getter", sourceProperty.getter);
		   varToCopy.put("setter", targetProperty.setter);
		   varsToCopy.add(varToCopy);
		}
	     }
	     if (!varsToCopy.isEmpty()) {
                validator.put("validatorVars", varsToCopy);
	     }
	     targetClass.validators.add(validator);
	  }
       }
       return objs;
    }

    @Override
    public CodegenParameter fromParameter(Parameter param, Set<String> imports) {
        CodegenParameter parameter = super.fromParameter(param, imports);
	if ((parameter.isPrimitiveType == null) || (parameter.isString != null)) {
           parameter.vendorExtensions.put("passByReference", "true");
	}
        return parameter;
    }

    @Override
    public String toDefaultValue(Property p) {
        if (p instanceof ObjectProperty) {
            return "{}";
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
        property.vendorExtensions.put("propertyClass", p.getClass().getName());
        property.vendorExtensions.put("jsonType", toJsonType(p));
	if (needsPassByReference(p)) {
	   property.vendorExtensions.put("passByReference", "true");
        }
        return property;
    }

    public String toJsonType(Property p) {
        if (p instanceof StringProperty) {
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
        } else if (p instanceof RefProperty) {
            return "object";
        }
	return null;
    }

    public boolean needsPassByReference(Property p) {
        return (p instanceof ObjectProperty) ||
               (p instanceof StringProperty) ||
               (p instanceof ArrayProperty) ||
               (p instanceof MapProperty);
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
            return "std::vector" + "<" + getTypeDeclaration(inner) + ">";
        } else if (p instanceof MapProperty) {
            MapProperty mp = (MapProperty) p;
            Property inner = mp.getAdditionalProperties();
            return "std::map<std::string," + getTypeDeclaration(inner) + ">";
        } else if (p instanceof RefProperty) {
	    return super.getTypeDeclaration(p) + "*";
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
        if (typeMapping.containsKey(swaggerType)) {
            return typeMapping.get(swaggerType);
        }
        return swaggerType;
    }
}
