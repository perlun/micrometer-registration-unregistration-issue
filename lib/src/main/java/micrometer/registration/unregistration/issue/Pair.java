package micrometer.registration.unregistration.issue;

import java.io.Serializable;
import java.util.Map;

public class Pair<A, B> implements Serializable, Map.Entry<A, B> {
    private static final long serialVersionUID = 1L;

    /** the first value of this pair */
    @SuppressWarnings( "serial" ) // Silence JDK 18+ deprecation (JDK-8274336)
    private final A value1;

    /** the second value of this pair */
    @SuppressWarnings( "serial" ) // Silence JDK 18+ deprecation (JDK-8274336)
    private final B value2;

    /**
     * Creates an instance with the specified values.
     *
     * @param value1 the value of the first object of the pair
     * @param value2 the value of the second object of the pair
     */
    public Pair( A value1, B value2 ) {
        this.value1 = value1;
        this.value2 = value2;
    }

    /**
     * Creates an instance with the specified values.
     *
     * @param a the value of the first object of the pair
     * @param b the value of the second object of the pair
     * @return the created pair
     */
    public static <A, B> Pair<A, B> of( A a, B b ) {
        return new Pair<>( a, b );
    }

    /**
     * @return the first value of the pair
     */
    public A getFirst() {
        return value1;
    }

    /**
     * @return the second value of the pair
     */
    public B getSecond() {
        return value2;
    }

    /**
     * @return {@link #getFirst()}
     */
    @Override
    public A getKey() {
        return getFirst();
    }

    /**
     * @return {@link #getSecond()}
     */
    @Override
    public B getValue() {
        return getSecond();
    }

    /**
     * @throws UnsupportedOperationException always.
     */
    @Override
    public B setValue( B value ) {
        immutable();
        return null;
    }

    /**
     * @throws UnsupportedOperationException always.
     */
    private static void immutable() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return a readable value of this pair, suitable for debugging
     */
    @Override
    public String toString() {
        return "Pair[first=" + getFirst() + ",second=" + getSecond() + ']';
    }

    @Override
    public boolean equals( Object other ) {
        if ( other == null || other.getClass() != getClass() ) {
            return false;
        }

        Pair<?, ?> o = (Pair<?, ?>)other;

        Object a1 = getFirst();
        Object a2 = o.getFirst();
        Object b1 = getSecond();
        Object b2 = o.getSecond();

        return ( a1 == null ? a2 == null : a1.equals( a2 ) ) && ( b1 == null ? b2 == null : b1.equals( b2 ) );
    }

    @Override
    public int hashCode() {
        Object a = getFirst();
        Object b = getSecond();
        return ( a == null ? 0 : a.hashCode() ) + ( b == null ? 0 : ( b.hashCode() << 2 ) );
    }
}
