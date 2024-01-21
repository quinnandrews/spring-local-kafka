package io.github.quinnandrews.spring.local.kafka.application.data.guitarpedals;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Table(name = "guitar_pedal")
@Entity
public class GuitarPedal {

    @Id
    @Column(name = "id",
            columnDefinition = "BIGINT",
            nullable = false,
            updatable = false)
    private Long id;

    @Column(name = "name",
            columnDefinition = "VARCHAR(63)",
            nullable = false)
    private String name;

    public GuitarPedal() {
        // no-op
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public GuitarPedal withId(final Long id) {
        this.id = id;
        return this;
    }

    public GuitarPedal withName(final String name) {
        this.name = name;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final GuitarPedal that = (GuitarPedal) o;
        return new EqualsBuilder()
                .append(id, that.id)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .toHashCode();
    }
}
