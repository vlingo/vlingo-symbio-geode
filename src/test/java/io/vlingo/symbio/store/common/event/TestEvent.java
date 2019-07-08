// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.symbio.store.common.event;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class TestEvent extends Event {
    public final String id;
    public final long number;

    public TestEvent(String id, long number) {
        this.id = id;
        this.number = number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestEvent that = (TestEvent) o;
        return number == that.number &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, number);
    }

    @Override
    public String toString() {
        return "TestEvent{" +
                "id='" + id + '\'' +
                ", number=" + number +
                '}';
    }

    public static TestEvent randomEvent(){
        return new TestEvent(UUID.randomUUID().toString(), new Random().nextInt());
    }
}
