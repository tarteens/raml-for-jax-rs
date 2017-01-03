package org.raml.jaxrs.generator.builders.extensions.types;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.raml.jaxrs.generator.CurrentBuild;
import org.raml.jaxrs.generator.Names;
import org.raml.jaxrs.generator.builders.CodeContainer;
import org.raml.jaxrs.generator.builders.JavaPoetTypeGenerator;
import org.raml.jaxrs.generator.builders.TypeGenerator;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.UnionTypeDeclaration;

import javax.lang.model.element.Modifier;
import java.io.IOException;

/**
 * Created by Jean-Philippe Belanger on 1/2/17.
 * Just potential zeroes and ones
 */
public class UnionSerializationGenerator implements JavaPoetTypeGenerator {
    private final CurrentBuild currentBuild;
    private final UnionTypeDeclaration unionTypeDeclaration;
    private final ClassName deserializer;

    public UnionSerializationGenerator(CurrentBuild currentBuild, UnionTypeDeclaration unionTypeDeclaration,
            ClassName deserializer) {
        this.currentBuild = currentBuild;
        this.unionTypeDeclaration = unionTypeDeclaration;
        this.deserializer = deserializer;
    }

    @Override
    public void output(CodeContainer<TypeSpec.Builder> rootDirectory) throws IOException {

        ClassName unionTypeName = ClassName.get(currentBuild.getModelPackage(), Names
                .typeName(unionTypeDeclaration.name(), "Union"));
        TypeSpec.Builder builder = TypeSpec.classBuilder(deserializer)
                .superclass(ParameterizedTypeName.get(ClassName.get(StdSerializer.class), unionTypeName))
                .addMethod(
                        MethodSpec.constructorBuilder()
                                .addModifiers(Modifier.PUBLIC)
                                .addCode("super($T.class);", unionTypeName).build()

                );
        MethodSpec.Builder serialize = MethodSpec.methodBuilder("serialize")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(unionTypeName, "object").build())
                .addParameter(ParameterSpec.builder(ClassName.get(JsonGenerator.class), "jsonGenerator").build())
                .addParameter(ParameterSpec.builder(ClassName.get(SerializerProvider.class), "jsonSerializerProvider").build())
                .addException(IOException.class)
                .addException(JsonProcessingException.class);

        for (TypeDeclaration typeDeclaration : unionTypeDeclaration.of()) {

            String isMethod = Names.methodName("is", typeDeclaration.name());
            String getMethod = Names.methodName("get", typeDeclaration.name());
            serialize.beginControlFlow("if ( object." + isMethod + "())"  );
            serialize.addStatement("jsonGenerator.writeObject(object." + getMethod + "())");
            serialize.addStatement("return");
            serialize.endControlFlow();
        }

        serialize.addStatement("throw new $T($S + object)", IOException.class, "Can't figure out type of object");

        builder.addMethod(serialize.build());


        rootDirectory.into(builder);

    }

    @Override
    public void output(CodeContainer<TypeSpec.Builder> rootDirectory, TYPE type) throws IOException {

        output(rootDirectory);
    }

    @Override
    public TypeName getGeneratedJavaType() {
        return deserializer;
    }
}