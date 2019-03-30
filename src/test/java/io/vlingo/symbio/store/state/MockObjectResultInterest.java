// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.symbio.store.state;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.common.Outcome;
import io.vlingo.symbio.Metadata;
import io.vlingo.symbio.Source;
import io.vlingo.symbio.store.Result;
import io.vlingo.symbio.store.StorageException;
import io.vlingo.symbio.store.state.StateStore.ConfirmDispatchedResultInterest;
import io.vlingo.symbio.store.state.StateStore.ReadResultInterest;
import io.vlingo.symbio.store.state.StateStore.WriteResultInterest;

public class MockObjectResultInterest
    implements ReadResultInterest,
               WriteResultInterest,
               ConfirmDispatchedResultInterest {

  private AccessSafely access;

  public AtomicInteger confirmDispatchedResultedIn = new AtomicInteger(0);
  public AtomicInteger readObjectResultedIn = new AtomicInteger(0);
  public AtomicInteger writeObjectResultedIn = new AtomicInteger(0);

  public AtomicReference<Result> objectReadResult = new AtomicReference<>();
  public AtomicReference<Result> objectWriteResult = new AtomicReference<>();
  public ConcurrentLinkedQueue<Result> objectWriteAccumulatedResults = new ConcurrentLinkedQueue<>();
  public AtomicReference<Object> objectState = new AtomicReference<>();
  public ConcurrentLinkedQueue<Exception> errorCauses = new ConcurrentLinkedQueue<>();
  public AtomicReference<Metadata> metadataHolder = new AtomicReference<>();

  public MockObjectResultInterest() {
  }

  @Override
  public void confirmDispatchedResultedIn(final Result result, final String dispatchId) {
    confirmDispatchedResultedIn.incrementAndGet();
  }

  @Override
  public <S> void readResultedIn(final Outcome<StorageException, Result> outcome, final String id, final S state, final int stateVersion, final Metadata metadata, final Object object) {
    outcome
      .andThen(result -> {
        access.writeUsing("readStoreData", new StoreData(1, result, state, metadata, null));
        return result;
      })
      .otherwise(cause -> {
        access.writeUsing("readStoreData", new StoreData(1, cause.result, state, metadata, cause));
        return cause.result;
      });
  }

  @Override
  public <S,C> void writeResultedIn(final Outcome<StorageException, Result> outcome, final String id, final S state, final int stateVersion, final List<Source<C>> sources, final Object object) {
    outcome
      .andThen(result -> {
        access.writeUsing("writeStoreData", new StoreData(1, result, state, null, null));
        return result;
      })
      .otherwise(cause -> {
        access.writeUsing("writeStoreData", new StoreData(1, cause.result, state, null, cause));
        return cause.result;
      });
  }


  public AccessSafely afterCompleting(final int times) {
    access = AccessSafely
      .afterCompleting(times)

      .writingWith("confirmDispatchedResultedIn", (Integer increment) -> confirmDispatchedResultedIn.addAndGet(increment))
      .readingWith("confirmDispatchedResultedIn", () -> confirmDispatchedResultedIn.get())

      .writingWith("writeStoreData", (StoreData data) -> {
        writeObjectResultedIn.addAndGet(data.resultedIn);
        objectWriteResult.set(data.result);
        objectWriteAccumulatedResults.add(data.result);
        objectState.set(data.state);
        metadataHolder.set(data.metadata);
        if (data.errorCauses != null) {
          errorCauses.add(data.errorCauses);
        }
      })
      .writingWith("readStoreData", (StoreData data) -> {
        readObjectResultedIn.addAndGet(data.resultedIn);
        objectReadResult.set(data.result);
        objectWriteAccumulatedResults.add(data.result);
        objectState.set(data.state);
        metadataHolder.set(data.metadata);
        if (data.errorCauses != null) {
          errorCauses.add(data.errorCauses);
        }
      })

      .readingWith("readObjectResultedIn", () -> readObjectResultedIn.get())
      .readingWith("objectReadResult", () -> objectReadResult.get())
      .readingWith("objectWriteResult", () -> objectWriteResult.get())
      .readingWith("objectWriteAccumulatedResults", () -> objectWriteAccumulatedResults.poll())
      .readingWith("objectWriteAccumulatedResultsCount", () -> objectWriteAccumulatedResults.size())
      .readingWith("metadataHolder", () -> metadataHolder.get())
      .readingWith("objectState", () -> objectState.get())
      .readingWith("errorCauses", () -> errorCauses.poll())
      .readingWith("errorCausesCount", () -> errorCauses.size())
      .readingWith("writeObjectResultedIn", () -> writeObjectResultedIn.get());

    return access;
  }

  public class StoreData {
    public final Exception errorCauses;
    public final Metadata metadata;
    public final Result result;
    public final Object state;
    public final int resultedIn;

    public StoreData(final int resultedIn, final Result objectResult, final Object state, final Metadata metadata, final Exception errorCauses) {
      this.resultedIn = resultedIn;
      this.result = objectResult;
      this.state = state;
      this.metadata = metadata;
      this.errorCauses = errorCauses;
    }
  }
}
