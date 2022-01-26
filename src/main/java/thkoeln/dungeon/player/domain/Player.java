package thkoeln.dungeon.player.domain;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import thkoeln.dungeon.domainprimitives.Moneten;
import thkoeln.dungeon.game.domain.Game;

import javax.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class Player {
    @Id
    private final UUID id = UUID.randomUUID();

    @Setter
    private String name;
    @Setter
    private String email;
    @Setter
    private UUID bearerToken;
    @Setter
    private UUID playerId;
    @Setter
    @Embedded
    private Moneten moneten = Moneten.fromInteger( 0 );

    private UUID registrationTransactionId;

    @ManyToOne
    private Game currentGame;

    /**
     * Choose a random and unique name and email for the player
     */
    public void assignRandomName() {
        String randomNickname = NameGenerator.generateName();
        setName( randomNickname );
        setEmail( randomNickname + "@microservicedungeon.com" );
    }

    public boolean isReadyToPlay() {
        return ( bearerToken != null && playerId != null && moneten != null );
    }

    public void registerFor ( Game game, UUID registrationTransactionId ) {
        if ( game == null ) throw new PlayerDomainException( "Game must not be null!" );
        if ( registrationTransactionId == null ) throw new PlayerDomainException( "registrationTransactionId must not be null!" );
        this.currentGame = game;
        this.registrationTransactionId = registrationTransactionId;
    }

    public void playRound() {
        // todo
    }

    @Override
    public String toString() {
        return "Player '" + name + "' (bearerToken: " + bearerToken + " playerId: " + playerId + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;
        Player player = (Player) o;
        return Objects.equals(id, player.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
