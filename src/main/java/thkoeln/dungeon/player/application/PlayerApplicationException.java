package thkoeln.dungeon.player.application;

import thkoeln.dungeon.DungeonPlayerRuntimeException;

public class PlayerApplicationException extends DungeonPlayerRuntimeException {
    public PlayerApplicationException(String message ) {
        super( message );
    }
}
