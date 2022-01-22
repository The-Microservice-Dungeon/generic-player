package thkoeln.dungeon.restadapter;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import thkoeln.dungeon.DungeonPlayerConfiguration;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringRunner.class)
@SpringBootTest( classes = DungeonPlayerConfiguration.class )
public class GameServiceRESTAdapterExceptionTest {
    @Value("${GAME_SERVICE:http://localhost:8080}")
    private String gameServiceURIString;
    private ModelMapper modelMapper = new ModelMapper();
    private PlayerRegistryDto playerRegistryDto = new PlayerRegistryDto();

    @Autowired
    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private GameServiceRESTAdapter gameServiceRESTAdapter;


    @Before
    public void setUp() throws Exception {
        playerRegistryDto.setName( "abcd" );

    }


    @Test
    public void testConnectionException_throws_RESTConnectionFailureException() throws Exception {
        // given
        URI uri = new URI( gameServiceURIString + "/games" );
        mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect( ExpectedCount.manyTimes(),
                        requestTo( uri ))
                .andExpect( method( GET ))
                .andRespond( withStatus( HttpStatus.NOT_FOUND ) );

        // when/then
        assertThrows( RESTAdapterException.class, () -> {
            gameServiceRESTAdapter.fetchCurrentGameState();
        });
    }



    @Test
    public void testFetchCurrentGameState_throws_UnexpectedRESTException() throws Exception {
        // given: mock with no body ...
        URI uri = new URI( gameServiceURIString + "/games" );
        mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect( ExpectedCount.manyTimes(),
                        requestTo( uri ))
                .andExpect( method( GET ))
                .andRespond( withStatus( HttpStatus.OK ) );

        // when/then
        assertThrows( RESTAdapterException.class, () -> {
            gameServiceRESTAdapter.fetchCurrentGameState();
        });
    }


    @Test
    public void testGetBearerTokenForPlayer_throws_RESTConnectionFailureException() throws Exception {
        // given
        URI uri = new URI( gameServiceURIString + "/players" );
        mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect( ExpectedCount.manyTimes(), requestTo( uri ))
                .andExpect( method( POST ))
                .andExpect( content().json(mapper.writeValueAsString( playerRegistryDto )) )
                .andRespond( withStatus( HttpStatus.NOT_FOUND ) );

        // when/then
        assertThrows( RESTAdapterException.class, () -> {
            gameServiceRESTAdapter.getBearerTokenForPlayer( playerRegistryDto );
        });
    }



    @Test
    public void testGetBearerTokenForPlayer_throws_HttpClientErrorException() throws Exception {
        // given
        URI uri = new URI( gameServiceURIString + "/players" );
        mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect( ExpectedCount.manyTimes(), requestTo( uri ))
                .andExpect( method( POST ))
                .andExpect( content().json(mapper.writeValueAsString( playerRegistryDto )) )
                .andRespond( withStatus( HttpStatus.NOT_ACCEPTABLE ) );

        // when/then
        assertThrows( RESTAdapterException.class, () -> {
            gameServiceRESTAdapter.getBearerTokenForPlayer( playerRegistryDto );
        });
    }


    @Test
    public void testRegisterPlayerForGame_throws_RESTConnectionFailureException() throws Exception {
        // given
        UUID gameId = UUID.randomUUID();
        UUID playerToken = UUID.randomUUID();
        URI uri = new URI( gameServiceURIString + "/games/" + gameId + "/players/" + playerToken );
        mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect( ExpectedCount.manyTimes(), requestTo( uri ))
                .andExpect( method( PUT ))
                .andRespond( withStatus( HttpStatus.NOT_FOUND ) );

        // when/then
        assertThrows( RESTAdapterException.class, () -> {
            gameServiceRESTAdapter.registerPlayerForGame( gameId, playerToken );
        });
    }




    @Test
    public void testRegisterPlayerForGame_throws_HttpClientErrorException_when_NOT_ACCEPTABLE() throws Exception {
        // given
        UUID gameId = UUID.randomUUID();
        UUID playerToken = UUID.randomUUID();
        URI uri = new URI( gameServiceURIString + "/games/" + gameId + "/players/" + playerToken );
        mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect( ExpectedCount.manyTimes(), requestTo( uri ))
                .andExpect( method( PUT ))
                .andRespond( withStatus( HttpStatus.NOT_ACCEPTABLE ) );

        // when/then
        assertThrows( RESTAdapterException.class, () -> {
            gameServiceRESTAdapter.registerPlayerForGame( gameId, playerToken );
        });
    }


    @Test
    public void testRegisterPlayerForGame_throws_HttpClientErrorException_when_BAD_REQUEST() throws Exception {
        // given
        UUID gameId = UUID.randomUUID();
        UUID playerToken = UUID.randomUUID();
        URI uri = new URI( gameServiceURIString + "/games/" + gameId + "/players/" + playerToken );
        mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect( ExpectedCount.manyTimes(), requestTo( uri ))
                .andExpect( method( PUT ))
                .andRespond( withStatus( HttpStatus.BAD_REQUEST ) );

        // when/then
        assertThrows( RESTAdapterException.class, () -> {
            gameServiceRESTAdapter.registerPlayerForGame( gameId, playerToken );
        });
    }


}
