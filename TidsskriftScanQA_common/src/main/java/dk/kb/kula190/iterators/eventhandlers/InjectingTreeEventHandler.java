package dk.kb.kula190.iterators.eventhandlers;


import dk.kb.kula190.ResultCollector;
import dk.kb.kula190.iterators.common.AttributeParsingEvent;
import dk.kb.kula190.iterators.common.InjectedAttributeParsingEvent;
import dk.kb.kula190.iterators.common.ParsingEvent;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InjectingTreeEventHandler extends DefaultTreeEventHandler {
    
    private static final ThreadLocal<Queue<AttributeParsingEvent>>
            eventQueue
            = new ThreadLocal<Queue<AttributeParsingEvent>>() {
        @Override
        protected Queue<AttributeParsingEvent> initialValue() {
            return new ConcurrentLinkedQueue<>();
        }
    };
    
    public InjectingTreeEventHandler(ResultCollector resultCollector) {
        super(resultCollector);
    }
    
    
    public final void pushEvent(ParsingEvent event, String type, byte[] data) {
        eventQueue.get().add(new InjectedAttributeParsingEvent(event.getName() + ".injected", type, event.getLocation(), data,
                                                               DigestUtils.md5Hex(data)));
    }
    
    public final AttributeParsingEvent popEvent() {
        return this.eventQueue.get().poll();
    }
}
