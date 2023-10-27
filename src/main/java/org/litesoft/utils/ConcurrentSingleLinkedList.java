package org.litesoft.utils;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A Concurrent Friendly version of the SingleLinkedList.
 *
 * @param <T> type of the Linked List Node Payloads
 */
public class ConcurrentSingleLinkedList<T> extends SingleLinkedList<T> {
    /**
     * Override the Consumer-like Lambda processor to wrap the execution with a <code>synchronized</code>.
     *
     * @param param    @see SingleLinkedList
     * @param consumer @see SingleLinkedList
     * @param <FT>     @see SingleLinkedList
     */
    @Override
    protected <FT> void process( FT param, Consumer<FT> consumer ) {
        synchronized ( this ) {
            super.process( param, consumer );
        }
    }

    /**
     * Override the Supplier-like Lambda processor to wrap the execution with a <code>synchronized</code>.
     *
     * @param supplier @see SingleLinkedList
     * @param <FR>     @see SingleLinkedList
     * @return @see SingleLinkedList
     */
    @Override
    protected <FR> FR process( Supplier<FR> supplier ) {
        synchronized ( this ) {
            return super.process( supplier );
        }
    }

    /**
     * Override the <code>ListProcessor</code>> based Lambda processor wrap the execution with a <code>synchronized</code>.
     *
     * @param toProcess List to process
     * @param processor How to Process the list
     * @param matcher   determines if a toProcess list entry matches an internal list entry.
     * @param <FR>      @see SingleLinkedList
     * @return resulting list of <code>FR</code> that relates the passed list to the internal list.
     */
    @Override
    protected <FR> List<FR> process( List<FR> toProcess, ListProcessor<FR> processor, BiPredicate<FR, FR> matcher ) {
        synchronized ( this ) {
            return super.process( toProcess, processor, matcher );
        }
    }
}
