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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.mysema.query.sql.domain.QEmployee;
import com.mysema.query.types.ConstantImpl;
import com.mysema.query.types.Expression;
import com.mysema.query.types.SubQueryExpression;
import com.mysema.query.types.query.ListSubQuery;

public class SQLSubQueryTest {
    
    @Test
    public void Multiple_Projections(){
        SQLSubQuery query = new SQLSubQuery();
        query.from(QEmployee.employee);
        assertEquals(1, query.list(QEmployee.employee).getMetadata().getProjection().size());
        assertEquals(1, query.list(QEmployee.employee).getMetadata().getProjection().size());
    }

    @Test
    public void List(){
        SQLSubQuery query = new SQLSubQuery();
        query.from(QEmployee.employee);
        ListSubQuery<?> subQuery = query.list(QEmployee.employee.id, "XXX", QEmployee.employee.firstname);
        List<? extends Expression<?>> exprs = subQuery.getMetadata().getProjection();
        assertEquals(QEmployee.employee.id, exprs.get(0));
        assertEquals(new ConstantImpl<String>("XXX") , exprs.get(1));
        assertEquals(QEmployee.employee.firstname, exprs.get(2));
    }
    
    @Test
    public void Unique(){
        SQLSubQuery query = new SQLSubQuery();
        query.from(QEmployee.employee);
        SubQueryExpression<?> subQuery = query.unique(QEmployee.employee.id, "XXX", QEmployee.employee.firstname);
        List<? extends Expression<?>> exprs = subQuery.getMetadata().getProjection();
        assertEquals(QEmployee.employee.id, exprs.get(0));
        assertEquals(new ConstantImpl<String>("XXX") , exprs.get(1));
        assertEquals(QEmployee.employee.firstname, exprs.get(2));
    }
    
}
