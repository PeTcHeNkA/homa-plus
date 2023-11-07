package plus.apiutils;

import java.util.Objects;
import java.util.UUID;


public class GameProfile {
    private final UUID id;
    private final String name;

    private boolean legacy;

    public GameProfile(UUID id, String name) {
        if (id == null && name.isBlank())
            throw new IllegalArgumentException("Name and ID cannot both be blank");
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

//    public PropertyMap getProperties() {
//        return this.properties;
//    }

    public boolean isComplete() {
        return (this.id != null && !name.isBlank());
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GameProfile that = (GameProfile)o;
        if (!Objects.equals(this.id, that.id))
            return false;
        return Objects.equals(this.name, that.name);
    }

    public int hashCode() {
        int result = (this.id != null) ? this.id.hashCode() : 0;
        result = 31 * result + ((this.name != null) ? this.name.hashCode() : 0);
        return result;
    }

//    public String toString() {
//        TsonMap map = new TsonMap();
//    }

    public boolean isLegacy() {
        return this.legacy;
    }
}