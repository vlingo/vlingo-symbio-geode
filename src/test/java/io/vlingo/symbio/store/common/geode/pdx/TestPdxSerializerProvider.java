// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.symbio.store.common.geode.pdx;

import io.vlingo.symbio.Metadata;
import io.vlingo.symbio.MetadataPdxSerializer;
import io.vlingo.symbio.State;
import io.vlingo.symbio.StateSerializer;
import io.vlingo.symbio.store.common.geode.dispatch.GeodeDispatchable;
import io.vlingo.symbio.store.common.geode.dispatch.GeodeDispatchableSerializer;
import io.vlingo.symbio.store.object.geode.GeodeEventJournalEntry;
import io.vlingo.symbio.store.object.geode.GeodeEventJournalEntrySerializer;
import io.vlingo.symbio.store.object.geode.Person;
import io.vlingo.symbio.store.object.geode.PersonSerializer;

public class TestPdxSerializerProvider extends PdxSerializerProviderAdapter {

  public TestPdxSerializerProvider() {
    super();
  }

  @Override
  protected void registerSerializers() {
    registerSerializer(GeodeDispatchable.class, GeodeDispatchableSerializer.class);
    registerSerializer(GeodeEventJournalEntry.class, GeodeEventJournalEntrySerializer.class);
    registerSerializer(Metadata.class, MetadataPdxSerializer.class);
    registerSerializer(Person.class, PersonSerializer.class);
    registerSerializer(State.BinaryState.class, StateSerializer.class);
    registerSerializer(State.ObjectState.class, StateSerializer.class);
    registerSerializer(State.TextState.class, StateSerializer.class);
  }
}