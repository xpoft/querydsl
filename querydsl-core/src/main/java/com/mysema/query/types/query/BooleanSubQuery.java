/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.query.types.query;

import com.mysema.query.QueryMetadata;
import com.mysema.query.types.Visitor;
import com.mysema.query.types.expr.EBoolean;
import com.mysema.query.types.operation.OBoolean;
import com.mysema.query.types.operation.Operator;
import com.mysema.query.types.operation.Ops;
import com.mysema.query.types.path.Path;

/**
 * Single result subquery
 * 
 * @author tiwe
 */
public final class BooleanSubQuery extends EBoolean implements SubQuery<Boolean>{

    private static final long serialVersionUID = -64156984110154969L;

    private final SubQueryMixin<Boolean> subQueryMixin;
    
    public BooleanSubQuery(QueryMetadata md) {
        subQueryMixin = new SubQueryMixin<Boolean>(md);
        subQueryMixin.setSelf(this);
    }
    
    @Override
    public void accept(Visitor v) {
        v.visit(this);        
    }

    @Override
    public boolean equals(Object o) {
       return subQueryMixin.equals(o);
    }
    
    @Override
    public EBoolean exists() {
        return subQueryMixin.exists();
    }
    
    @Override
    public QueryMetadata getMetadata() {
        return subQueryMixin.getMetadata();
    }

    @Override
    public int hashCode(){
        return subQueryMixin.hashCode();
    }
    
    @Override
    public EBoolean notExists() {
        return subQueryMixin.notExists();
    }

    @SuppressWarnings("unchecked")
    @Override
    public EBoolean as(Path<Boolean> alias) {
        return OBoolean.create((Operator)Ops.ALIAS, this, alias.asExpr());
    }

}