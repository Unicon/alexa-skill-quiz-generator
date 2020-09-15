package org.unicon.lex.services.external;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class KinesisEvent {

    @Getter @Setter String eventType;
    @Getter @Setter Date eventTimestamp;
    @Getter @Setter Object before;
    @Getter @Setter Object after;
}
