package thkoeln.dungeon.eventconsumer.core;


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
import thkoeln.dungeon.core.AbstractDungeonMockingTest;
import thkoeln.dungeon.eventconsumer.game.GameEventConsumerService;
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

public class AbstractEventTest extends AbstractDungeonMockingTest {
    @Autowired
    protected PlayerApplicationService playerApplicationService;
    @Autowired
    protected GameApplicationService gameApplicationService;
    @Autowired
    protected GameRepository gameRepository;
    @Autowired
    protected PlayerRepository playerRepository;
    @Autowired
    protected GameEventConsumerService gameEventConsumerService;

    protected List<Player> players;
    protected final UUID bearerToken = UUID.randomUUID();
    protected final UUID gameId = UUID.randomUUID();
    protected final UUID playerId = UUID.randomUUID();
    protected String eventPayloadString;


    @Setter
    @Getter
    @AllArgsConstructor
    protected class GameStatusEventPayloadDto {
        private UUID gameId;
        private GameStatus status;
    }
    protected GameStatusEventPayloadDto gameStatusEventPayloadDto;

    @Setter
    @Getter
    @AllArgsConstructor
    protected class PlayerStatusEventPayloadDto {
        private UUID playerId;
    }
    protected PlayerStatusEventPayloadDto playerStatusEventPayloadDto;

    @Before
    protected void setUp() throws Exception {
        super.setUp();
        playerRepository.deleteAll();
        gameRepository.deleteAll();
        playerApplicationService.createPlayers();
        players = playerRepository.findAll();
        resetMockServer();
        for ( Player player: players ) mockBearerTokenEndpointFor( player );
        playerApplicationService.obtainBearerTokensForMultiplePlayers();
        players = playerRepository.findAll();
        assertEquals( 1, players.size() );
        assertNotNull( players.get( 0 ).getBearerToken() );
    }

}
