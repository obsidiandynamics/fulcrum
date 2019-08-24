package com.obsidiandynamics.flow;

import java.util.*;

import com.obsidiandynamics.worker.*;

/**
 *  A train of sequentially-ordered elements that may be processed in parallel and in 
 *  arbitrary order, subject to the constraint whereby processing must be completed in 
 *  deterministic order. <p>
 *  
 *  Flows are useful for subdividing an unordered or a partially-ordered stream of events for 
 *  parallel processing, where it is essential that the events are be acknowledged in the
 *  strict order in which they appear in the stream. <p>
 *  
 *  An application choreographs a flow by invoking {@link #begin(Object, Runnable)} in the order
 *  consistent with how elements must be completed. The result is a {@link StatefulConfirmation}
 *  object that will accompany the element during processing. When processing completes, the
 *  application will invoke {@link StatefulConfirmation#confirm()}, signalling to the {@link Flow}
 *  that the element can now be dispatched using the behaviour in the supplied {@link Runnable}
 *  task. <p>
 *  
 *  The actual processing of an element is the responsibility of the calling application, and will
 *  typically take place in a thread pool (for unordered elements) or an actor system (for
 *  partially-ordered elements). {@link Flow} is responsible for carrying out the
 *  terminal dispatch phase, using the {@link Runnable} provided by the caller. <p>
 *  
 *  An element may only be dispatched when <em>all</em> preceding elements have been confirmed.
 *  For efficiency, and where the application permits this, it is possible to lazily skip the 
 *  dispatch of intermediate elements when a contiguous sequence of elements is observed to
 *  have completed. This behaviour is influenced by installing an appropriate {@link FiringStrategy}. 
 *  A {@link LazyFiringStrategy} will not dispatch intermediate elements in a contiguous sequence,
 *  dispatching only the last element in the sequence. Conversely, a {@link StrictFiringStrategy}
 *  will dispatch all elements a contiguous sequence of completed elements. <p>
 *  
 *  Concurrency is supported not just across elements, but also for any given element. The
 *  {@link #begin(Object, Runnable)} method may be called multiple times, implying that an element
 *  must be confirmed an equal number of times before it is deemed to be complete.
 *  
 *  @see StatefulConfirmation
 *  @see FiringStrategy
 */
public interface Flow extends Terminable, Joinable {
  StatefulConfirmation begin(Object id, Runnable onComplete);
  
  Map<Object, StatefulConfirmation> getPendingConfirmations();
}
