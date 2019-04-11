// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.symbio.store.state;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.symbio.Entry;
import io.vlingo.symbio.State;
import io.vlingo.symbio.store.state.StateStore.ConfirmDispatchedResultInterest;
import io.vlingo.symbio.store.state.StateStore.Dispatcher;
import io.vlingo.symbio.store.state.StateStore.DispatcherControl;

public class MockObjectDispatcher implements Dispatcher {
  private AccessSafely access;

  public final ConfirmDispatchedResultInterest confirmDispatchedResultInterest;
  public DispatcherControl control;
  public final Map<String,Object> dispatched = new HashMap<>();
  private final ConcurrentLinkedQueue<Entry<?>> dispatchedEntries = new ConcurrentLinkedQueue<>();
  public final AtomicBoolean processDispatch = new AtomicBoolean(true);
  public final AtomicInteger dispatchAttemptCount = new AtomicInteger(0);

  public MockObjectDispatcher(final ConfirmDispatchedResultInterest confirmDispatchedResultInterest) {
    this.confirmDispatchedResultInterest = confirmDispatchedResultInterest;
    this.access = AccessSafely.afterCompleting(0);
  }

  @Override
  public void controlWith(final DispatcherControl control) {
    this.control = control;
  }

  @Override
  public <S extends State<?>, E extends Entry<?>> void dispatch(final String dispatchId, final S state, final Collection<E> entries) {
    dispatchAttemptCount.getAndIncrement();
    if (processDispatch.get()) {
      access.writeUsing("dispatched", dispatchId, new Dispatch<>(state, entries));
      control.confirmDispatched(dispatchId, confirmDispatchedResultInterest);
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public AccessSafely afterCompleting(final int times) {
    access = AccessSafely
      .afterCompleting(times)

      .writingWith("dispatched", (String id, Dispatch dispatch) -> { dispatched.put(id, dispatch.state); dispatchedEntries.addAll(dispatch.entries); })

      .readingWith("dispatchedState", (String id) -> dispatched.get(id))
      .readingWith("dispatchedStateCount", () -> dispatched.size())

      .writingWith("processDispatch", (Boolean flag) -> processDispatch.set(flag))
      .readingWith("processDispatch", () -> processDispatch.get())

      .readingWith("dispatchAttemptCount", () -> dispatchAttemptCount.get())

      .readingWith("dispatched", () -> dispatched);

    return access;
  }

  public void dispatchUnconfirmed() {
    control.dispatchUnconfirmed();
  }

  private static class Dispatch<S extends State<?>,E extends Entry<?>> {
    final Collection<E> entries;
    final S state;

    Dispatch(final S state, final Collection<E> entries) {
      this.state = state;
      this.entries = entries;
    }
  }
}
