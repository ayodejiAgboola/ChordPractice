package com.practice.chord;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;

public class LoadBalancer implements ILoadBalancer{
    ArrayList<IChordNode> serverList = new ArrayList<>();
    final String NAME = "loadbalancer";
    final int PORT = 4000;
    @Override
    public IChordNode addNode(IChordNode node) throws RemoteException {
        IChordNode nodeToJoin = null;
        if(serverList.size()>0){
            Random random = new Random();
            int randomNode = random.nextInt(serverList.size());
            nodeToJoin=serverList.get(randomNode);
        }else {
            nodeToJoin = node;
        }
        serverList.add(node);
        System.out.println(serverList.size());
        return nodeToJoin;
    }

    @Override
    public IChordNode getNode() throws RemoteException {
        Random random = new Random();
        int randomNode = random.nextInt(serverList.size());
        return serverList.get(randomNode);
    }

    public static void main(String[] args) {
        LoadBalancer loadBalancer = new LoadBalancer();
        try{
            ILoadBalancer stub = (ILoadBalancer) UnicastRemoteObject.exportObject(loadBalancer, loadBalancer.PORT);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(loadBalancer.NAME, stub);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
