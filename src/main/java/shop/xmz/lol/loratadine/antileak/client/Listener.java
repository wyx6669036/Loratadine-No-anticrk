package shop.xmz.lol.loratadine.antileak.client;

import shop.xmz.lol.loratadine.antileak.Fucker;
import shop.xmz.lol.loratadine.antileak.packet.impls.send.CPacketKeepAlive;
import shop.xmz.lol.loratadine.antileak.utils.PacketWrapper;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

public class Listener extends Thread implements Wrapper {
    @Override
    public void run() {
        while (true) {
            try {
                /*10 second heartbeat packet*/
                Thread.sleep(10000L);
                if(Client.channel != null && Fucker.userData != null)
                    PacketWrapper.release(new CPacketKeepAlive(Client.channel));
            } catch (Throwable ignored) {}
        }
    }
}