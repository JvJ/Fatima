package FAtiMA.NormativeComponent;

import java.util.ArrayList;

public class NormativeEnvironment {
	
	private ArrayList<Norm> _normsAdoptedBySelf;
	private ArrayList<Norm> _normsRejectedBySelf;
	
	private ArrayList<Norm> _instancesRecentlyCreated;
	private ArrayList<Norm> _instancesCurrentlyActive;
	private ArrayList<Norm> _instancesFollowedBySelf;
	private ArrayList<Norm> _instancesFollowedByOthers;
	private ArrayList<Norm> _instancesRecentlyExpired;
	private ArrayList<Norm> _instancesFulfilledBySelf;
	private ArrayList<Norm> _instancesViolatedBySelf;
	private ArrayList<Norm> _instancesFulfilledByOthers;
	private ArrayList<Norm> _instancesViolatedByOthers;
	private ArrayList<Norm> _instancesOfThePast;
	
	
	private static NormativeEnvironment _instance;
	
	private NormativeEnvironment(){
		_normsAdoptedBySelf = new ArrayList<Norm>();
		_normsRejectedBySelf = new ArrayList<Norm>();
		_instancesRecentlyCreated = new ArrayList<Norm>();
		_instancesCurrentlyActive = new ArrayList<Norm>();
		_instancesFollowedBySelf = new ArrayList<Norm>();
		_instancesFollowedByOthers = new ArrayList<Norm>();
		_instancesRecentlyExpired = new ArrayList<Norm>();
		_instancesFulfilledBySelf = new ArrayList<Norm>();
		_instancesViolatedBySelf = new ArrayList<Norm>();
		_instancesFulfilledByOthers = new ArrayList<Norm>();
		_instancesViolatedByOthers = new ArrayList<Norm>();
		_instancesOfThePast = new ArrayList<Norm>();
	}
	
	public static synchronized NormativeEnvironment getNormativeEnvironment(){
		if(_instance == null){
			_instance = new NormativeEnvironment();
		}
		return _instance;
	}
	
	public Object clone() throws CloneNotSupportedException{
		throw new CloneNotSupportedException();
	}

	public ArrayList<Norm> getNormsAdoptedBySelf() {
		return _normsAdoptedBySelf;
	}

	public ArrayList<Norm> getNormsRejectedBySelf() {
		return _normsRejectedBySelf;
	}

	public ArrayList<Norm> getInstancesRecentlyCreated() {
		return _instancesRecentlyCreated;
	}

	public ArrayList<Norm> getInstancesCurrentlyActive() {
		return _instancesCurrentlyActive;
	}

	public ArrayList<Norm> getInstancesFollowedBySelf() {
		return _instancesFollowedBySelf;
	}

	public ArrayList<Norm> getInstancesFollowedByOthers() {
		return _instancesFollowedByOthers;
	}

	public ArrayList<Norm> getInstancesRecentlyExpired() {
		return _instancesRecentlyExpired;
	}
	
	public ArrayList<Norm> getInstancesFulfilledBySelf() {
		return _instancesFulfilledBySelf;
	}

	public ArrayList<Norm> getInstancesViolatedBySelf() {
		return _instancesViolatedBySelf;
	}

	public ArrayList<Norm> getInstancesFulfilledByOthers() {
		return _instancesFulfilledByOthers;
	}

	public ArrayList<Norm> getInstancesViolatedByOthers() {
		return _instancesViolatedByOthers;
	}

	public ArrayList<Norm> getInstancesOfThePast() {
		return _instancesOfThePast;
	}
	
}