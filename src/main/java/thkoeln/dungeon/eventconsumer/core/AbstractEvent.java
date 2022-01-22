package thkoeln.dungeon.eventconsumer.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;
import thkoeln.dungeon.eventconsumer.game.GameStatusEvent;
import thkoeln.dungeon.player.application.PlayerApplicationService;

import javax.persistence.Embedded;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor( access = AccessLevel.PROTECTED )
public abstract class AbstractEvent /*<PayloadDtoType extends AbstractEventPayloadDto> */{
    @Id
    @Setter( AccessLevel.NONE )
    protected UUID id = UUID.randomUUID();
    protected UUID eventId;
    protected Long timestamp;
    protected UUID transactionId;

    // TODO obsolete
    //@Embedded
    //private PayloadDtoType payloadDto;

    @Transient
    protected Logger logger = LoggerFactory.getLogger( AbstractEvent.class );

    public static final String TRANSACTION_ID_KEY = "transactionId";


    public <EventType extends AbstractEvent> EventType fillHeader( String eventIdStr, String timestampStr, String transactionIdStr ) {
        try {
            setEventId( UUID.fromString( eventIdStr ) );
        }
        catch ( IllegalArgumentException e ) {
            logger.warn( "Event " + eventId + " at time " + timestamp + " has invalid eventId." );
        }
        try {
            setTimestamp( Long.valueOf( timestampStr ) );
        }
        catch ( IllegalArgumentException e ) {
            logger.warn( "Event " + eventId + " at time " + timestamp + " has invalid timestamp." );
        }
        try {
            setTransactionId( UUID.fromString( transactionIdStr ) );
        }
        catch ( IllegalArgumentException e ) {
            logger.warn( "Event " + eventId + " at time " + timestamp + " doesn't have a valid transactionId " + transactionIdStr );
        }
        return (EventType) this;
    }


    public <EventType extends AbstractEvent> EventType fillWithPayload( String jsonString ) {
        try {
            ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
            return (EventType) objectMapper.readValue( jsonString, this.getClass() );
        }
        catch(JsonProcessingException conversionFailed ) {
            logger.error( "Error converting payload for event with jsonString " + jsonString );
            return (EventType) this;
        }
    }

    public abstract boolean isValid();

}
