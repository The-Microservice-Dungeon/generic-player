package thkoeln.dungeon.eventconsumer.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import thkoeln.dungeon.eventconsumer.core.AbstractEvent;
import thkoeln.dungeon.game.domain.GameStatus;

import javax.persistence.Entity;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor ( access = AccessLevel.PROTECTED )
public class PlayerStatusEvent extends AbstractEvent {
    private UUID playerId;

    public boolean isValid() {
        return ( playerId != null );
    }
}
