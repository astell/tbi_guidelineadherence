/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package patients;

import java.util.*;

public class EventProcessModel {
    
    private Vector<Node> nodes;
    private Vector<Edge> edges;
    private String type, timeTaken, nature;
    private String patientID;
    private int eventIndex;
    
    public EventProcessModel(String _type, String _timeTaken, String _nature, String _patientID, int _eventIndex){
        
        this.patientID = _patientID;
        this.eventIndex = _eventIndex;
        
        if(_type.equals("")){
            this.type = _type;
            this.timeTaken = _timeTaken;
            this.nature = _nature;
        
            Node startNode = new Node("Event start","");        
            Node endNode = new Node("Check level","");
        
            nodes = new Vector<Node>();
            nodes.add(startNode);
            nodes.add(endNode);
        
            edges = new Vector<Edge>();
            edges.add(new Edge(startNode,endNode));
            
        }else{
            
            //System.out.println("Into EPM creation...");
            
            this.type = _type;
            this.timeTaken = _timeTaken;
            this.nature = _nature;
        
            Node startNode = new Node("Event start","");        
            Node typeNode = new Node("Type",this.type);
            Node timeTakenNode = new Node("Time to treatment",this.timeTaken);
            Node natureNode = new Node("Dose",this.nature);        
            Node endNode = new Node("Check level","");
            
            //System.out.println("Created five nodes...");
            
            nodes = new Vector<Node>();
            nodes.add(startNode);
            nodes.add(typeNode);
            nodes.add(timeTakenNode);
            nodes.add(natureNode);        
            nodes.add(endNode);
            
            //System.out.println("Added five nodes to EPM...");
            
            edges = new Vector<Edge>();
            edges.add(new Edge(startNode,typeNode));
            edges.add(new Edge(typeNode,endNode));
            edges.add(new Edge(typeNode,timeTakenNode));
            edges.add(new Edge(typeNode,natureNode));
            
            //System.out.println("Added four edges to EPM...");
            
            //System.out.println("====");
        }
        
    }
    
    public Node getNode(String label){
        
        boolean nodeFound = false;
        int nodeCount = 0;
        while(!nodeFound && nodeCount > nodes.size()){
            if(nodes.get(nodeCount).getLabel().equals(label)){
                nodeFound = true;
            }else{
                nodeCount++;
            }
        }
        return nodes.get(nodeCount);
    }
    
    
    public Vector<Node> getNodes(){
        return nodes;
    }
    
    public Vector<Edge> getEdges(){
        return edges;
    }
    
}
