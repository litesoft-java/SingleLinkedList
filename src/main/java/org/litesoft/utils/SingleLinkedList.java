package org.litesoft.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.litesoft.annotations.NotNull;
import org.litesoft.annotations.Nullable;

/**
 * A FIFO queue, built on single linked list of nodes, which has the ability to
 * jump the line by <code>prepend</code>ing entries to the front <code>head</code>
 * of the queue.
 *
 * @param <T> data type of the entries in the queue
 */
public class SingleLinkedList<T> {
    /**
     * Number of entries currently in the queue.
     *
     * @return entries count -- GTE 0
     */
    public final int size() {
        return process( this::lambdaSize );
    }

    /**
     * Peek (read but do not consume) the 'head' of the queue.
     * <p>
     * Note: Since nulls are allowed in the queue, a null means EITHER a null entry OR an empty queue!
     *
     * @return 'head' of the queue or null (if the queue was empty)
     */
    public final @Nullable T peek() {
        Node<T> node = process( this::lambdaPeek );
        return (node == null) ? null : node.entry;
    }

    /**
     * Remove (read AND consume) the 'head' of the queue.
     * <p>
     * Note: Since nulls are allowed in the queue, a null means EITHER a null entry OR an empty queue!
     *
     * @return 'head' of the queue or null (if the queue was empty)
     */
    public final @Nullable T remove() {
        Node<T> node = process( this::lambdaRemove );
        return (node == null) ? null : node.entry;
    }

    /**
     * Prepend <code>entry</code> to the 'head' of the queue -- make the new entry the new 'head'.
     *
     * @param entry to Prepend -- null OK
     */
    public final void prepend( @Nullable T entry ) {
        process( entry, this::lambdaPrepend );
    }

    /**
     * Append <code>entries</code> to the 'tail' of the queue.
     *
     * @param entries Java List of entries to add (null entries OK)
     */
    public final void append( @Nullable List<T> entries ) {
        if ( (entries != null) && !entries.isEmpty() ) { // Left to Right!
            process( entries, this::lambdaAppend );
        }
    }

    /**
     * Get all the current Entries as a Java List.
     *
     * @return Java List containing all the current Entries (not null), make be an empty List!
     */
    public final @NotNull List<T> toList() {
        return process( this::lambdaToList );
    }

    @Override
    public String toString() {
        List<T> list = toList();
        return getClass().getSimpleName() + "(" + list.size() + ")" + list;
    }

    /**
     * Checks if list (queue) is empty.
     *
     * @return true if queue Is Empty; otherwise false
     */
    public final boolean isEmpty() {
        return (0 == size());
    }

    /**
     * Append <code>entries</code> to the 'tail' of the queue.
     *
     * @param entries varArg array of entries to add (null entries OK)
     */
    @SafeVarargs
    public final void append( @Nullable T... entries ) {
        if ( (entries != null) && (entries.length > 0) ) { // Left to Right!
            append( Arrays.asList( entries ) );
        }
    }

    private Node<T> head; // defaults to null
    private Node<T> tail; // defaults to null
    private int size; // defaults to zero

    private Integer lambdaSize() {
        return size;
    }

    private Node<T> lambdaPeek() {
        return head;
    }

    private Node<T> lambdaRemove() {
        if ( 0 == size ) {
            return null;
        }
        Node<T> seizedHead = head;
        if ( 0 == --size ) {
            tail = head = null;
        } else {
            head = seizedHead.next;
        }
        return seizedHead;
    }

    private void lambdaPrepend( T entry ) {
        if ( 0 == size++ ) {
            tail = head = new Node<>( entry );
        } else {
            head = new Node<>( entry, head );
        }
    }

    private void lambdaAppend( List<T> entries ) {
        if ( (entries == null) || entries.isEmpty() ) { // Left to Right!
            return;
        }
        int from = 0;
        if ( size == 0 ) {
            tail = head = new Node<>( entries.get( from++ ) );
            size++;
        }
        while ( from < entries.size() ) {
            tail = tail.append( entries.get( from++ ) );
            size++;
        }
    }

    private List<T> lambdaToList() {
        ArrayList<T> list = new ArrayList<>( size );
        if ( size > 0 ) {
            head.populate( list );
        }
        return list;
    }

    private static class Node<T> {
        private final T entry;
        private Node<T> next;

        public Node( T entry, Node<T> next ) {
            this.entry = entry;
            this.next = next;
        }

        public Node( T entry ) {
            this( entry, null );
        }

        public Node<T> append( T entry ) {
            Node<T> newNode = new Node<>( entry );
            next = newNode;
            return newNode;
        }

        public void populate( ArrayList<T> list ) {
            list.add( entry );
            if ( next != null ) {
                next.populate( list );
            }
        }
    }

    /**
     * Vectoring Consumer-like Lambda processor to provide the option to wrap the execution.
     *
     * @param param    Data to write/update
     * @param consumer Writing (Update) process
     * @param <FT>     type of data
     */
    protected <FT> void process( FT param, Consumer<FT> consumer ) {
        consumer.accept( param );
    }

    /**
     * Vectoring Supplier-like Lambda processor to provide the option to wrap the execution.
     *
     * @param supplier Reading Process
     * @param <FR>     type of data
     * @return Read data
     */
    protected <FR> FR process( Supplier<FR> supplier ) {
        return supplier.get();
    }
}
