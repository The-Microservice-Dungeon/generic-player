package thkoeln.dungeon.eventconsumer.game;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import thkoeln.dungeon.DungeonPlayerConfiguration;
import thkoeln.dungeon.core.AbstractRESTEndpointMockingTest;
import thkoeln.dungeon.game.application.GameApplicationService;
import thkoeln.dungeon.game.domain.GameRepository;
import thkoeln.dungeon.game.domain.GameStatus;
import thkoeln.dungeon.player.application.PlayerApplicationService;
import thkoeln.dungeon.player.domain.Player;
import thkoeln.dungeon.player.domain.PlayerRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static thkoeln.dungeon.game.domain.GameStatus.CREATED;

@RunWith(SpringRunner.class)
@SpringBootTest( classes = DungeonPlayerConfiguration.class )
public class GameServiceStatusChangedEventTest extends AbstractRESTEndpointMockingTest {
    @Autowired
    private PlayerApplicationService playerApplicationService;
    @Autowired
    private GameApplicationService gameApplicationService;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private GameEventConsumerService gameEventConsumerService;

    private List<Player> players;
    private UUID bearerToken = UUID.randomUUID();
    private UUID gameId = UUID.randomUUID();
    private UUID transactionId = UUID.randomUUID();
    private String eventPayloadString;
    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Setter
    @Getter
    @AllArgsConstructor
    private class TestPayloadDto {
        @JsonProperty("gameId")
        private UUID gameId;
        @JsonProperty("status")
        private GameStatus status;
    }
    private TestPayloadDto createdEventPayload;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        playerRepository.deleteAll();
        gameRepository.deleteAll();
        playerApplicationService.createPlayers();

        players = playerRepository.findAll();
        for ( Player player: players ) mockBearerTokenEndpointFor( player );
        for ( Player player: players ) {
            playerApplicationService.obtainBearerTokenForPlayer( player );
            assertNotNull( player.getBearerToken() );
            playerRepository.save( player );
        }
        createdEventPayload = new TestPayloadDto( gameId, CREATED );
    }

    @Test
    public void testGameCreatedEventReceiced() throws Exception {
        // given
        resetMockServer();
        for ( Player player: players ) mockRegistrationEndpointFor( player, gameId );
        eventPayloadString = objectMapper.writeValueAsString( createdEventPayload );

        // when
        gameEventConsumerService.consumeGameStatusEvent(
                String.valueOf( UUID.randomUUID() ), "99999L", String.valueOf( UUID.randomUUID() ),
                eventPayloadString );

        // then
        assertEquals( 1, gameRepository.findAll().size() );
        assertEquals( gameId, gameRepository.findAllByGameStatusEquals( CREATED ).get( 0 ).getGameId() );
        for ( Player player: players ) {
            player.setBearerToken( bearerToken );
            assertNotNull( player.getBearerToken() );
        }
    }
}
