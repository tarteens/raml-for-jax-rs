/*
 * Copyright 2013-2017 (c) MuleSoft, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.raml.jaxrs.generator;

import java.util.List;

import org.raml.jaxrs.generator.builders.extensions.types.TypeExtensionHelper;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.api.model.v10.declarations.AnnotationRef;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec.Builder;

public class SensitiveInfoTypeExtension extends TypeExtensionHelper {

  @Override
  public void onFieldImplementation(CurrentBuild currentBuild, Builder typeSpec, TypeDeclaration typeDeclaration) {
    List<AnnotationRef> annotation = typeDeclaration.annotations();

    if (annotation != null && !annotation.isEmpty()) {
      for (AnnotationRef ref : annotation) {
        System.out.println(ref.name());
        if (ref.name().equals("(sensitive-info)")) {
          typeSpec.addAnnotation(AnnotationSpec.builder(Secured.class).build());
        }
      }
    }
  }

}
