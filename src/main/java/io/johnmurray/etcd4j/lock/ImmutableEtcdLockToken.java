package io.johnmurray.etcd4j.lock;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Generated;

/**
 * Immutable implementation of {@link EtcdLockToken}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableEtcdLockToken.builder()}.
 */
@SuppressWarnings("all")
@Generated({"Immutables.generator", "EtcdLockToken"})
public final class ImmutableEtcdLockToken implements EtcdLockToken {
  private final String name;
  private final Long index;

  private ImmutableEtcdLockToken(String name, Long index) {
    this.name = name;
    this.index = index;
  }

  /**
   * @return The value of the {@code name} attribute
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * @return The value of the {@code index} attribute
   */
  @Override
  public Long getIndex() {
    return index;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link EtcdLockToken#getName() name} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for name
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableEtcdLockToken withName(String value) {
    if (this.name.equals(value)) return this;
    return new ImmutableEtcdLockToken(Objects.requireNonNull(value, "name"), this.index);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link EtcdLockToken#getIndex() index} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for index
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableEtcdLockToken withIndex(Long value) {
    if (this.index.equals(value)) return this;
    return new ImmutableEtcdLockToken(this.name, Objects.requireNonNull(value, "index"));
  }

  /**
   * This instance is equal to all instances of {@code ImmutableEtcdLockToken} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(Object another) {
    if (this == another) return true;
    return another instanceof ImmutableEtcdLockToken
        && equalTo((ImmutableEtcdLockToken) another);
  }

  private boolean equalTo(ImmutableEtcdLockToken another) {
    return name.equals(another.name)
        && index.equals(another.index);
  }

  /**
   * Computes a hash code from attributes: {@code name}, {@code index}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    int h = 31;
    h = h * 17 + name.hashCode();
    h = h * 17 + index.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code EtcdLockToken...} with all non-generated
   * and non-auxiliary attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return "EtcdLockToken{"
        + "name=" + name
        + ", index=" + index
        + "}";
  }

  /**
   * Creates an immutable copy of a {@link EtcdLockToken} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable EtcdLockToken instance
   */
  public static ImmutableEtcdLockToken copyOf(EtcdLockToken instance) {
    if (instance instanceof ImmutableEtcdLockToken) {
      return (ImmutableEtcdLockToken) instance;
    }
    return ImmutableEtcdLockToken.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableEtcdLockToken ImmutableEtcdLockToken}.
   * @return A new ImmutableEtcdLockToken builder
   */
  public static ImmutableEtcdLockToken.Builder builder() {
    return new ImmutableEtcdLockToken.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableEtcdLockToken ImmutableEtcdLockToken}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  public static final class Builder {
    private static final long INIT_BIT_NAME = 0x1L;
    private static final long INIT_BIT_INDEX = 0x2L;
    private long initBits = 0x3;

    private String name;
    private Long index;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code EtcdLockToken} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder from(EtcdLockToken instance) {
      Objects.requireNonNull(instance, "instance");
      name(instance.getName());
      index(instance.getIndex());
      return this;
    }

    /**
     * Initializes the value for the {@link EtcdLockToken#getName() name} attribute.
     * @param name The value for name 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder name(String name) {
      this.name = Objects.requireNonNull(name, "name");
      initBits &= ~INIT_BIT_NAME;
      return this;
    }

    /**
     * Initializes the value for the {@link EtcdLockToken#getIndex() index} attribute.
     * @param index The value for index 
     * @return {@code this} builder for use in a chained invocation
     */
    public final Builder index(Long index) {
      this.index = Objects.requireNonNull(index, "index");
      initBits &= ~INIT_BIT_INDEX;
      return this;
    }

    /**
     * Builds a new {@link ImmutableEtcdLockToken ImmutableEtcdLockToken}.
     * @return An immutable instance of EtcdLockToken
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableEtcdLockToken build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableEtcdLockToken(name, index);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_NAME) != 0) attributes.add("name");
      if ((initBits & INIT_BIT_INDEX) != 0) attributes.add("index");
      return "Cannot build EtcdLockToken, some of required attributes are not set " + attributes;
    }
  }
}
