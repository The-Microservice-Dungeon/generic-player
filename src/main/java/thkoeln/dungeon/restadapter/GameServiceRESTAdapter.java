package thkoeln.dungeon.restadapter;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import thkoeln.dungeon.DungeonPlayerRuntimeException;

import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;

import static org.springframework.http.HttpMethod.PUT;

/**
 * Adapter for sending Game and Player life cycle calls to GameService
 */
@Component
public class GameServiceRESTAdapter {
    private RestTemplate restTemplate;
    private Logger logger = LoggerFactory.getLogger( GameServiceRESTAdapter.class );
    @Value("${GAME_SERVICE:http://localhost:8080}")
    private String gameServiceUrlString;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public GameServiceRESTAdapter( RestTemplate restTemplate ) {
        this.restTemplate = restTemplate;
    }


    public GameDto[] fetchCurrentGameState() {
        GameDto[] gameDtos = new GameDto[0];
        String urlString = gameServiceUrlString + "/games";
        try {
            gameDtos = restTemplate.getForObject( urlString, GameDto[].class );
            if ( gameDtos == null ) throw new RESTAdapterException( urlString, "Received a null GameDto array - wtf ...?", null );
            logger.info( "Got " + gameDtos.length + " game(s) via REST ...");
            Iterator<GameDto> iterator = Arrays.stream(gameDtos).iterator();
            while ( iterator.hasNext() ) { logger.info( "... " + iterator.next() ); }
        }
        catch ( RestClientException e ) {
            throw new RESTAdapterException( urlString, e.getMessage(), null );
        }
        return gameDtos;
    }



    public PlayerRegistryDto getBearerTokenForPlayer( PlayerRegistryDto playerRegistryDto ) {
        PlayerRegistryDto returnedPlayerRegistryDto = null;
        String urlString = gameServiceUrlString + "/players";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(playerRegistryDto);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType( MediaType.APPLICATION_JSON );
            HttpEntity<String> request = new HttpEntity<String>( json, headers );
            returnedPlayerRegistryDto =
                     restTemplate.postForObject( urlString, request, PlayerRegistryDto.class);
        }
        catch ( JsonProcessingException e ) {
            throw new DungeonPlayerRuntimeException(
                    "Unexpected error converting playerRegistryDto to JSON: " + playerRegistryDto );
        }
        catch ( HttpClientErrorException e ) {
            if ( e.getStatusCode().equals( HttpStatus.FORBIDDEN ) ) {
                // this is a business logic problem - so let the application service handle this
                throw new RESTAdapterException( "Player " + playerRegistryDto + " already registered",
                        urlString, e.getStatusCode() );
            }
            else {
                throw new RESTAdapterException( urlString, e.getMessage(), e.getStatusCode() );
            }
        }
        catch ( RestClientException e ) {
            throw new RESTAdapterException( urlString, e.getMessage(), null );
        }
        logger.info( "Registered player via REST, got bearer token: " + returnedPlayerRegistryDto.getBearerToken() );
        return returnedPlayerRegistryDto;
    }


    /**
     * Register a specific player for a specific game via call to GameService endpoint.
     * Caveat: GameService returns somewhat weird error codes (non-standard).
     * @param gameId of the game
     * @param bearerToken of the player
     * @return transactionId if successful
     */
    public UUID registerPlayerForGame( UUID gameId, UUID bearerToken ) {
        String urlString = gameServiceUrlString + "/games/" + gameId + "/players/" + bearerToken;
        try {
            TransactionIdResponseDto transactionIdResponseDto =
                    restTemplate.execute( urlString, PUT, requestCallback(), responseExtractor() );
            return transactionIdResponseDto.getTransactionId();
        }
        catch ( HttpClientErrorException e ) {
            if ( e.getStatusCode().equals( HttpStatus.NOT_ACCEPTABLE ) ) {
                // this is a business logic problem - so let the application service handle this
                throw new RESTAdapterException( urlString, "Player with bearer token " + bearerToken +
                        " already registered in game with id " + gameId, e.getStatusCode() );
            }
            else if ( e.getStatusCode().equals( HttpStatus.BAD_REQUEST ) ) {
                throw new RESTAdapterException( urlString, "For player with bearer token " + bearerToken +
                        " and game with id " + gameId + " the player registration went wrong; original error msg: "
                        + e.getMessage(), e.getStatusCode() );
            }
            else {
                throw new RESTAdapterException( urlString, e.getMessage(), e.getStatusCode() );
            }
        }
        catch ( RestClientException e ) {
            throw new RESTAdapterException( urlString, e.getMessage(), null );
        }
    }


    /**
     * Adapted from Baeldung example: https://www.baeldung.com/rest-template
     */
    private RequestCallback requestCallback() {
        return clientHttpRequest -> {
            clientHttpRequest.getHeaders().setContentType( MediaType.APPLICATION_JSON );
        };
    }

    private ResponseExtractor<TransactionIdResponseDto> responseExtractor() {
        return response -> {
            return objectMapper.readValue( response.getBody(), TransactionIdResponseDto.class );
        };
    }
}
