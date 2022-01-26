package thkoeln.dungeon.game.application;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import thkoeln.dungeon.DungeonPlayerConfiguration;
import thkoeln.dungeon.game.domain.Game;
import thkoeln.dungeon.game.domain.GameRepository;
import thkoeln.dungeon.player.domain.PlayerRepository;

import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static thkoeln.dungeon.game.domain.GameStatus.*;

@RunWith(SpringRunner.class)
@SpringBootTest( classes = DungeonPlayerConfiguration.class )
public class GameLifecycleTest {
    private static final UUID GAME_ID_0 = UUID.randomUUID();
    private static final UUID GAME_ID_1 = UUID.randomUUID();
    private static final UUID GAME_ID_2 = UUID.randomUUID();
    private Game game0, game1, game2;

    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private GameApplicationService gameApplicationService;


    @Before
    public void setUp() throws Exception {
        playerRepository.deleteAll();
        gameRepository.deleteAll();
        game0 = Game.newlyCreatedGame( GAME_ID_0 );
        game1 = Game.newlyCreatedGame( GAME_ID_1 );
        game2 = Game.newlyCreatedGame( GAME_ID_2 );
    }


    @Test
    public void testGameExternallyCreated_OnEmptyDatabase() {
        // given
        // when
        Game game = gameApplicationService.gameExternallyCreated( GAME_ID_0 );

        // then
        assertTrue( gameApplicationService.retrieveRunningGame().isEmpty() );
        assertEquals( 1, gameRepository.findAllByGameStatusEquals( CREATED ).size() );
        assertEquals( 1, gameRepository.findAll().size() );
        assertEquals( GAME_ID_0, gameRepository.findAllByGameStatusEquals( CREATED ).get( 0 ).getGameId() );
        assertEquals( game, gameRepository.findAllByGameStatusEquals( CREATED ).get( 0 ) );
        assertEquals( game, gameApplicationService.findByGameId( GAME_ID_0 ).get() );
    }


    @Test
    public void testGameExternallyStarted_OnEmptyDatabase() {
        // given
        // when
        Game game = gameApplicationService.gameExternallyStarted( GAME_ID_0 );

        // then
        assertEquals( GAME_ID_0, gameApplicationService.retrieveRunningGame().get().getGameId() );
        assertEquals(RUNNING, gameApplicationService.retrieveRunningGame().get().getGameStatus() );
        assertEquals( 1, gameRepository.findAll().size() );
        assertEquals( game, gameRepository.findAllByGameStatusEquals(RUNNING).get( 0 ) );
        assertEquals( game, gameApplicationService.retrieveRunningGame().get() );
        assertEquals( game, gameApplicationService.findByGameId( GAME_ID_0 ).get() );
    }



    @Test
    public void testGameExternallyEnded_OnEmptyDatabase() {
        // given
        // when
        Game game = gameApplicationService.gameExternallyFinished( GAME_ID_0 );

        // then
        assertTrue( gameApplicationService.retrieveRunningGame().isEmpty() );
        assertEquals( 1, gameRepository.findAll().size() );
        assertEquals( 1, gameRepository.findAllByGameStatusEquals(FINISHED).size() );
        assertEquals( game, gameRepository.findAllByGameStatusEquals(FINISHED).get( 0 ) );
        assertEquals( game, gameApplicationService.findByGameId( GAME_ID_0 ).get() );
    }


    @Test
    public void testGameExternallyCreated_OnFilledDatabase() {
        // given
        game1.setGameStatus(RUNNING);
        gameRepository.save( game1 );
        game2.setGameStatus(FINISHED);
        gameRepository.save( game2 );

        // when
        Game game = gameApplicationService.gameExternallyCreated( GAME_ID_0 );

        // then
        assertEquals( game1, gameApplicationService.retrieveRunningGame().get() );
        assertEquals( 1, gameRepository.findAllByGameStatusEquals( CREATED ).size() );
        assertEquals( 3, gameRepository.findAll().size() );
        assertEquals( game, gameRepository.findAllByGameStatusEquals( CREATED ).get( 0 ) );
        assertEquals( game, gameApplicationService.findByGameId( GAME_ID_0 ).get() );
    }


    @Test
    public void testGameExternallyStarted_OnFilledDatabase() {
        // given
        game1.setGameStatus(RUNNING);
        gameRepository.save( game1 );
        game2.setGameStatus(FINISHED);
        gameRepository.save( game2 );

        // when
        Game game = gameApplicationService.gameExternallyStarted( GAME_ID_0 );

        // then
        assertEquals( GAME_ID_0, gameApplicationService.retrieveRunningGame().get().getGameId() );
        assertEquals(RUNNING, gameApplicationService.retrieveRunningGame().get().getGameStatus() );
        assertEquals( 1, gameRepository.findAllByGameStatusEquals(RUNNING).size() );
        assertEquals( 2, gameRepository.findAllByGameStatusEquals(FINISHED).size() );
        assertEquals( 3, gameRepository.findAll().size() );
        assertEquals( game, gameRepository.findAllByGameStatusEquals(RUNNING).get( 0 ) );
        assertEquals( game, gameApplicationService.findByGameId( GAME_ID_0 ).get() );
        assertEquals( game, gameApplicationService.retrieveRunningGame().get() );
    }



    @Test
    public void testGameExternallyEnded_OnFilledDatabase() {
        // given
        game1.setGameStatus(RUNNING);
        gameRepository.save( game1 );
        game2.setGameStatus(FINISHED);
        gameRepository.save( game2 );

        // when
        Game game = gameApplicationService.gameExternallyFinished( GAME_ID_0 );

        // then
        assertEquals( game1, gameApplicationService.retrieveRunningGame().get() );
        assertEquals( 1, gameRepository.findAllByGameStatusEquals(RUNNING).size() );
        assertEquals( 2, gameRepository.findAllByGameStatusEquals(FINISHED).size() );
        assertEquals( 3, gameRepository.findAll().size() );
        assertEquals( game, gameApplicationService.findByGameId( GAME_ID_0 ).get() );
    }


    @Test
    public void testGameExternallyStarted_withLocalStateCollision() {
        // given
        game0.setGameStatus( CREATED );
        gameRepository.save( game0 );
        game1.setGameStatus(RUNNING);
        gameRepository.save( game1 );
        game2.setGameStatus(FINISHED);
        gameRepository.save( game2 );

        // when
        Game game = gameApplicationService.gameExternallyStarted( GAME_ID_2 );

        // then
        assertEquals( GAME_ID_2, gameApplicationService.retrieveRunningGame().get().getGameId() );
        assertEquals( 1, gameRepository.findAllByGameStatusEquals(RUNNING).size() );
        assertEquals( 2, gameRepository.findAllByGameStatusEquals(FINISHED).size() );
        assertEquals( 3, gameRepository.findAll().size() );
        assertEquals( game, gameRepository.findAllByGameStatusEquals(RUNNING).get( 0 ) );
        assertEquals( game, gameApplicationService.findByGameId( GAME_ID_2 ).get() );
        assertEquals( game, gameApplicationService.retrieveRunningGame().get() );
    }
}
