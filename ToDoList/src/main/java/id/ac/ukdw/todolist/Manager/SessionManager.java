package id.ac.ukdw.todolist.Manager;

import java.io.*;
import java.util.Map;

public class SessionManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String SESSION_FILE = "session.ser";

    private static volatile SessionManager instance;
    private boolean isLoggedIn;
    private Map<String, Object> userData;

    private SessionManager() {
        isLoggedIn = false;
        userData = null;
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                    instance.createSessionFile();
                }
            }
        }
        return instance;
    }

    public void createSessionFile() {
        File file = new File(SESSION_FILE);
        if (!file.exists()) {
            saveSession();
        } else {
            loadSession();
        }
    }

    private void loadSession() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SESSION_FILE))) {
            SessionManager sessionManager = (SessionManager) ois.readObject();
            this.isLoggedIn = sessionManager.isLoggedIn;
            this.userData = sessionManager.userData;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading session: " + e.getMessage());
        }
    }

    private void saveSession() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SESSION_FILE))) {
            oos.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void login() {
        isLoggedIn = true;
        saveSession();
    }

    public void logout() {
        isLoggedIn = false;
        userData = null;
        saveSession();
    }

    public void setUserData(Map<String, Object> data) {
        userData = data;
        saveSession();
    }

    public Map<String, Object> getUserData() {
        return userData;
    }
}