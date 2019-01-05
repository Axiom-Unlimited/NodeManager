package Manager;

import DatagramStructures.NodeStatusPackage;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class Node
{
    private Integer basePort;
    private Integer id;
    private DatagramSocket receiverSocket;
    private InetAddress receiverAddress;
    private Thread receiverThread;

    //Property change listener
    private PropertyChangeSupport support;

    void init(Integer basePort, Integer portOffset, InetAddress ipAddress)
    {
        this.basePort = basePort;
        this.id = portOffset;
        this.receiverAddress = ipAddress;
        support = new PropertyChangeSupport(this);
        try
        {
            this.receiverSocket = new DatagramSocket(basePort + portOffset);
        }
        catch (SocketException e)
        {
            // todo: send message to bit manager
            e.printStackTrace();
        }
        this.receiverThread = new Thread(new MessageReceiverThread());
        this.receiverThread.start();
    }

    void addPropertyChangeListener(PropertyChangeListener pcl)
    {
        this.support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl)
    {
        support.removePropertyChangeListener(pcl);
    }


    private class MessageReceiverThread implements Runnable
    {
        NodeStatusPackage state = new NodeStatusPackage();
        byte[] buff = new byte[state.size()];

        @Override
        public void run()
        {
            while (!receiverSocket.isClosed())
            {
                DatagramPacket datagramPacket = new DatagramPacket(buff, buff.length, receiverAddress, basePort + id);
                try
                {
                    receiverSocket.receive(datagramPacket);
                    state.setByteBuffer(ByteBuffer.wrap(buff), 0);
                    support.firePropertyChange("State Received", null, state);
                }
                catch (IOException e)
                {
                    //todo: send message to bit manager
                    e.printStackTrace();
                }
            }
        }
    }
}
