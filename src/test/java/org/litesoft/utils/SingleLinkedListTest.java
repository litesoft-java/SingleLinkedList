package org.litesoft.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SingleLinkedListTest {

    static class BackgroundRunner implements Runnable {
        private final FibGenerator_1_1 fibs = new FibGenerator_1_1();
        private final SingleLinkedList<Integer> sll;
        private int groupSize;

        public BackgroundRunner( int groupSize, SingleLinkedList<Integer> sll ) {
            this.sll = sll;
            this.groupSize = groupSize;
        }

        @Override
        public void run() {
            while ( groupSize > 0 ) {
                sll.append( fibs.next( groupSize-- ) );
                Sleeper.INSTANCE.forMillis( 15 );
            }
            sll.append( 0 ); // END!
        }
    }

    @Test
    void writerAndReaderThreadsConcurrentSLL() {
        ConcurrentSingleLinkedList<Integer> sll = new ConcurrentSingleLinkedList<>();

        Thread bgt = new Thread( new BackgroundRunner( 5, sll ) );
        bgt.setDaemon( true );

        FibGenerator_1_1 fibs = new FibGenerator_1_1();

        bgt.start();

        int offset = 0;
        long timeout = System.currentTimeMillis() + 5000;
        while ( System.currentTimeMillis() < timeout ) {
            if ( sll.isEmpty() ) {
                Sleeper.INSTANCE.forMillis( 5 );
                continue;
            }
            Integer actual = sll.remove();
            if ( (actual != null) && (0 == actual) ) { // terminator
                break;
            }
            Integer expected = fibs.next();
            assertEquals( expected, actual, "[" + offset++ + "]" );
        }
        assertTrue( sll.isEmpty() );
    }

    @Test
    void singleThreadRegularSLL() {
        checkSingleThread( new SingleLinkedList<>() );
    }

    @Test
    void singleThreadConcurrentSLL() {
        checkSingleThread( new ConcurrentSingleLinkedList<>() );
    }

    void checkSingleThread( SingleLinkedList<Integer> sll ) {
        String simpleName = sll.getClass().getSimpleName();

        assertTrue( sll.isEmpty() );
        assertEquals( 0, sll.size() );
        assertNull( sll.peek() );
        assertNull( sll.remove() );
        List<Integer> list = sll.toList();
        assertEquals( 0, list.size() );
        assertEquals( sllToString( simpleName ), sll.toString() );

        FibGenerator_1_1 fibs = new FibGenerator_1_1();
        List<Integer> first4 = fibs.next( 4 );
        assertEquals( List.of( 1, 2, 3, 5 ), first4 );

        Integer[] first4AsArray = first4.toArray( new Integer[0] );
        sll.append( first4AsArray );

        assertFalse( sll.isEmpty() );
        assertEquals( 4, sll.size() );
        assertEquals( 1, sll.peek() );
        assertEquals( 1, sll.remove() );
        list = sll.toList();
        assertEquals( 3, list.size() );
        assertEquals( sllToString( simpleName, 2, 3, 5 ), sll.toString() );

        sll.prepend( 0 );
        assertEquals( 4, sll.size() );
        assertEquals( 0, sll.peek() );
        assertEquals( sllToString( simpleName, 0, 2, 3, 5 ), sll.toString() );

        sll.append( fibs.next( 3 ) );
        assertEquals( sllToString( simpleName, 0, 2, 3, 5, 8, 13, 21 ), sll.toString() );
        assertEquals( 7, sll.size() );
        remove( sll, 0, 2, 3 );
        assertEquals( sllToString( simpleName, 5, 8, 13, 21 ), sll.toString() );
        assertEquals( 4, sll.size() );
        remove( sll, 5, 8 );
        assertEquals( sllToString( simpleName, 13, 21 ), sll.toString() );
        assertEquals( 2, sll.size() );
        remove( sll, 13, 21, null );
        assertEquals( sllToString( simpleName ), sll.toString() );
        assertEquals( 0, sll.size() );

        sll.append( 1, 2, 3, 4, 5, null );
        List<Integer> noEffect = sll.removeAllEqual( Arrays.asList( 1, 2, 3, 5, 6, null ) );
        assertEquals( 1, noEffect.size(), noEffect.toString() );
        assertEquals( 6, noEffect.get( 0 ) );
        assertEquals( 1, sll.size() );
        assertEquals( 4, sll.remove() );

        Integer iNull = null; // Following numbers should be outside of the cached range
        Integer i10 = sBoxed( 10 );
        Integer i11 = sBoxed( 11 );
        Integer i12 = sBoxed( 12 );
        Integer i13 = sBoxed( 13 );
        Integer i14 = sBoxed( 14 );
        Integer i15 = sBoxed( 15 );
        int alt11 = sBoxed( 11 );
        sll.append( i10, i11, i12, i13, iNull, i14, null, i15 ); // i11 != 11
        noEffect = sll.removeAllIdentity( Arrays.asList( i13, i12, i10, i14, null, i15, alt11 ) );
        assertEquals( 1, noEffect.size(), noEffect.toString() );
        assertEquals( 11, unBox( noEffect.get( 0 ) ) ); // alt 11 passed in
        assertEquals( 1, sll.size() );
        assertEquals( 11, unBox( sll.remove() ) ); // i11 - no matching identity
    }

    private int unBox( Integer value ) {
        return value - Integer.MIN_VALUE;
    }

    private int sBoxed( int value ) {
        return Integer.MIN_VALUE + value;
    }

    private void remove( SingleLinkedList<Integer> sll, Integer... removed ) {
        for ( int i = 0; i < removed.length; i++ ) {
            Integer expected = removed[i];
            Integer actual = sll.remove();
            assertEquals( expected, actual, "[" + i + "]" );
        }
    }

    private String sllToString( String simpleName, Integer... list ) {
        StringBuilder sb = new StringBuilder( simpleName )
                .append( '(' ).append( list.length ).append( ')' ).append( '[' );
        if ( list.length > 0 ) {
            sb.append( list[0] );
            for ( int i = 1; i < list.length; i++ ) {
                sb.append( ", " ).append( list[i] );
            }
        }
        return sb.append( ']' ).toString();
    }

    private static class FibGenerator_1_1 {
        private int v1 = 0;
        private int v2 = 1;

        public List<Integer> next( int count ) {
            List<Integer> list = new ArrayList<>();
            while ( 0 < count-- ) {
                list.add( next() );
            }
            return list;
        }

        public Integer next() {
            int next = v1 + v2;
            v1 = v2;
            return v2 = next;
        }
    }
}