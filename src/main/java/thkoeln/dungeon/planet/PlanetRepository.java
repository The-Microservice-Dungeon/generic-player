package thkoeln.dungeon.planet;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface PlanetRepository extends CrudRepository<Planet, UUID> {

}