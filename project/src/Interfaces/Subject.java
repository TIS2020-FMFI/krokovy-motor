package Interfaces;

public interface Subject {

    void attach(Observer observer);
    void notifyObservers();
}
