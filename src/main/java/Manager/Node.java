package Manager;

import DataStructures.*;
import javafx.concurrent.Task;
import javolution.io.Struct;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Node extends Task
{

    public Integer nodeId;
    private Queue<Struct> commandQueue;
    public Queue<StatusReport> reports;

    // tcp socket member fields
    private String ipAddress;
    private Integer port;
    private ServerSocket serverSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    //Property change listener
    private PropertyChangeSupport support;


    public Node(String ipAddress,Integer basePort, Integer portAdjuster) throws IOException
    {
        this.ipAddress      = ipAddress;
        this.port           = basePort + portAdjuster;
        this.nodeId         = portAdjuster;
        this.commandQueue   = new ConcurrentLinkedQueue<>();
        this.reports        = new ConcurrentLinkedQueue<>();
        this.serverSocket   = new ServerSocket(this.port, 1, InetAddress.getByName(ipAddress));
        this.support        = new PropertyChangeSupport(this);

    }

    public Node(Integer port) throws IOException
    {
        this.port       = port;
        commandQueue    = new ConcurrentLinkedQueue<>();
        reports         = new ConcurrentLinkedQueue<>();
        serverSocket    = new ServerSocket(this.port);
    }

    public Boolean sendCommand(Struct command)
    {
        this.commandQueue.add(command);
        return this.commandQueue.isEmpty();
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl)
    {
        this.support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl)
    {
        support.removePropertyChangeListener(pcl);
    }

    @Override
    public Object call()
    {
        //set up connection stuff
        Socket connectionSocket = null;
        try
        {
            System.out.println("attempting socket connection!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            connectionSocket = serverSocket.accept();
            connectionSocket.setKeepAlive(true);
            connectionSocket.setSoTimeout(10);
            this.inputStream = new DataInputStream(connectionSocket.getInputStream());
            this.outputStream = new DataOutputStream(connectionSocket.getOutputStream());

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


        Instant NOW = Instant.now();
        long LAST_MILLI = NOW.toEpochMilli();


        // thread loop
        if (connectionSocket == null)
        {
            System.out.println("failed to get tcp connection...");
        }
        else
        {
            while (connectionSocket.isConnected())
            {
                System.out.println(connectionSocket.isConnected());
                while (!this.commandQueue.isEmpty())
                {
                    Struct cmd = this.commandQueue.poll();
                    try
                    {
                        outputStream.write(cmd.getByteBuffer().array());
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }

                long NOW_MILLI = Instant.now().toEpochMilli();

                if (NOW_MILLI - LAST_MILLI >= 10) // test if 10 milliseconds have elapsed
                {
                    StatusRequest statusRequest = new StatusRequest();
                    statusRequest.setByteBuffer(ByteBuffer.wrap(new byte[8]),0);
                    int size = statusRequest.size();
                    statusRequest.type.set(0);
                    statusRequest.size.set(size);

                    try
                    {
                        if (!statusRequest.getByteBuffer().hasArray()){
                            System.out.println("there is no array!!!!!!");
                        }
                        this.outputStream.write(statusRequest.getByteBuffer().array(), 0, size);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }

                try
                {

                    int size = inputStream.readInt();
                    System.out.println("data size: " + String.valueOf(size));
                    if (size == 0)
                    {
                        continue;
                    }

                    byte[] inputByteBuff = new byte[size];
                    inputByteBuff[0] = (byte) ((size & 0xf000) >> 3);
                    inputByteBuff[1] = (byte) ((size & 0x0f00) >> 2);
                    inputByteBuff[2] = (byte) ((size & 0x00f0) >> 1);
                    inputByteBuff[3] = (byte) (size & 0x000f);

                    //todo: this should read all of the message as the messages being sent are not that large, may need to modify though.
                    int ret = this.inputStream.read(inputByteBuff, 4, size - 4);
                    if (ret == 0)
                    {
                        System.out.println("no bytes read...");
                    }
                    else
                    {
                        ByteBuffer byteBuff = ByteBuffer.wrap(inputByteBuff);
                        StatusReport report = new StatusReport();
                        report.setByteBuffer(byteBuff, 0);
                        this.reports.add(report);
                        this.support.firePropertyChange("Node" + String.valueOf(this.nodeId),null,report);
                    }
                }
                catch (IOException e)
                {
                    System.out.println("input timed out, no message...");
                }

            }
        }
        System.out.println("shit broke!!!");
        return null;
    }
}
