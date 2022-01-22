package thkoeln.dungeon.player.domain;

import org.springframework.data.repository.CrudRepository;
import thkoeln.dungeon.game.domain.Game;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerRepository extends CrudRepository<Player, UUID> {
    List<Player> findAll();
    List<Player> findByCurrentGame( Game game );
    List<Player> findByRegistrationTransactionId( UUID transactionId );
}
