package thkoeln.dungeon.domainprimitives;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import thkoeln.dungeon.restadapter.RESTAdapterException;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MonetenTest {
    private Moneten m27_1, m27_2, m28;

    @BeforeEach
    public void setUp() {
        m27_1 = Moneten.fromInteger( 27 );
        m27_2 = Moneten.fromInteger( 27 );
        m28 = Moneten.fromInteger( 28 );
    }

    @Test
    public void testTwoMonetenEqualAndUnequal() {
        assertEquals( m27_1, m27_2 );
        assertNotEquals( m27_1, m28 );
    }

    @Test
    public void testValidation() {
        assertThrows( MonetenException.class, () -> {
            Moneten.fromInteger( null );
        });
        assertThrows( MonetenException.class, () -> {
            Moneten.fromInteger( -1 );
        });
    }

}
