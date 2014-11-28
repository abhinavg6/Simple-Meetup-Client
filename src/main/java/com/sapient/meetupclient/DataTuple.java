package com.sapient.meetupclient;

/**
 * A simple tuple object, which can be used to map a key to a value
 * 
 * @author abhinavg6
 *
 * @param <L>
 * @param <R>
 */
public class DataTuple<L, R> {

	private final L key;
	private final R value;

	public DataTuple(L key, R value) {
		this.key = key;
		this.value = value;
	}

	public L getKey() {
		return key;
	}

	public R getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		return key.hashCode() ^ value.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof DataTuple))
			return false;
		DataTuple<?, ?> tuple = (DataTuple<?, ?>) o;
		return this.key.equals(tuple.getKey())
				&& this.value.equals(tuple.getValue());
	}

}
