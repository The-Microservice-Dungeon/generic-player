package thkoeln.dungeon;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import thkoeln.dungeon.game.application.GameApplicationService;
import thkoeln.dungeon.game.domain.GameRepository;
import thkoeln.dungeon.player.application.PlayerApplicationService;

@Service
public class DungeonPlayerStartupService implements ApplicationListener<ContextRefreshedEvent> {

    private Logger logger = LoggerFactory.getLogger( DungeonPlayerStartupService.class );
    private PlayerApplicationService playerApplicationService;

    @Autowired
    public DungeonPlayerStartupService( PlayerApplicationService playerApplicationService ) {
        this.playerApplicationService = playerApplicationService;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        playerApplicationService.createPlayers();
        playerApplicationService.obtainBearerTokensForMultiplePlayers();
    }
}
