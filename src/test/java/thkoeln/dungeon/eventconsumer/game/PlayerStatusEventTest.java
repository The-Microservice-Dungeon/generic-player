package thkoeln.dungeon.eventconsumer.game;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import thkoeln.dungeon.DungeonPlayerConfiguration;
import thkoeln.dungeon.eventconsumer.core.AbstractEventTest;
import thkoeln.dungeon.game.application.GameApplicationService;
import thkoeln.dungeon.game.domain.Game;
import thkoeln.dungeon.game.domain.GameRepository;
import thkoeln.dungeon.player.application.PlayerApplicationService;
import thkoeln.dungeon.player.domain.Player;
import thkoeln.dungeon.player.domain.PlayerRepository;

import java.util.List;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static thkoeln.dungeon.game.domain.GameStatus.CREATED;

@RunWith(SpringRunner.class)
@SpringBootTest( classes = DungeonPlayerConfiguration.class )
public class PlayerStatusEventTest extends AbstractEventTest {
    @Before
    public void setUp() throws Exception {
        super.setUp();
        gameStatusEventPayloadDto = new GameStatusEventPayloadDto( gameId, CREATED );
        playerStatusEventPayloadDto = new PlayerStatusEventPayloadDto( playerId );
    }


    @Test
    public void testGameCreatedEventReceiced() throws Exception {
        // given
        resetMockServer();
        for ( Player player: players ) mockRegistrationEndpointFor( player, gameId );
        eventPayloadString = objectMapper.writeValueAsString( gameStatusEventPayloadDto );
        gameEventConsumerService.consumeGameStatusEvent(
                genericEventIdStr, genericEventTimestampStr, genericTransactionIdStr, eventPayloadString );
        assertEquals( 1, playerRepository.findByRegistrationTransactionId( genericTransactionId ).size() );

        // when
        eventPayloadString = objectMapper.writeValueAsString( playerStatusEventPayloadDto );
        gameEventConsumerService.consumePlayerStatusEvent(
                genericEventIdStr, genericEventTimestampStr, genericTransactionIdStr, eventPayloadString );
        List<Player> foundPlayers = playerRepository.findByPlayerId( playerId );

        // then
        assertEquals( 1, foundPlayers.size() );
        assertEquals( playerId, foundPlayers.get( 0 ).getPlayerId() );
    }

}
