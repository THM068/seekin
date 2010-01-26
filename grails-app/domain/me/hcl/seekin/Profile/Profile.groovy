package me.hcl.seekin.Profile

import me.hcl.seekin.Auth.User;

/*
 * Profile class
 */
class Profile extends User {
	
	/** First name */
	String firstName
	
	/**	Last name */
	String lastName
	
	/** Address */
	String address
	
	/** phone number */
	String phone
	
	/** Constraints used to check if an instance is correct */
    static constraints = {
		firstName(blank: false)
		lastName(blank: false)
		address(blank: false)
		phone(size: 10..10)
    }
}
