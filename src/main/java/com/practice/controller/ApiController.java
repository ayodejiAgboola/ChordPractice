package com.practice.controller;

import com.practice.chord.IChordNode;
import com.practice.chord.ILoadBalancer;
import com.practice.model.Store;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

@RestController
@RequestMapping("/")
public class ApiController {
    @PostMapping("values/{key}")
    public ResponseEntity putValue(@PathVariable String key, @RequestBody Store store){
        try {
            Registry registry = LocateRegistry.getRegistry();
            ILoadBalancer loadBalancer = (ILoadBalancer) registry.lookup("loadbalancer");
            IChordNode node = loadBalancer.getNode();
            System.out.println(key);
            System.out.println(store.getValue());
            node.put(key, store.getValue());
            return ResponseEntity.ok().build();
        } catch (RemoteException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (NotBoundException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("values/{key}")
    public ResponseEntity getValue(@PathVariable String key){
        try {
            Registry registry = LocateRegistry.getRegistry();
            ILoadBalancer loadBalancer = (ILoadBalancer) registry.lookup("loadbalancer");
            IChordNode node = loadBalancer.getNode();
            String value = node.get(key);
            if(value==null||value.isEmpty()){
                return ResponseEntity.status(204).build();
            }else {
                Store store = new Store();
                store.setValue(value);
                return ResponseEntity.ok().body(store);
            }
        } catch (AccessException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (RemoteException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (NotBoundException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
