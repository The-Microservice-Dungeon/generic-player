package thkoeln.dungeon.game.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Game {
    @Id
    private final UUID id = UUID.randomUUID();

    // this is the EXTERNAL id that we receive from GameService. We could use this also as our own id, but then
    // we'll run into problems in case GameService messes up their ids (e.g. start the same game twice, etc.) So,
    // we better keep these two apart.
    private UUID gameId;
    private GameStatus gameStatus;
    private Integer currentRoundCount;

    @Transient
    private Logger logger = LoggerFactory.getLogger( Game.class );

    public void resetToNewlyCreated() {
        setGameStatus( GameStatus.CREATED );
        setCurrentRoundCount( 0 );
        logger.warn( "Reset game " + this + " to CREATED!" );
    }

    public void makeOrphan() {
        setGameStatus( GameStatus.ORPHANED );
        logger.warn( "Marked game " + this + " as ORPHANED!" );
    }

    public static Game newlyCreatedGame( UUID gameId ) {
        Game game = new Game();
        game.setGameId( gameId );
        game.resetToNewlyCreated();
        return game;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return id.equals(game.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Game (" + getGameStatus() + ", " + getGameId() + ")";
    }
}
