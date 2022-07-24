package com.practice.chord;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IChordNode extends Remote {

    IChordNode findSuccessor(int key) throws RemoteException;
    IChordNode closestPrecedingNode(int key) throws RemoteException;
    void join(IChordNode node) throws RemoteException;
    void stabilize() throws RemoteException;
    void fixFingers() throws RemoteException;
    IChordNode getPredecessor() throws RemoteException;
    void notifyNode(IChordNode node) throws RemoteException;
    void checkDataMoveDown() throws RemoteException;
    int getKey() throws RemoteException;
    String get(String key) throws RemoteException;
    void put(String key, String value) throws RemoteException;
    void checkPredecessor() throws RemoteException;

}
