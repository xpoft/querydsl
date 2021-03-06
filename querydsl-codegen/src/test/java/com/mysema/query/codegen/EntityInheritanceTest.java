package com.mysema.query.codegen;

import org.junit.Ignore;

import com.mysema.query.annotations.QueryEntity;
import com.mysema.query.annotations.QuerySupertype;

@Ignore
public class EntityInheritanceTest {
    
    @QuerySupertype
    public static class TreeEntity<T extends TreeEntity<T>> {
        
        public Integer id;
        
        public T parent;
        
    }

    @QueryEntity
    public static class TestEntity extends TreeEntity<TestEntity> {
        
        public String name;
        
    }
    
}
