/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.util;

import org.drools.RuleBase;
import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseFactory;
import org.drools.StatefulSession;
import org.drools.base.ClassFieldAccessorCache;
import org.drools.base.ClassFieldAccessorStore;
import org.drools.base.ClassObjectType;
import org.drools.base.evaluators.EvaluatorRegistry;
import org.drools.base.evaluators.Operator;
import org.drools.common.BetaConstraints;
import org.drools.common.InternalFactHandle;
import org.drools.common.SingleBetaConstraints;
import org.drools.core.util.AbstractHashTable;
import org.drools.core.util.Entry;
import org.drools.core.util.Iterator;
import org.drools.core.util.index.RightTupleIndexHashTable;
import org.drools.core.util.index.RightTupleIndexHashTable.FieldIndexHashTableFullIterator;
import org.drools.core.util.index.RightTupleList;
import org.drools.reteoo.BetaMemory;
import org.drools.reteoo.LeftTupleImpl;
import org.drools.reteoo.NodeTypeEnums;
import org.drools.reteoo.RightTuple;
import org.drools.rule.Declaration;
import org.drools.rule.MvelConstraintTestUtil;
import org.drools.rule.Pattern;
import org.drools.spi.BetaNodeFieldConstraint;
import org.drools.spi.InternalReadAccessor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class RightTupleIndexHashTableIteratorTest {

    public static EvaluatorRegistry registry = new EvaluatorRegistry();

    @Test
    public void test1() {
        BetaNodeFieldConstraint constraint0 = getConstraint( "d", Operator.EQUAL, "this", Foo.class );
        BetaNodeFieldConstraint[] constraints = new BetaNodeFieldConstraint[] { constraint0 };
        
        RuleBaseConfiguration config = new RuleBaseConfiguration();

        BetaConstraints betaConstraints = null;
        
        betaConstraints = new SingleBetaConstraints(constraints, config);
        
        BetaMemory betaMemory = betaConstraints.createBetaMemory( config, NodeTypeEnums.JoinNode );
        
        RuleBase rb = RuleBaseFactory.newRuleBase();
        StatefulSession ss = rb.newStatefulSession();
        
        InternalFactHandle fh1 = (InternalFactHandle) ss.insert( new Foo( "brie", 1) );
        InternalFactHandle fh2 = (InternalFactHandle) ss.insert( new Foo( "brie", 1) );
        InternalFactHandle fh3 = (InternalFactHandle) ss.insert( new Foo( "soda", 1) );
        InternalFactHandle fh4 = (InternalFactHandle) ss.insert( new Foo( "soda", 1) );
        InternalFactHandle fh5 = (InternalFactHandle) ss.insert( new Foo( "bread", 3) );
        InternalFactHandle fh6 = (InternalFactHandle) ss.insert( new Foo( "bread", 3) );
        InternalFactHandle fh7 = (InternalFactHandle) ss.insert( new Foo( "cream", 3) );
        InternalFactHandle fh8 = (InternalFactHandle) ss.insert( new Foo( "gorda", 15) );
        InternalFactHandle fh9 = (InternalFactHandle) ss.insert( new Foo( "beer", 16) );
        
        InternalFactHandle fh10 = (InternalFactHandle) ss.insert( new Foo( "mars", 0) );
        InternalFactHandle fh11 = (InternalFactHandle) ss.insert( new Foo( "snicker", 0) );
        InternalFactHandle fh12 = (InternalFactHandle) ss.insert( new Foo( "snicker", 0) );
        InternalFactHandle fh13 = (InternalFactHandle) ss.insert( new Foo( "snicker", 0) );
        
        betaMemory.getRightTupleMemory().add( new RightTuple( fh1, null ) );
        betaMemory.getRightTupleMemory().add( new RightTuple( fh2, null ) );
        betaMemory.getRightTupleMemory().add( new RightTuple( fh3, null ) );
        betaMemory.getRightTupleMemory().add( new RightTuple( fh4, null ) );
        betaMemory.getRightTupleMemory().add( new RightTuple( fh5, null ) );
        betaMemory.getRightTupleMemory().add( new RightTuple( fh6, null ) );
        betaMemory.getRightTupleMemory().add( new RightTuple( fh7, null ) );
        betaMemory.getRightTupleMemory().add( new RightTuple( fh8, null ) );
        betaMemory.getRightTupleMemory().add( new RightTuple( fh9, null ) );
        
        RightTupleIndexHashTable hashTable = (RightTupleIndexHashTable) betaMemory.getRightTupleMemory();
        // can't create a 0 hashCode, so forcing 
        RightTupleList rightTupleList = new RightTupleList();
        rightTupleList.add( new RightTuple( fh10, null) );
        hashTable.getTable()[0] = rightTupleList;
        rightTupleList = new RightTupleList();
        rightTupleList.add( new RightTuple( fh11, null ) );
        rightTupleList.add( new RightTuple( fh12, null ) );
        rightTupleList.add( new RightTuple( fh13, null ) );
        ((RightTupleList)hashTable.getTable()[0]).setNext( rightTupleList );
        
        Entry[] table = hashTable.getTable();
        List list = new ArrayList();
        for ( int i = 0; i < table.length; i++ ) {
            if ( table[i] != null ) {
                List entries = new ArrayList();
                entries.add( i );
                Entry entry = table[i];
                while ( entry != null ) {
                    entries.add( entry );
                    entry = entry.getNext();
                }
                list.add( entries.toArray() );
            }
        }
        assertEquals( 5, list.size() );
        
        Object[] entries = (Object[]) list.get( 0 );
        assertEquals( 0, entries[0]);
        assertEquals( 3, entries.length );
        
        entries = (Object[]) list.get( 1 );
        assertEquals( 102, entries[0]);
        assertEquals( 2, entries.length );
        
        entries = (Object[]) list.get( 2 );
        assertEquals( 103, entries[0]);
        assertEquals( 2, entries.length );
        
        entries = (Object[]) list.get( 3 );
        assertEquals( 115, entries[0]);
        assertEquals( 3, entries.length );
        
        entries = (Object[]) list.get( 4 );
        assertEquals( 117, entries[0]);
        assertEquals( 3, entries.length );
        
        
        //System.out.println( entries );

        list = new ArrayList<LeftTupleImpl>();
        Iterator it = betaMemory.getRightTupleMemory().iterator();
        for ( RightTuple rightTuple = ( RightTuple ) it.next(); rightTuple != null; rightTuple = ( RightTuple ) it.next() ) {
            list.add( rightTuple );
        }
        
        assertEquals( 13, list.size() );
             
    }
    
    @Test
    public void testLastBucketInTheTable() {
        // JBRULES-2574
        // setup the entry array with an element in the first bucket, one 
        // in the middle and one in the last bucket
        Entry[] entries = new Entry[10];
        entries[0] = mock( RightTupleList.class );
        entries[5] = mock( RightTupleList.class );
        entries[9] = mock( RightTupleList.class );

        RightTuple[] tuples = new RightTuple[]{mock( RightTuple.class ), mock( RightTuple.class ), mock( RightTuple.class )};

        // set return values for methods
        when( entries[0].getNext() ).thenReturn( null );
        when( ((RightTupleList) entries[0]).getFirst() ).thenReturn( tuples[0] );
        
        when( entries[5].getNext() ).thenReturn( null );
        when( ((RightTupleList) entries[5]).getFirst(  ) ).thenReturn( tuples[1] );

        when( entries[9].getNext() ).thenReturn( null );
        when( ((RightTupleList) entries[9]).getFirst( ) ).thenReturn( tuples[2] );

        // create the mock table for the iterator
        AbstractHashTable table = mock( AbstractHashTable.class );
        when( table.getTable() ).thenReturn( entries );

        // create the iterator
        FieldIndexHashTableFullIterator iterator = new FieldIndexHashTableFullIterator( table );

        // test it
        assertThat( iterator.next(),
                    sameInstance( (Object) tuples[0] ) );
        assertThat( iterator.next(),
                    sameInstance( (Object) tuples[1] ) );
        assertThat( iterator.next(),
                    sameInstance( (Object) tuples[2] ) );
        assertThat( iterator.next(),
                    is( (Object) null ) );

    }

    protected BetaNodeFieldConstraint getConstraint(String identifier,
                                                    Operator operator,
                                                    String fieldName,
                                                    Class clazz) {
        ClassFieldAccessorStore store = new ClassFieldAccessorStore();
        store.setClassFieldAccessorCache( new ClassFieldAccessorCache( Thread.currentThread().getContextClassLoader() ) );
        store.setEagerWire( true );
        InternalReadAccessor extractor = store.getReader( clazz,
                                                          fieldName,
                                                          getClass().getClassLoader() );
        Declaration declaration = new Declaration( identifier,
                                                   extractor,
                                                   new Pattern( 0,
                                                                new ClassObjectType( clazz ) ) );

        String expression = fieldName + " " + operator.getOperatorString() + " " + declaration.getIdentifier();
        return new MvelConstraintTestUtil(expression, declaration, extractor);
    }
    
    public static class Foo {
        private String val;
        private int hashCode;
        
        public Foo(String val, int hashCode) {
            this.val = val;
            this.hashCode = hashCode;
        }

        public String getVal() {
            return val;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if ( this == obj ) return true;
            if ( obj == null ) return false;
            if ( getClass() != obj.getClass() ) return false;
            Foo other = (Foo) obj;
            if ( hashCode != other.hashCode ) return false;
            if ( val == null ) {
                if ( other.val != null ) return false;
            } else if ( !val.equals( other.val ) ) return false;
            return true;
        }
        
        
    }
       
}
