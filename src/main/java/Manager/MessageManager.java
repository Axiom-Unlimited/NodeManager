package Manager;

import DatagramStructures.BroadcastPackage;
import org.json.JSONObject;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MessageManager
{
    private DatagramSocket broadcastSocket;
    private List<Node> nodes;
    private BroadcastPackage currentBroadcast;
    private Thread messageBroadcastThread;
    private PropertyChangeListener changeListener;

    private JSONObject settings;

    public void init(Integer numOfNodes, JSONObject settings, PropertyChangeListener ref)
    {
        this.currentBroadcast = new BroadcastPackage();
        this.currentBroadcast.setByteBuffer(ByteBuffer.wrap(new byte[this.currentBroadcast.size()]),0);
        this.changeListener = ref;
        this.settings = settings;
        this.nodes = new ArrayList<>(numOfNodes);


        try
        {
            InetAddress ipAddress = InetAddress.getByName((String)settings.get("ipaddress"));

            for (int i = 0; i < numOfNodes; i++)
            {
                Node node = new Node();
                node.init((Integer) settings.get("baseport"),i,ipAddress);
                node.addPropertyChangeListener(this.changeListener);
                this.nodes.add(node);
            }

            this.broadcastSocket = new DatagramSocket();
            this.broadcastSocket.setBroadcast(true);

            this.messageBroadcastThread = new Thread(new MessageBroadcastThread());
            this.messageBroadcastThread.start();
        }
        catch (SocketException | UnknownHostException e)
        {
            //todo: send message to the bit manager
            e.printStackTrace();
        }
    }

    public synchronized void broadcast(BroadcastPackage broadcast)
    {
        this.currentBroadcast = broadcast;
    }

    private class MessageBroadcastThread implements Runnable
    {
        InetAddress broadcastAddress;

        {
            try
            {
                broadcastAddress = InetAddress.getByName((String) settings.get("ipaddress"));
            }
            catch (UnknownHostException e)
            {
                //todo: alert the bitmanager
                e.printStackTrace();
            }
        }

        @Override
        public void run()
        {
            while (!broadcastSocket.isClosed())
            {
                try
                {
                    DatagramPacket packet = new DatagramPacket(currentBroadcast.getByteBuffer().array()
                            , currentBroadcast.getByteBuffer().array().length, broadcastAddress, (int) settings.get("broadcastPort"));

                    broadcastSocket.send(packet);
//                    System.out.println("sending message!");
                }
                catch (IOException e)
                {
                    //todo: send message to the bit manager
                    e.printStackTrace();
                }
            }
        }
    }

}
