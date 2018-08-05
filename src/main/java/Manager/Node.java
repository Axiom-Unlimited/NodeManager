package Manager;

import DataStructures.*;
import javolution.io.Struct;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Node implements Runnable
{

    private Queue<Struct> commandQueue;
    public Queue<StatusReport> reports;

    // tcp socket member fields
    private Integer port;
    private ServerSocket serverSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;


    public Node(Integer port) throws IOException
    {
        this.port = port;
        commandQueue = new ConcurrentLinkedQueue<>();
        reports = new ConcurrentLinkedQueue<>();
        serverSocket = new ServerSocket(this.port);
    }

    public Boolean sendCommand(Struct command)
    {
        this.commandQueue.add(command);
        return this.commandQueue.isEmpty();
    }

    @Override
    public void run()
    {
        //set up connection stuff
        Socket connectionSocket = null;
        try
        {
            connectionSocket = serverSocket.accept();
            connectionSocket.setKeepAlive(true);
            connectionSocket.setSoTimeout(1);
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
                    int size = statusRequest.size();
                    statusRequest.type.set(0);
                    statusRequest.size.set(size);
                    ByteBuffer statReqBuff = statusRequest.getByteBuffer();

                    try
                    {
                        this.outputStream.write(statReqBuff.array(), 0, size);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }


                try
                {
                    int size = inputStream.readInt();
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
                    }
                }
                catch (IOException e)
                {
                    System.out.println("input timed out, no message...");
                }

            }
        }
    }
}
