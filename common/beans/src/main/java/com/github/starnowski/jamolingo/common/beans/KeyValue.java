package com.github.starnowski.jamolingo.common.beans;

import java.util.Objects;

/**
 * An immutable key-value pair.
 *
 * @param <K> the type of the key
 * @param <V> the type of the value
 */
public final class KeyValue<K, V> {

  private final K key;
  private final V value;

  public KeyValue(K key, V value) {
    this.key = key;
    this.value = value;
  }

  public K getKey() {
    return key;
  }

  public V getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    KeyValue<?, ?> keyValue = (KeyValue<?, ?>) o;
    return Objects.equals(key, keyValue.key) && Objects.equals(value, keyValue.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, value);
  }

  @Override
  public String toString() {
    return "KeyValue{" + "key=" + key + ", value=" + value + '}';
  }

  public static <K, V> Builder<K, V> builder() {
    return new Builder<>();
  }

  public static class Builder<K, V> {
    private K key;
    private V value;

    public Builder<K, V> withKeyValue(KeyValue<K, V> keyValue) {
      this.key = keyValue.key;
      this.value = keyValue.value;
      return this;
    }

    public Builder<K, V> withKey(K key) {
      this.key = key;
      return this;
    }

    public Builder<K, V> withValue(V value) {
      this.value = value;
      return this;
    }

    public KeyValue<K, V> build() {
      return new KeyValue<>(key, value);
    }
  }
}
