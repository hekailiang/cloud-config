package org.squirrelframework.cloud.resource.cache;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by kailianghe on 15/12/31.
 */
public class MockUser implements Serializable {

    private final String id;

    private final String name;

    private final int age;

    @JsonCreator
    public MockUser(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("age") int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MockUser)) return false;

        MockUser mockUser = (MockUser) o;
        if (age != mockUser.age) return false;
        if (!id.equals(mockUser.id)) return false;
        return name.equals(mockUser.name);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + age;
        return result;
    }
}
