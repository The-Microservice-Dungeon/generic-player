package thkoeln.dungeon.player.application;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import thkoeln.dungeon.domainprimitives.Moneten;
import thkoeln.dungeon.game.application.GameApplicationService;
import thkoeln.dungeon.game.domain.Game;
import thkoeln.dungeon.game.domain.GameRepository;
import thkoeln.dungeon.player.domain.Player;
import thkoeln.dungeon.player.domain.PlayerMode;
import thkoeln.dungeon.player.domain.PlayerRepository;
import thkoeln.dungeon.restadapter.GameServiceRESTAdapter;
import thkoeln.dungeon.restadapter.PlayerRegistryDto;
import thkoeln.dungeon.restadapter.RESTAdapterException;

import java.util.List;
import java.util.UUID;

/**
 * This game class encapsulates the game tactics for a simple autonomous controlling of a robot
 * swarm. It has the following structure:
 * - the "round started" event triggers the main round() method
 * - if there is enough money, new robots are bought (or, depending on configuration, existing robots are upgraded)
 * - for each robot, the proper command is chosen and issued (based on the configured tactics)
 * - each time an answer is received (with transaction id), the robots and the map are updated.
 */
@Service
public class PlayerApplicationService {
    private Logger logger = LoggerFactory.getLogger(PlayerApplicationService.class);
    private ModelMapper modelMapper = new ModelMapper();
    private Environment env;

    private PlayerRepository playerRepository;
    private GameApplicationService gameApplicationService;
    private GameRepository gameRepository;
    private GameServiceRESTAdapter gameServiceRESTAdapter;

    @Value("${dungeon.playerName}")
    private String playerName;

    @Value("${dungeon.playerEmail}")
    private String playerEmail;

    @Autowired
    public PlayerApplicationService(
            PlayerRepository playerRepository,
            GameApplicationService gameApplicationService,
            GameRepository gameRepository,
            GameServiceRESTAdapter gameServiceRESTAdapter,
            Environment env ) {
        this.playerRepository = playerRepository;
        this.gameServiceRESTAdapter = gameServiceRESTAdapter;
        this.gameRepository = gameRepository;
        this.gameApplicationService = gameApplicationService;
        this.env = env;
    }

    public int numberOfPlayers() {
        int numberOfPlayers = Integer.valueOf( env.getProperty( "dungeon.playerNumber" ) );
        return numberOfPlayers;
    }


    /**
     * Create player(s), if not there already
     */
    public void createPlayers() {
        List<Player> players = playerRepository.findAll();
        int numberOfPlayers = numberOfPlayers();
        if (players.size() == 0) {
            for (int iPlayer = 0; iPlayer < numberOfPlayers; iPlayer++) {
                Player player = new Player();
                if ( (numberOfPlayers == 1) && (! "".equals(playerName) ) && (! "".equals(playerEmail) )  ) {
                    player.setName( playerName );
                    player.setEmail( playerEmail );
                }
                else {
                    player.assignRandomName();
                }
                playerRepository.save(player);
                logger.info("Created new player: " + player);
                players.add(player);
            }
        }
    }


    /**
     * Obtain the bearer token for all players defined in this service
     */
    public void obtainBearerTokensForMultiplePlayers() {
        List<Player> players = playerRepository.findAll();
        for (Player player : players) obtainBearerTokenForPlayer( player );
    }


    /**
     * Obtain the bearer token for one specific player
     * @param player
     * @return true if successful
     */
    public void obtainBearerTokenForPlayer( Player player ) {
        if ( player.getBearerToken() != null ) return;
        try {
            PlayerRegistryDto playerDto = modelMapper.map(player, PlayerRegistryDto.class);
            PlayerRegistryDto registeredPlayerDto = gameServiceRESTAdapter.getBearerTokenForPlayer(playerDto);
            if ( registeredPlayerDto != null ) {
                if ( registeredPlayerDto.getBearerToken() == null ) logger.error( "Received no bearer token for " + player + "!");
                else player.setBearerToken( registeredPlayerDto.getBearerToken() );
                playerRepository.save( player );
                logger.info("Bearer token received for " + player );
            }
            else {
                logger.error( "PlayerRegistryDto returned by REST service is null for player " + player );
            }
        }
        catch ( RESTAdapterException e ) {
            if ( HttpStatus.FORBIDDEN.equals( e.getReturnValue() ) ) {
                // TODO - unclear what to do in this cases
                logger.error("Name collision while getting bearer token for player " + player);
            }
            else {
                logger.error( "No connection or no valid response from GameService - no bearer token for player " + player );
            }
        }
    }



    /**
     * We have received the event that a game has been created. So make sure that the game state is suitable,
     * and that our player(s) can join.
     * for the game.
     * @param gameId
     */
    public void registerPlayersForNewlyCreatedGame( UUID gameId ) {
        Game game = gameApplicationService.gameExternallyCreated( gameId );
        List<Player> players = playerRepository.findAll();
        for (Player player : players) registerOnePlayerForGame( player, game );
    }



    /**
     * Register one specific player for a game
     * @param player
     * @param game
     */
    public void registerOnePlayerForGame( Player player, Game game ) {
        if ( player.getBearerToken() == null ) {
            logger.error( "Player" + player + " has no BearerToken!" );
            return;
        }
        try {
            UUID transactionId = gameServiceRESTAdapter.registerPlayerForGame( game.getGameId(), player.getBearerToken() );
            if ( transactionId != null ) {
                player.registerFor( game, transactionId );
                playerRepository.save( player );
                logger.info( "Player " + player + " successfully registered for game " + game +
                        " with transactionId " + transactionId );
            }
        } catch ( RESTAdapterException e ) {
            // shouldn't happen - cannot do more than logging and retrying later
            // todo - err msg wrong
            logger.error( "Could not register " + player + " for " + game +
                    "\nOriginal Exception:\n" + e.getMessage() + "\n" + e.getStackTrace() );
        }
    }




    /**
     * Method to be called when the answer event after a game registration has been received
     */
    public void assignPlayerId( UUID registrationTransactionId, UUID playerId ) {
        logger.info( "Assign playerId from game registration" );
        if ( registrationTransactionId == null )
            throw new PlayerApplicationException( "registrationTransactionId cannot be null!" );
        if ( playerId == null )  throw new PlayerApplicationException( "PlayerId cannot be null!" );
        List<Player> foundPlayers =
                playerRepository.findByRegistrationTransactionId( registrationTransactionId );
        if ( foundPlayers.size() != 1 ) {
            throw new PlayerApplicationException( "Found not exactly 1 player with transactionId"
                    + registrationTransactionId + ", but " + foundPlayers.size() );
        }
        Player player = foundPlayers.get( 0 );
        player.setPlayerId( playerId );
        playerRepository.save( player );
    }

    /**
     * @param playerId
     * @param moneyAsInt
     */
    public void adjustBankAccount( UUID playerId, Integer moneyAsInt ) {
        Moneten newMoney = Moneten.fromInteger( moneyAsInt );
        List<Player> foundPlayers = playerRepository.findByPlayerId( playerId );
        if ( foundPlayers.size() != 1 ) {
            throw new PlayerApplicationException( "Found not exactly 1 player with playerId " + playerId
                    + ", but " + foundPlayers.size() );
        }
        foundPlayers.get( 0 ).setMoneten( newMoney );
    }
}
