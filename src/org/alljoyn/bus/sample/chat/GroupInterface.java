package org.alljoyn.bus.sample.chat;

import java.util.ArrayList;
import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusMethod;

@BusInterface (name = "org.alljoyn.bus.samples.GroupInterface")
public interface GroupInterface {
    
    
    @BusMethod
    public String[] getMem() throws BusException;
    @BusMethod 
    public String[] getUni() throws BusException;
}