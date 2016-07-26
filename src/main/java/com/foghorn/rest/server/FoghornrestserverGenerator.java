package com.foghorn.rest.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.lang.RuntimeException;

import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.CodegenModel;
import io.swagger.codegen.CodegenModelType;
import io.swagger.codegen.CodegenModelFactory;
import io.swagger.codegen.CodegenProperty;
import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.CodegenParameter;
import io.swagger.codegen.CodegenType;
import io.swagger.codegen.DefaultCodegen;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.util.Json;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.BaseIntegerProperty;
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
       public Boolean hasBarePtrs;
    }
    public static class FoghornCodegenOperation extends CodegenOperation {
       public String curlopMethod;
    }
    public static class FoghornCodegenProperty extends CodegenProperty {
       public String tester;
       public Boolean isJson;
       public Boolean isRef;
       public String jsonType;
       public String fieldType;
       public String paramType;
    }

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
	CodegenModelFactory.setTypeMapping(CodegenModelType.PROPERTY, FoghornCodegenProperty.class);
	CodegenModelFactory.setTypeMapping(CodegenModelType.OPERATION, FoghornCodegenOperation.class);

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
        apiTemplateFiles.put("client-cc.mustache", ".cc");
        apiTemplateFiles.put("client-h.mustache", ".h");
        apiTemplateFiles.put("server-cc.mustache", ".cc");
        apiTemplateFiles.put("server-h.mustache", ".h");

        /**
         * Template Location.  This is the location which templates will be read from.  The generator
         * will use the resource stream to attempt to read the templates.
         */
        templateDir = "FoghornRestServer";

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
                        "bool",
                        "int",
                        "long",
                        "float",
                        "double",
                        "std::string",
                        "std::map",
                        "std::vector",
		        "Json"
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

	  // If this model ends in "New" or "Edit", then the model with the basename
	  // will get a "fromNow" or "fromEdit" method that takes this model as a param.
	  for (String suffix : (new String[] { "New", "Edit" })) {
             if (fhcm.name.endsWith(suffix)) {
                fhcm.validatorClass = fhcm.name.substring(0, fhcm.name.length() - suffix.length());
	     }
	  }
	  for (CodegenProperty var : fhcm.vars) {
	     FoghornCodegenProperty fvar = (FoghornCodegenProperty) var;
	     // If this model has non-container refs, we add a "nullPtrs" method.
	     if ((fvar.isRef == Boolean.TRUE) && (fvar.isNotContainer == Boolean.TRUE)) {
	       fhcm.hasBarePtrs = true;
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
		if ((sourceProperty != null) &&
		    (sourceProperty.datatype.equals(targetProperty.datatype))) {
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
    public CodegenOperation fromOperation(String path, String httpMethod, Operation operation, Map<String, Model> definitions, Swagger swagger) {
       FoghornCodegenOperation op = (FoghornCodegenOperation) super.fromOperation(path, httpMethod, operation, definitions, swagger);
       op.curlopMethod = httpMethod.equals("delete") ? "delete_req" : httpMethod;
       return op;
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

        // Default values are supported for Booleans, Strings, and Numbers,
	// but not for more complex types like arrays

        if (p instanceof StringProperty) {
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
            return (dp2.getDefault() != null ? dp2.getDefault().toString() : "0.0") + "f";
        } else if (p instanceof IntegerProperty) {
            IntegerProperty dp1 = (IntegerProperty) p;
            return dp1.getDefault() != null ? dp1.getDefault().toString() : "0";
        } else if (p instanceof LongProperty) {
            LongProperty dp = (LongProperty) p;
            return (dp.getDefault() != null ? dp.getDefault().toString() : "0") + "L";
        }
	return null;
    }

    private boolean isSimpleType(Property p) {
      return (p instanceof BooleanProperty) ||  // field type bool
	     (p instanceof IntegerProperty) ||  // field type int
               (p instanceof BaseIntegerProperty) ||  // field type int
	     (p instanceof LongProperty)    ||  // field type long
	     (p instanceof FloatProperty)   ||  // field type float
	     (p instanceof DoubleProperty)  ||  // field type double
	     (p instanceof StringProperty)  ||  // field type std::string
             (p instanceof ObjectProperty)  ||  // field type Json
	     (p instanceof RefProperty);        // field type is another Model class
    }

    @Override
    public CodegenProperty fromProperty(String name, Property p) {
        FoghornCodegenProperty property = (FoghornCodegenProperty) super.fromProperty(name, p);
        System.out.println("processing property " +  name + ": "  + p);
        // The swagger specification supports a lot of different types for Properties,
	// but this code can only translate a subset of them into C++.
	// So throw an exception if the type is something we know isn't going to work:

        if (!isSimpleType(p) && !(p instanceof MapProperty) && !(p instanceof ArrayProperty)) {
	   throw new RuntimeException("Property \"" + p.getName() + "\" " +
	     "has unexpected Property class \"" + p.getClass().getName() + "\"");
	}
	if (p instanceof MapProperty) {
	   //throw new RuntimeException("Property \"" + p.getName() + "\" " +
	   //  "is a MapProperty.  Sorry Map implementation not complete yet.");
	}
	if ((p instanceof ArrayProperty) &&
	    !isSimpleType(((ArrayProperty) p).getItems())) {
           throw new RuntimeException("Property \"" + p.getName() + "\" " +
	     "is an array-of-array or array-of-map.  Not implemented.");
	}

	// Set custom booleans "isRef" and "isJson"

	if ((p instanceof RefProperty) ||
	    ((p instanceof ArrayProperty)
	      && ((ArrayProperty)p).getItems() instanceof RefProperty)) {
	   property.isRef = true;
	}
	if ((p instanceof ObjectProperty) ||
	    ((p instanceof ArrayProperty)
	      && ((ArrayProperty)p).getItems() instanceof ObjectProperty)) {
	   property.isJson = true;
	}

	// Set custom fields "jsonType", "fieldType", "paramType"

	property.jsonType  = toJsonType(p);
	property.fieldType = getFieldDeclaration(p);

	if ((p instanceof ObjectProperty) ||
            (p instanceof RefProperty) ||
            (p instanceof StringProperty) ||
            (p instanceof ArrayProperty)) {
	   property.paramType = "const " + property.datatype + "&";
	} else {
           property.paramType = property.datatype;
        }

        property.tester = "has" + getterAndSetterCapitalize(property.name);
        return property;
    }

    private String toJsonType(Property p) {
        if (p instanceof StringProperty) {
            return "string";
        }
	if (p instanceof BooleanProperty) {
            return "boolean";
        }
	if ((p instanceof DoubleProperty) ||
            (p instanceof FloatProperty) ||
            (p instanceof IntegerProperty) ||
            (p instanceof BaseIntegerProperty) ||
            (p instanceof LongProperty)) {
	    return "number";
        }
        return null;
    }

    public String getFieldDeclaration(Property p) {
        String typeDecl = super.getTypeDeclaration(p);
        if (p instanceof ArrayProperty) {
            ArrayProperty ap = (ArrayProperty) p;
            Property inner = ap.getItems();
            typeDecl = typeDecl + "<" + getFieldDeclaration(inner) + ">";
        } else if (p instanceof MapProperty) {
            MapProperty mp = (MapProperty) p;
            Property inner = mp.getAdditionalProperties();
            typeDecl = typeDecl + "<std::string, " + getFieldDeclaration(inner) + ">";
        } else if (p instanceof RefProperty) {
            typeDecl = typeDecl + "*";
	}
        return typeDecl;
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
     * Location to write model files.
     */
    @Override
    public String modelFileFolder() {
        return outputFolder + "/model";
    }

    /**
     * Location to write api files.  If the template is called "foo-cc.mustache",
     * it will go in the "foo" subdirectory.
     */
    @Override
    public String apiFileFolder() {
        return outputFolder;
    }
    @Override
    public String apiFilename(String templateName, String tag) {
       String suffix = apiTemplateFiles().get(templateName);
       String subdir = templateName.substring(0, templateName.indexOf('-'));
       return apiFileFolder() + "/" + subdir + "/" + toApiFilename(tag) + suffix;
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
        String typeDecl = super.getTypeDeclaration(p);
        if (p instanceof ArrayProperty) {
            ArrayProperty ap = (ArrayProperty) p;
            Property inner = ap.getItems();
            typeDecl = typeDecl + "<" + getTypeDeclaration(inner) + ">";
        } else if (p instanceof MapProperty) {
            MapProperty mp = (MapProperty) p;
            Property inner = mp.getAdditionalProperties();
            typeDecl = typeDecl + "<std::string, " + getTypeDeclaration(inner) + ">";
        }
        return typeDecl;
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
