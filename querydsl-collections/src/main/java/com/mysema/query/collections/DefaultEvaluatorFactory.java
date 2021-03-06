/*
 * Copyright 2011, Mysema Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mysema.query.collections;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import com.google.common.primitives.Primitives;
import com.mysema.codegen.Evaluator;
import com.mysema.codegen.EvaluatorFactory;
import com.mysema.codegen.model.ClassType;
import com.mysema.codegen.model.SimpleType;
import com.mysema.codegen.model.Type;
import com.mysema.codegen.model.TypeCategory;
import com.mysema.codegen.model.Types;
import com.mysema.codegen.support.ClassUtils;
import com.mysema.query.JoinExpression;
import com.mysema.query.JoinType;
import com.mysema.query.QueryMetadata;
import com.mysema.query.support.CollectionAnyVisitor;
import com.mysema.query.support.Context;
import com.mysema.query.types.Expression;
import com.mysema.query.types.FactoryExpression;
import com.mysema.query.types.Operation;
import com.mysema.query.types.ParamExpression;
import com.mysema.query.types.ParamNotSetException;
import com.mysema.query.types.Predicate;

/**
 * DefaultEvaluatorFactory provides Java source templates for evaluation of ColQuery queries
 *
 * @author tiwe
 *
 */
public class DefaultEvaluatorFactory {

    private final EvaluatorFactory factory;

    private final ColQueryTemplates templates;

    /**
     * @param templates
     */
    public DefaultEvaluatorFactory(ColQueryTemplates templates){
        this(templates,
        (URLClassLoader)Thread.currentThread().getContextClassLoader(),
        ToolProvider.getSystemJavaCompiler());
    }

    protected DefaultEvaluatorFactory(ColQueryTemplates templates,
            URLClassLoader classLoader, JavaCompiler compiler){
        this.templates = templates;
        this.factory = new EvaluatorFactory(classLoader, compiler);
    }

    /**
     * Create an Evaluator for the given query sources and projection
     *
     * @param <T>
     * @param sources
     * @param projection
     * @return
     */
    public <T> Evaluator<T> create(QueryMetadata metadata, List<? extends Expression<?>> sources, 
            Expression<T> projection) {
        ColQuerySerializer serializer = new ColQuerySerializer(templates);
        serializer.handle(projection);

        Map<Object,String> constantToLabel = serializer.getConstantToLabel();
        Map<String, Object> constants = getConstants(metadata, constantToLabel);
        Class<?>[] types = new Class<?>[sources.size()];
        String[] names = new String[sources.size()];
        for (int i = 0; i < sources.size(); i++) {
            types[i] = sources.get(i).getType();
            names[i] = sources.get(i).toString();
        }

        // normalize types
        for (int i = 0; i < types.length; i++) {
            if (Primitives.isWrapperType(types[i])){
                types[i] = Primitives.unwrap(types[i]);
            }
        }

        String javaSource = serializer.toString();
        if (projection instanceof FactoryExpression<?>) {
            javaSource = "("+ClassUtils.getName(projection.getType())+")(" + javaSource+")";
        }
        
        return factory.createEvaluator("return " + javaSource +";", projection.getType(), names, 
                types, constants);
    }

    /**
     * Create an Evaluator for the given source and filter
     *
     * @param <T>
     * @param source
     * @param filter
     * @return
     */
    public <T> Evaluator<List<T>> createEvaluator(QueryMetadata metadata, 
            Expression<? extends T> source, Predicate filter){
        String typeName = ClassUtils.getName(source.getType());
        ColQuerySerializer ser = new ColQuerySerializer(templates);
        ser.append("java.util.List<"+typeName+"> rv = new java.util.ArrayList<"+typeName+">();\n");
        ser.append("for (" + typeName + " "+ source + " : " + source + "_){\n");
        ser.append("    if (").handle(filter).append("){\n");
        ser.append("        rv.add("+source+");\n");
        ser.append("    }\n");
        ser.append("}\n");
        ser.append("return rv;");

        Map<Object,String> constantToLabel = ser.getConstantToLabel();
        Map<String, Object> constants = getConstants(metadata, constantToLabel);

        Type sourceType = new ClassType(TypeCategory.SIMPLE, source.getType());
        ClassType sourceListType = new ClassType(TypeCategory.SIMPLE, Iterable.class, sourceType);
        
        return factory.createEvaluator(
                ser.toString(),
                sourceListType,
                new String[]{source+"_"},
                new Type[]{sourceListType},
                new Class[]{Iterable.class},
                constants);
    }

