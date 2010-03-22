package me.hcl.seekin.Auth

import me.hcl.seekin.Auth.User
import me.hcl.seekin.Util.Address
import me.hcl.seekin.Auth.Role.Role
import me.hcl.seekin.Internship.Internship
import me.hcl.seekin.Message.*


/**
 * User domain class.
 */
class User {

	/** Make User searchable */
	static searchable = {
		only = ['email','firstName','lastName','phone','showEmail']
		spellCheck "include"
	}

	/** A user can have some Role */
	static hasMany = [ authorities : Role, boxes: MessageBox ]

    /** Email which is used as a login */
	String email

	/** MD5 Password */
	String password

	/** Indicates if the user is enabled by an administrator */
	boolean enabled = false

	/** Indicate if the user has been validated by the admin **/
	boolean validated = false

	/** First name */
	String firstName

	/**	Last name */
	String lastName

	/** Address */
	Address address

	/** Phone number */
	String phone

    /** Indicates if the user wants to share his email */
	boolean showEmail = true

	/** Constraints used to check if an instance is correct */
	static constraints = {
		email(blank: false, unique: true, email: true)
		password(blank: false, minSize: 6)
		enabled(nullable:true)
        firstName(blank: false, maxSize: 30)
		lastName(blank: false, maxSize: 30)
		address(nullable: false)
		phone(nullable:true, maxSize: 15)
	}

        String toString() {
            firstName + " " + lastName
        }

        def beforeInsert = {
            def inbox = new InBox()
            def sent = new SentBox()
            def trash = new TrashBox()

            this.addToBoxes(inbox)
            this.addToBoxes(sent)
            this.addToBoxes(trash)

        }
        
        static mapping = {
            boxes sort:"label"
        }
}
