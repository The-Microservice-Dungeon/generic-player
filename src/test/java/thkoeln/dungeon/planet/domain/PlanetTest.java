package thkoeln.dungeon.planet.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.jetbrains.annotations.NotNull;

import javax.transaction.Transactional;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class PlanetTest {
    private Planet[][] planetArray= new Planet[3][3];
    private Integer[][] numberOfNeighbours = new Integer[][] {{2, 3, 2}, {3, 4, 3}, {2, 3, 2}};

    @Autowired
    private PlanetRepository planetRepository;

    @BeforeEach
    public void setup() {
        for( int i = 0; i<=2; i++ ) {
            for (int j = 0; j <= 2; j++) {
                planetArray[i][j] = new Planet();
                planetArray[i][j].setName( "p" + String.valueOf( i ) + String.valueOf( j ) );
            }
        }
    }

    @Test
    public void testNeighbouringSetterGetterViaReflection() throws Exception {
        // given
        Planet planet = new Planet();

        // when
        Method getter = planet.neighbouringGetter( CompassDirection.south );
        Method setter = planet.neighbouringSetter( CompassDirection.west );

        // then
        assertEquals( "getSouthNeighbour", getter.getName() );
        assertEquals( "setWestNeighbour", setter.getName() );
    }

    @Test
    public void testEstablishNeighbouringRelationship() {
        // given
        // when
        planetArray[0][1].defineNeighbour( planetArray[1][1], CompassDirection.east );
        planetArray[0][1].defineNeighbour( planetArray[0][2], CompassDirection.north );

        // then
        assertEquals( planetArray[1][1], planetArray[0][1].getEastNeighbour() );
        assertEquals( planetArray[0][1], planetArray[1][1].getWestNeighbour() );
        assertEquals( planetArray[0][2], planetArray[0][1].getNorthNeighbour() );
        assertEquals( planetArray[0][1], planetArray[0][2].getSouthNeighbour() );
    }


    @Test
    public void testPersistMixOfPersistentAndTransient() {
        // given
        planetRepository.save( planetArray[1][1] );

        // when
        planetArray[1][2].defineNeighbour( planetArray[1][1], CompassDirection.west );
        planetRepository.save( planetArray[1][1] );
        planetRepository.save( planetArray[1][2] );
        Planet p11 = planetRepository.findByName( "p11" ).get();
        Planet p12 = planetRepository.findByName( "p12" ).get();

        // then
        List<Planet> persistentPlanets = planetRepository.findAll();
        assertEquals( 2, persistentPlanets.size() );
        assertEquals( p11, p12.getWestNeighbour() );
        assertEquals( p12, p11.getEastNeighbour() );
    }

    @Test
    @Transactional
    public void testSaveAllNeighboursAtOnce() {
        // given
        for( int i = 0; i<=2; i++ ) {
            for (int j = 0; j <= 2; j++) {
                if ( i < 2 ) planetArray[i][j].defineNeighbour( planetArray[i+1][j], CompassDirection.east );
                if ( j < 2 ) planetArray[i][j].defineNeighbour( planetArray[i][j+1], CompassDirection.south );
            }
        }

        // when
        for( int i = 0; i<=2; i++ ) {
            for (int j = 0; j <= 2; j++) {
                planetRepository.save( planetArray[i][j] );
            }
        }

        // then
        for( int i = 0; i<=2; i++ ) {
            for (int j = 0; j <= 2; j++) {
                Planet planet = planetRepository.findById( planetArray[i][j].getId() ).get();
                assertEquals( numberOfNeighbours[i][j], planet.allNeighbours().size() );
            }
        }
    }

}
