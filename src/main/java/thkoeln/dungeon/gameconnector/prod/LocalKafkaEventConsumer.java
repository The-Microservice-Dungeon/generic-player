package thkoeln.dungeon.gameconnector.prod;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile( "localKafka" )
public class LocalKafkaEventConsumer {

    public void listenToAnswers() {

    }

    public void listenToOtherMoveEvents() {

    }

}