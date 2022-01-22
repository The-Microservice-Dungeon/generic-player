package thkoeln.dungeon.player.application;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;
import thkoeln.dungeon.DungeonPlayerConfiguration;
import thkoeln.dungeon.core.AbstractRESTEndpointMockingTest;
import thkoeln.dungeon.game.domain.Game;
import thkoeln.dungeon.game.domain.GameRepository;
import thkoeln.dungeon.player.domain.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest( classes = DungeonPlayerConfiguration.class )
public class PlayerGameRegistrationTest extends AbstractRESTEndpointMockingTest {
    private Player player, playerWithoutToken;
    private UUID playerId = UUID.randomUUID();
    @Autowired
    private Environment env;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private PlayerApplicationService playerApplicationService;


    @Before
    public void setUp() throws Exception {
        super.setUp();
        playerRepository.deleteAll();
        gameRepository.deleteAll();
        game = Game.newlyCreatedGame( UUID.randomUUID() );
        gameRepository.save( game );
        player = new Player();
        player.setName( "playerWith" );
        player.setEmail( "xxx@x.de" );
        playerWithoutToken = new Player();
        playerWithoutToken.setName( "playerWithout" );
        playerWithoutToken.setEmail( "yyy@x.de" );
        playerRepository.save( player );
        playerRepository.save( playerWithoutToken );
    }


    @Test
    public void testRegisterPlayerWithToken() throws Exception {
        // given
        mockBearerTokenEndpointFor( player );
        playerApplicationService.obtainBearerTokenForPlayer( player );
        assert ( player.getBearerToken() != null );
        super.resetMockServer();
        mockBearerTokenEndpointFor( player );
        mockRegistrationEndpointFor( player, game.getGameId() );

        // when
        playerApplicationService.joinPlayersInNewlyCreatedGame( game.getGameId() );
        // ... and we assume that the playerStatus event comes after that
        playerApplicationService.assignPlayerId( transactionId, playerId );

        // then
        List<Player> readyPlayers = playerRepository.findByCurrentGame( game );
        assertEquals( 1, readyPlayers.size() );
        assertEquals( player, readyPlayers.get( 0 ) );
        assertEquals( game, readyPlayers.get( 0 ).getCurrentGame() );
        assertTrue( readyPlayers.get( 0 ).isReadyToPlay() );
    }


}
