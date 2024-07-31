package CS555.overlay.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class StatisticsCollectorAndDisplay {

    public StatisticsCollectorAndDisplay() {
        this.numSent = new AtomicInteger( 0 );
        this.numReceived = new AtomicInteger( 0 );
        this.numRelayed = new AtomicInteger( 0 );
        this.sumSent = new AtomicLong( 0 );
        this.sumReceived = new AtomicLong( 0 );
    }

    private AtomicInteger numSent;
    private AtomicInteger numReceived;
    private AtomicInteger numRelayed;
    private AtomicLong sumSent;
    private AtomicLong sumReceived;


    public AtomicInteger getNumSent() {
        return this.numSent;
    }

    public AtomicInteger getNumReceived() {
        return this.numReceived;
    }

    public AtomicInteger getNumRelayed() {
        return this.numRelayed;
    }

    public AtomicLong getSumSent() {
        return this.sumSent;
    }

    public AtomicLong getSumReceived() {
        return this.sumReceived;
    }
}
