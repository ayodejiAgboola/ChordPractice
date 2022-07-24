package com.practice.chord;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ChordNode implements Runnable, IChordNode {

    int myKey;
    IChordNode successor;
    int successorKey;
    IChordNode predecessor;
    int predecessorKey;
    final int KEY_BITS = 8;
    int fingerTableLength;
    Finger[] fingers;
    int fixFingerIndex;

    HashMap<String, String > store = new HashMap<>();

    public ChordNode(String key){
        myKey = hash(key);
        successor = this;
        successorKey = myKey;
        fingerTableLength = KEY_BITS;
        fingers = new Finger[KEY_BITS];
        for( int i=0; i<fingers.length;i++){
            fingers[i] = new Finger();
        }
        new Thread(this).start();
    }

    @Override
    public IChordNode findSuccessor(int key) throws RemoteException {
        if(successor == this || isInHalfOpenRange(key, myKey, successorKey)){
            return successor;
        }else {
            IChordNode closestNode = closestPrecedingNode(key);
            if(closestNode == this){
                return this;
            }else {
                return closestNode.findSuccessor(key);
            }
        }
    }

    @Override
    public IChordNode closestPrecedingNode(int key) throws RemoteException {
        for(int i = fingers.length-1; i>=0 && i<fingers.length; i--){
            if(fingers[i]!=null && isInCLoseRange(fingers[i].fingerKey, myKey, key)){
                return fingers[i].finger;
            }
        }
        return this;
    }

    @Override
    public void join(IChordNode node) throws RemoteException {
        predecessor = null;
        predecessorKey = 0;
        successor = findSuccessor(myKey);
        successorKey = successor.getKey();
    }

    @Override
    public void stabilize() throws RemoteException {
        IChordNode potentialSuccessor = successor.getPredecessor();
        if(potentialSuccessor!=null && isInCLoseRange(potentialSuccessor.getKey(),myKey,successorKey)){
            successor = potentialSuccessor;
            successorKey = potentialSuccessor.getKey();
        }
        successor.notifyNode(this);
    }

    @Override
    public void fixFingers() throws RemoteException {
        fixFingerIndex = fixFingerIndex+1;
        if(fixFingerIndex>=fingerTableLength){
            fixFingerIndex = 1;
        }
        IChordNode finger = findSuccessor(myKey+(int)Math.pow(2,fixFingerIndex-1));
        fingers[fixFingerIndex].finger = finger;
        fingers[fixFingerIndex].fingerKey = finger.getKey();

    }

    @Override
    public IChordNode getPredecessor() throws RemoteException {
        return predecessor;
    }

    @Override
    public void notifyNode(IChordNode node) throws RemoteException {
        if(predecessor==null || isInCLoseRange(node.getKey(),predecessorKey, myKey)){
            predecessor = node;
            predecessorKey = node.getKey();
        }

    }

    @Override
    public void checkDataMoveDown() throws RemoteException {
        Iterator it = store.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            String key = (String) pair.getKey();
            String value = (String) pair.getValue();
            if(isInHalfOpenRange(hash(key),predecessor.getPredecessor().getKey(), predecessorKey)){
                predecessor.put(key,value);
            }
            it.remove();
        }
    }

    @Override
    public int getKey() throws RemoteException {
        return myKey;
    }

    @Override
    public String get(String key) throws RemoteException {
        return null;
    }

    @Override
    public void put(String key, String value) throws RemoteException {

    }

    @Override
    public void checkPredecessor() throws RemoteException {
        try {
            predecessor.getKey();
        }catch (RemoteException e){
            predecessor = null;
            predecessorKey = 0;
        }

    }

    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                stabilize();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                fixFingers();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                checkPredecessor();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                checkDataMoveDown();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public int hash(String key){
        int hash = 0;
        for(int i=0; i<key.length();i++){
            hash = hash*31+key.charAt(i);
        }
        if(hash<0){
            hash = hash * -1;
        }

        return hash % (int) Math.pow(2,KEY_BITS);
    }

    public boolean isInCLoseRange(int key, int start, int end){
        if(end>start)
            return key>start&&key<end;
        else
            return key>start || key<end;
    }

    public boolean isInHalfOpenRange(int key, int start, int end){
        if(end>start)
            return key>start&&key<=end;
        else
            return key>start || key<=end;
    }

    public static void main(String[] args) {
        ChordNode node = new ChordNode(args[0]);

        try {
            IChordNode stub = (IChordNode) UnicastRemoteObject.exportObject(node, Integer.valueOf(args[1]));
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(String.valueOf(node.myKey),stub);
            ILoadBalancer loadBalancer =(ILoadBalancer) registry.lookup("loadbalancer");
            IChordNode nodeToJoin = loadBalancer.addNode(stub);
            nodeToJoin.join(stub);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
