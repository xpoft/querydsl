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
package com.mysema.query.sql;

import com.mysema.query.types.Ops;

/**
 * HSQLDBTemplates is an SQL dialect for HSQLDB
 *
 * @author tiwe
 *
 */
public class HSQLDBTemplates extends SQLTemplates {

    public HSQLDBTemplates() {
        this('\\', false);
    }
    
    public HSQLDBTemplates(boolean quote) {
        this('\\', quote);
    }

    public HSQLDBTemplates(char escape, boolean quote) {
        super("\"", escape, quote);
        //setDummyTable(null);
        setAutoIncrement(" identity");        
        add(Ops.TRIM, "trim(both from {0})");
        add(Ops.NEGATE, "{0} * -1", 7);
        
        add(Ops.MathOps.ROUND, "round({0},0)");
        add(Ops.MathOps.LN, "log({0})");
        add(Ops.MathOps.LOG, "(log({0}) / log({1}))");
        add(Ops.MathOps.COSH, "(exp({0}) + exp({0} * -1)) / 2");
        add(Ops.MathOps.COTH, "(exp({0} * 2) + 1) / (exp({0} * 2) - 1)");
        add(Ops.MathOps.SINH, "(exp({0}) - exp({0} * -1)) / 2");
        add(Ops.MathOps.TANH, "(exp({0} * 2) - 1) / (exp({0} * 2) + 1)");
        
//        add(Ops.DateTimeOps.DATE_ADD, "dateadd('{2s}', {1}, {0})");
//        add(Ops.DateTimeOps.DATE_DIFF, "datediff('{2s}', {0}, {1})");
    }
    
    @Override
    public String getTypeForCast(Class<?> cl) {
        return (cl.equals(String.class)) ? "varchar(10)" : getTypeForClass(cl);
    }

}