    /**
     * Create an Evaluator for the given sources and the given optional filter
     *
     * @param joins
     * @param filter
     * @return
     */
    @SuppressWarnings("unchecked")
    public Evaluator<List<Object[]>> createEvaluator(QueryMetadata metadata, 
            List<JoinExpression> joins, @Nullable Predicate filter){
        List<String> sourceNames = new ArrayList<String>();
        List<Type> sourceTypes = new ArrayList<Type>();
        List<Class> sourceClasses = new ArrayList<Class>();
        StringBuilder vars = new StringBuilder();
        ColQuerySerializer ser = new ColQuerySerializer(templates);
        ser.append("java.util.List<Object[]> rv = new java.util.ArrayList<Object[]>();\n");

        List<String> anyJoinMatchers = new ArrayList<String>();

        // creating context
        for (JoinExpression join : joins) {
            Expression<?> target = join.getTarget();
            String typeName = com.mysema.codegen.support.ClassUtils.getName(target.getType());
            if (vars.length() > 0) {
                vars.append(",");
            }
            if (join.getType() == JoinType.DEFAULT) {
                ser.append("for (" + typeName + " "+ target + " : " + target + "_){\n");
                vars.append(target);
                sourceNames.add(target+"_");
                sourceTypes.add(new SimpleType(Types.ITERABLE, new ClassType(TypeCategory.SIMPLE,target.getType())));
                sourceClasses.add(Iterable.class);

            } else if (join.getType() == JoinType.INNERJOIN) {
                Operation alias = (Operation)join.getTarget();
                boolean colAnyJoin = join.getCondition() != null && join.getCondition().toString().equals("any");
                String matcher = null;
                if (colAnyJoin){
                    matcher = alias.getArg(1).toString() + "_matched";
                    ser.append("boolean " + matcher + " = false;\n");
                    anyJoinMatchers.add(matcher);
                }
                ser.append("for (" + typeName + " " + alias.getArg(1) + " : ");
                if (colAnyJoin) {
                    Context context = new Context();
                    Expression<?> replacement = (Expression<?>) alias.getArg(0)
                            .accept(CollectionAnyVisitor.DEFAULT, context);
                    ser.handle(replacement);                    
                } else {
                    ser.handle(alias.getArg(0));
                }
                if (alias.getArg(0).getType().equals(Map.class)) {
                    ser.append(".values()");
                }
                ser.append("){\n");
                if (matcher != null) {
                    ser.append("if (!" + matcher + "){\n");
                }                
                vars.append(alias.getArg(1));

            } else {
                throw new IllegalArgumentException("Illegal join expression " + join);
            }
        }

        // filter
        if (filter != null) {
            ser.append("if (");
            ser.handle(filter).append("){\n");
            for (String matcher : anyJoinMatchers) {
                ser.append("    "+ matcher + " = true;\n");
            }
            ser.append("    rv.add(new Object[]{"+vars+"});\n");
            ser.append("}\n");
        } else {
            ser.append("rv.add(new Object[]{"+vars+"});\n");
        }

        // closing context
        int amount = joins.size() + anyJoinMatchers.size();
        for (int i = 0; i < amount; i++) {
            ser.append("}\n");
        }
        ser.append("return rv;");

        Map<Object,String> constantToLabel = ser.getConstantToLabel();
        Map<String, Object> constants = getConstants(metadata, constantToLabel);

        ClassType projectionType = new ClassType(TypeCategory.LIST, List.class, Types.OBJECTS);
        return factory.createEvaluator(
                ser.toString(),
                projectionType,
                sourceNames.toArray(new String[sourceNames.size()]),
                sourceTypes.toArray(new Type[sourceTypes.size()]),
                sourceClasses.toArray(new Class[sourceClasses.size()]),
                constants);
    }

    private Map<String, Object> getConstants(QueryMetadata metadata, 
            Map<Object, String> constantToLabel) {
        Map<String,Object> constants = new HashMap<String,Object>();
        for (Map.Entry<Object,String> entry : constantToLabel.entrySet()) {
            if (entry.getKey() instanceof ParamExpression<?>) {
                Object value = metadata.getParams().get(entry.getKey());
                if (value == null) {
                    throw new ParamNotSetException((ParamExpression<?>) entry.getKey());
                }
                constants.put(entry.getValue(), value);
            } else {
                constants.put(entry.getValue(), entry.getKey());
            }
        }
        return constants;
    }

}
