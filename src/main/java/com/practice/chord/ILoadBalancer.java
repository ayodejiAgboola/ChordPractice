package com.practice.chord;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ILoadBalancer extends Remote {
    IChordNode addNode(IChordNode node) throws RemoteException;
    IChordNode getNode() throws RemoteException;
}
