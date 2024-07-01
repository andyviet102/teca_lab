package vn.teca.digitization.migrate.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum RealmType {
    CITIZEN("CITIZEN"), OFFICER("OFFICER"), FOREIGN("FOREIGN"), UNKNOWN("");

    private final String value;

    RealmType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static RealmType fromValue(String value) {
        return Stream.of(RealmType.values())
                .filter(targetEnum -> targetEnum.value.equalsIgnoreCase(value))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
