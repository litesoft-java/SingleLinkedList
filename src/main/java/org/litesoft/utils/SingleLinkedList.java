package org.litesoft.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
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
     * Remove all entries (this) that (identity) match from the <code>toRemove</code> list.
     *
     * @return Java List (not null) containing all the <code>toRemove</code> entries that did NOT cause a removal.
     */
    public final @NotNull List<T> removeAllIdentity( @Nullable List<T> toRemove ) {
        return process( toRemove, this::lambdaRemoveAllMatching, ( t1, t2 ) -> t1 == t2 );
    }

    /**
     * Remove all entries (this) that (equal) match from the <code>toRemove</code> list.
     *
     * @return Java List (not null) containing all the <code>toRemove</code> entries that did NOT cause a removal.
     */
    public final List<T> removeAllEqual( @Nullable List<T> toRemove ) {
        return process( toRemove, this::lambdaRemoveAllMatching, Objects::equals );
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

    private Node<T> removeHead( Node<T> seizedHead ) {
        if ( 0 == --size ) {
            tail = head = null;
        } else {
            head = seizedHead.next;
        }
        return seizedHead;
    }

    private void removeNodeNext( Node<T> keep ) {
        size--;
        Node<T> newKeepNext = keep.next = keep.next.next;
        if ( newKeepNext == null ) { // no 'next' after 'next'
            tail = keep; // -> 'next' must have been the 'tail'
        }
    }

    private boolean removeAll( T entry, BiPredicate<T, T> matcher ) {
        boolean removed = false;
        while ( (head != null) && matcher.test( entry, head.entry ) ) {
            removeHead( head );
            removed = true;
        }
        for ( Node<T> keep = head; keep != null; keep = keep.next ) {
            for ( Node<T> nn = keep.next; ((nn != null) && matcher.test( entry, nn.entry )); nn = keep.next ) {
                removeNodeNext( keep );
                removed = true;
            }
        }
        return removed;
    }

    private Integer lambdaSize() {
        return size;
    }

    private Node<T> lambdaPeek() {
        return head;
    }

    private Node<T> lambdaRemove() {
        return (0 != size) ? removeHead( head ) : null;
    }

    private List<T> lambdaRemoveAllMatching( List<T> toRemove, BiPredicate<T, T> matcher ) {
        if ( (toRemove == null) || toRemove.isEmpty() ) {
            return List.of();
        }
        if ( 0 == size ) {
            return toRemove;
        }
        ArrayList<T> nonMatched = new ArrayList<>();
        for ( T entry : toRemove ) {
            if ( !removeAll( entry, matcher ) ) {
                nonMatched.add( entry );
            }
        }
        return nonMatched;
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

    /**
     * Vectoring <code>ListProcessor</code>> based Lambda processor to provide the option to wrap the execution.
     *
     * @param toProcess List to process
     * @param processor How to Process the list
     * @param matcher   determines if a toProcess list entry matches an internal list entry.
     * @param <FR>      @see SingleLinkedList
     * @return resulting list of <code>FR</code> that relates the passed list to the internal list.
     */
    protected <FR> List<FR> process( List<FR> toProcess, ListProcessor<FR> processor, BiPredicate<FR, FR> matcher ) {
        return processor.process( toProcess, matcher );
    }

    protected interface ListProcessor<LT> {
        List<LT> process( List<LT> list, BiPredicate<LT, LT> matcher );
    }
}
