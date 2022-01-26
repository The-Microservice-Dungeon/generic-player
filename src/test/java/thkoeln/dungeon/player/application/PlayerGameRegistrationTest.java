package thkoeln.dungeon.player.application;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;
import thkoeln.dungeon.DungeonPlayerConfiguration;
import thkoeln.dungeon.core.AbstractDungeonMockingTest;
import thkoeln.dungeon.game.domain.Game;
import thkoeln.dungeon.game.domain.GameRepository;
import thkoeln.dungeon.player.domain.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest( classes = DungeonPlayerConfiguration.class )
public class PlayerGameRegistrationTest extends AbstractDungeonMockingTest {
    private Player player, playerWithoutToken;
    private UUID playerId = UUID.randomUUID();
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
        playerApplicationService.registerPlayersForNewlyCreatedGame( game.getGameId() );

        // then
        List<Player> playersForGame = playerRepository.findByCurrentGame( game );
        assertEquals( 1, playersForGame.size() );
        assertEquals( player, playersForGame.get( 0 ) );
        assertEquals( game, playersForGame.get( 0 ).getCurrentGame() );
        assertEquals( genericTransactionId, playersForGame.get( 0 ).getRegistrationTransactionId() );
    }




    @Test
    public void testAssignPlayerId() throws Exception {
        // given
        mockBearerTokenEndpointFor( player );
        playerApplicationService.obtainBearerTokenForPlayer( player );
        assert ( player.getBearerToken() != null );
        super.resetMockServer();
        mockBearerTokenEndpointFor( player );
        mockRegistrationEndpointFor( player, game.getGameId() );
        playerApplicationService.registerPlayersForNewlyCreatedGame( game.getGameId() );

        // when
        playerApplicationService.assignPlayerId(genericTransactionId, playerId );

        // then
        List<Player> readyPlayers = playerRepository.findByCurrentGame( game );
        assertEquals( 1, readyPlayers.size() );
        assertTrue( readyPlayers.get( 0 ).isReadyToPlay() );
    }


}
