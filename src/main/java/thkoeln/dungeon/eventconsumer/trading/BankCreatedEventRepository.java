package thkoeln.dungeon.eventconsumer.trading;

import org.springframework.data.repository.CrudRepository;
import thkoeln.dungeon.eventconsumer.game.PlayerStatusEvent;

import java.util.UUID;

public interface BankCreatedEventRepository extends CrudRepository<BankCreatedEvent, UUID> {

}
