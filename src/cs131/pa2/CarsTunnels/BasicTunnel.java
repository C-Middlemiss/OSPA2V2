package cs131.pa2.CarsTunnels;

import java.awt.List;
import java.util.ArrayList;

import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;

public class BasicTunnel extends Tunnel{
	public volatile ArrayList<Vehicle> occupantsVehicles; 
	private static int maxVehicles; 
	private boolean ambulancePresent; 
	
	public BasicTunnel(String name) {
		super(name);
		this.ambulancePresent = false; 
		maxVehicles = 3; 
		occupantsVehicles = new ArrayList<Vehicle>();
	}

	@Override
	public synchronized boolean tryToEnterInner(Vehicle vehicle) {
		//deal with ambulance 
		if (vehicle instanceof Ambulance){
			this.setAmbulancePresent(true);
			for (Vehicle v: occupantsVehicles){
				v.setAmbulancePresent(true);
			}
			occupantsVehicles.add(vehicle);
			return true;
			
		}	
		//deal with Sled
		if (vehicle instanceof Sled){	
			if (occupantsVehicles.size() > 0){
				return false;
			}
			occupantsVehicles.add(vehicle);
			return true; 
		} else {
			//deal with sleds
			for (int i = 0; i < occupantsVehicles.size(); i++){
				if (occupantsVehicles.get(i) instanceof Sled || occupantsVehicles.get(i) instanceof Ambulance){
					return false; 
				}
			}
			//deal with direction
			if (occupantsVehicles.size() > 0 && occupantsVehicles.get(0).getDirection() != vehicle.getDirection()){
				return false;
			}
			//deal with capacity
			if (occupantsVehicles.size() >= this.maxVehicles){
				return false;
			} 
			//System.out.println("enter" + vehicle.getName());
			occupantsVehicles.add(vehicle);
			return true; 
		}
	}


	@Override
	public synchronized void exitTunnelInner(Vehicle vehicle) {
		for (int i = 0; i < occupantsVehicles.size(); i++){
			if (vehicle instanceof Ambulance){
				this.setAmbulancePresent(false);
				for (Vehicle v: occupantsVehicles){
					v.setAmbulancePresent(false);
				}
			}
			if (occupantsVehicles.get(i).equals(vehicle)){
				occupantsVehicles.remove(i);
			}
		}
	}
	
	public boolean getAmbulancePresent(){
    	return this.ambulancePresent; 
    }
    public void setAmbulancePresent(boolean value){
    	this.ambulancePresent = value;
    }
}
