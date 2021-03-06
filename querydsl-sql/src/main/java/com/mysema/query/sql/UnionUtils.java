/*
 * Copyright 2012, Mysema Ltd
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
package com.mysema.query.sql;

import com.mysema.query.support.Expressions;
import com.mysema.query.types.Expression;
import com.mysema.query.types.ExpressionUtils;
import com.mysema.query.types.Path;
import com.mysema.query.types.SubQueryExpression;

/**
 * UnionUtils provides static utility methods for Union handling
 * 
 * @author tiwe
 *
 */
public final class UnionUtils {
    
    public static Expression<?> combineUnion(SubQueryExpression<?>[] union, Path<?> alias, SQLTemplates templates, boolean unionAll) {
        StringBuilder builder = new StringBuilder("(");
        String separator = unionAll ? templates.getUnionAll() : templates.getUnion();
        for (int i = 0; i < union.length; i++) {
            if (i > 0) builder.append(separator);
            builder.append("{"+i+"}");
        }
        builder.append(")");
        Expression<?> combined = Expressions.template(Object.class, builder.toString(), union);
        return ExpressionUtils.as((Expression)combined, alias);
    }
    
    
    private UnionUtils() {}

}
