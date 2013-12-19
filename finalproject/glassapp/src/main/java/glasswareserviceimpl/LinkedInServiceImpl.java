package glasswareserviceimpl;

import glasswaredata.LinkedIn;
import glasswareservice.UserService;

public class LinkedInServiceImpl implements UserService{
	private LinkedIn user;
	
	public LinkedIn getInfo(String name){
		
		this.user = new LinkedIn(name);
		//whatever processing you need
		
		return this.user;
		
	}

}
