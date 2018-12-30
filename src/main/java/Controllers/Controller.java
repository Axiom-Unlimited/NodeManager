package Controllers;

import DataStructures.ReturnStruct;
import Manager.MessageManager;
import org.json.JSONObject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class Controller
{
    //Property change listener
    PropertyChangeSupport support;

    MessageManager messageManager;

    JSONObject settings;

    Controller()
    {
        support = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl)
    {
        this.support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl)
    {
        support.removePropertyChangeListener(pcl);
    }

    public abstract void create(ReturnStruct param);
}
