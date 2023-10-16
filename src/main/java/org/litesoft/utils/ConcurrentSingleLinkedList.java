package org.litesoft.utils;

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
     * @param param @see SingleLinkedList
     * @param consumer @see SingleLinkedList
     * @param <FT> @see SingleLinkedList
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
     * @return @see SingleLinkedList
     * @param <FR> @see SingleLinkedList
     */
    @Override
    protected <FR> FR process( Supplier<FR> supplier ) {
        synchronized ( this ) {
            return super.process( supplier );
        }
    }
}
