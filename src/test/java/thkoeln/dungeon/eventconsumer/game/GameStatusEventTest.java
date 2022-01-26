package thkoeln.dungeon.eventconsumer.game;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import thkoeln.dungeon.DungeonPlayerConfiguration;
import thkoeln.dungeon.eventconsumer.core.AbstractEventTest;
import thkoeln.dungeon.game.domain.Game;
import thkoeln.dungeon.game.domain.GameRepository;
import thkoeln.dungeon.player.domain.Player;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static thkoeln.dungeon.game.domain.GameStatus.CREATED;

@RunWith(SpringRunner.class)
@SpringBootTest( classes = DungeonPlayerConfiguration.class )
public class GameStatusEventTest extends AbstractEventTest {
    @Before
    public void setUp() throws Exception {
        super.setUp();
        gameStatusEventPayloadDto = new GameStatusEventPayloadDto( gameId, CREATED );
    }


    @Test
    public void testGameCreatedEventReceiced() throws Exception {
        // given
        resetMockServer();
        for ( Player player: players ) mockRegistrationEndpointFor( player, gameId );
        eventPayloadString = objectMapper.writeValueAsString( gameStatusEventPayloadDto );

        // when
        gameEventConsumerService.consumeGameStatusEvent(
                genericEventIdStr, genericEventTimestampStr, genericTransactionIdStr, eventPayloadString );

        // then
        assertEquals( 1, gameRepository.findAllByGameStatusEquals( CREATED ).size() );
        Game newGame = gameRepository.findAllByGameStatusEquals( CREATED ).get( 0 );
        assertEquals( gameId, newGame.getGameId() );
        assertEquals( 1, playerRepository.findByRegistrationTransactionId( genericTransactionId ).size() );
        assertEquals( 1, playerRepository.findByCurrentGame( newGame ).size() );
        Player newlyRegisteredPlayer = playerRepository.findByCurrentGame( newGame ).get( 0 );
        assertNotNull( newlyRegisteredPlayer.getBearerToken() );
        assertNull( newlyRegisteredPlayer.getPlayerId() );
    }
}
