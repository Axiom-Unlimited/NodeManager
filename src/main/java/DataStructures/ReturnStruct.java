package DataStructures;

import Manager.MessageManager;
import org.json.JSONObject;

public class ReturnStruct
{
    private String nextScene;
    private MessageManager messageManager;
    private JSONObject settings;

    public String getNextScene()
    {
        return nextScene;
    }

    public void setNextScene(String nextScene)
    {
        this.nextScene = nextScene;
    }

    public MessageManager getMessageManager()
    {
        return messageManager;
    }

    public void setMessageManager(MessageManager messageManager)
    {
        this.messageManager = messageManager;
    }

    public JSONObject getSettings()
    {
        return settings;
    }

    public void setSettings(JSONObject settings)
    {
        this.settings = settings;
    }
}
