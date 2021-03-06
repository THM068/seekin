package me.hcl.seekin.Internship


import org.codehaus.groovy.grails.plugins.springsecurity.Secured
import grails.converters.JSON
import me.hcl.seekin.Auth.Role.*
import me.hcl.seekin.Auth.User
import me.hcl.seekin.Auth.UserController
import me.hcl.seekin.Util.Address
import me.hcl.seekin.Internship.Company
import me.hcl.seekin.Formation.Millesime
import me.hcl.seekin.Formation.Promotion
import org.hibernate.LockMode
import me.hcl.seekin.Formation.Formation

class InternshipController {

    def authenticateService
    def fileService
    def sessionFactory

    def index = { redirect(action: "list", params: params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def list = {

        /* Controls if some internships which wait for Validation are validated by examinate params */
        def internship
        params.each {
            if(it.key.contains("validate_") && it.value == "on") {
                /* Get the offer and do the mofification before saving */
                internship = Internship.get(it.key.split("_")[1].toInteger())
                internship.isApproval = true
                internship.save(flush:true)
            }
        }

        /* If the user is not logged in, we redirect him at the index of seekin */
        def status = new HashSet()

        if(!authenticateService.isLoggedIn()) {
            redirect(controller: "user", action: "index")
        }
        else
        {

            def userInstance = authenticateService.userDomain()

            if(authenticateService.ifAnyGranted("ROLE_ADMIN,ROLE_FORMATIONMANAGER,ROLE_STAFF"))
            {
                def millesimes = Millesime.findAllByBeginDateLessThan(new Date())
                def millesimeCurrent = Millesime.getCurrent()
                def millesimeSelected

                if(params.idMillesime)
                        millesimeSelected = Millesime.get(params.idMillesime)
                else
                        millesimeSelected = millesimeCurrent

                if(authenticateService.ifAnyGranted("ROLE_ADMIN"))
                {
                    Internship.findAllByMillesime(millesimeSelected).each {
                        status.add it.getStatus()
                    }
                }
                else if(authenticateService.ifAnyGranted("ROLE_FORMATIONMANAGER"))
                {
                    def manager = FormationManager.findByUser(userInstance)
                    def promotion = Promotion.findByMillesimeAndFormation(millesimeSelected, manager?.formation)
                    if(promotion?.students?.size() > 0)
                    {
                        def internships = Internship.createCriteria().list() {
                            'in'('student', promotion?.students)
                            eq('millesime', millesimeSelected)
                        }
                        internships.each {
                            status.add it.getStatus()
                        }
                    }
                }
                else if(authenticateService.ifAnyGranted("ROLE_STAFF"))
                {
                    def staff = Staff.findByUser(userInstance)
                    Internship.findAllByAcademicTutorAndMillesime(staff, millesimeSelected).each {
                        if(it.getStatus() == 'internship.validated')
                            status.add it.getStatus()
                    }
                }

                render(view: "list", model: [status: status, millesimes: millesimes])

            }
            else if(authenticateService.ifAnyGranted("ROLE_STUDENT")) {
                def student = Student.findByUser(userInstance)
                Internship.findAllByStudent(student).each {
                        status.add it.getStatus()
                }
                render(view: "list", model: [status: status])
            }
            else if(authenticateService.ifAnyGranted("ROLE_EXTERNAL")) {
                def external = External.findByUser(userInstance)
                external.internships.each {
                    status.add it.getStatus()
                }
                render(view: "list", model: [status: status])
            }
        }
    }

    @Secured(['ROLE_ADMIN','ROLE_FORMATIONMANAGER'])
    def assignate = {

        def internship

        params.each {
            if(it.key.contains("assignate_")) {
                def internshipId = it.key.split("_")[1].toInteger()

                internship = Internship.get(internshipId)
                if(internship)
                {
                    if(it.value != "null")
                        internship.academicTutor = Staff.get(it.value.toInteger())
                    else
                        internship.academicTutor = null
                    internship.save()
                }
            }
        }

        def formations = new HashSet()
        if(authenticateService.ifAnyGranted("ROLE_ADMIN")) {
            formations = Promotion.getCurrents().formation
        }
        else if(authenticateService.ifAnyGranted("ROLE_FORMATIONMANAGER")) {
            def userInstance = authenticateService.userDomain()
            sessionFactory.currentSession.refresh(userInstance, LockMode.NONE)
            formations = FormationManager.findByUser(userInstance).formation
        }
        return [formations: formations]
    }

    @Secured(['ROLE_ADMIN','ROLE_FORMATIONMANAGER', 'ROLE_STUDENT'])
    def create = {
        def staff = Staff.list().collect {
            [
                    id: it.id,
                    value: it.user?.firstName + " " + it.user?.lastName
            ]
        }
        def student = Student.list().collect {
            [
                    id: it.id,
                    value: it.user?.firstName + " " + it.user?.lastName
            ]
        }

        def millesime = Millesime.getCurrent()

        def offer = []
        Offer.findAllByBeginAtGreaterThanEquals(millesime.beginDate).each {
            if(it.getStatus() == "offer.validated")
            {
                offer <<
                    [
                            id: it.id,
                            value: it.subject
                    ]
            }
        }
        def internshipInstance
        if(params?.offer?.id != null) {
            def offerFromSelection = Offer.get(params.offer.id)
            internshipInstance = new Internship()
                internshipInstance.properties = params
                internshipInstance.subject = offerFromSelection.subject
                internshipInstance.beginAt = offerFromSelection.beginAt
                internshipInstance.length = offerFromSelection.length
                internshipInstance.company = offerFromSelection.company
                internshipInstance.fromOffer = true
                internshipInstance.description = offerFromSelection.description
                internshipInstance.file = offerFromSelection.file
        }
        else {
            internshipInstance = new Internship()
            internshipInstance.properties = params
        }
        return [internshipInstance: internshipInstance, staff: staff, student: student, offer: offer, company: internshipInstance.company]
    }

    @Secured(['ROLE_ADMIN','ROLE_FORMATIONMANAGER', 'ROLE_STUDENT'])
    def save = {

        def staff = Staff.list().collect {
            [
                    id: it.id,
                    value: it.user?.firstName + " " + it.user?.lastName
            ]
        }
        def student = Student.list().collect {
            [
                    id: it.id,
                    value: it.user?.firstName + " " + it.user?.lastName
            ]
        }

        def millesime = Millesime.getCurrent()
        def offer = []
        Offer.findAllByBeginAtGreaterThanEquals(millesime.beginDate).each {
            if(it.getStatus() == "offer.validated")
            {
                offer <<
                    [
                            id: it.id,
                            value: it.subject
                    ]
            }
        }

        def internshipInstance = new Internship()

        if(authenticateService.ifAnyGranted("ROLE_STUDENT")) {
            def userInstance = authenticateService.userDomain()
            sessionFactory.currentSession.refresh(userInstance, LockMode.NONE)
            def statusStudent = Student.findByUser(userInstance)
            internshipInstance.student = statusStudent
            internshipInstance.isApproval = false
        }

        internshipInstance.properties = params
        internshipInstance.millesime = Millesime.getCurrent()

        def company
        if(params.companyName != null && params.companyName != "")
        {
            if(Company.countByName(params.companyName) == 0) {
              company = new Company()
              company.name = params.companyName
            }
            else {
              company = Company.findByName(params.companyName)
            }
            internshipInstance.company = company
        }


        if(params?.file?.id)
        {
            internshipInstance.file = InternshipSubjectFile.get(params?.file?.id)
        }
        else if(request.getFile( 'data' )?.getSize() > 0)
        {
            def document = new InternshipSubjectFile()
            document.title = params.subject
            document.fileData = fileService.createFile(request.getFile( 'data' ))
            internshipInstance.file = document
        }

        internshipInstance.validate()

        if (params.email == null || params.email == "") {
            internshipInstance.errors.rejectValue(
                'companyTutor.user.email',
                'companyTutor.email.null'
            )
        }

        if (params.firstName == null || params.firstName == "") {
            internshipInstance.errors.rejectValue(
                'companyTutor.user.firstName',
                'companyTutor.firstName.null'
            )
        }

        if (params.lastName == null || params.lastName == "") {
            internshipInstance.errors.rejectValue(
                'companyTutor.user.lastName',
                'companyTutor.lastName.null'
            )
        }

        if (!internshipInstance.hasErrors())
        {
            def role
            def companyTutor

            if(User.countByEmail(params.email) == 0) {
                if(User.countByFirstNameAndLastName(params.firstName, params.lastName) == 0)
                {
                  role = new External()
                  company.save()
                  role.company = company
                  role.formerStudent = false
                  role = role.save(flush:true)
                  companyTutor = new User()
                  companyTutor.properties = params
                  companyTutor.password = authenticateService.encodePassword(UserController.generatePwd(8))
                  companyTutor.addToAuthorities(role)
                  companyTutor.save(flush:true)
                }
                else {
                  companyTutor = User.findByFirstNameAndLastName(params.firstName, params.lastName)
                  role = External.findByUser(companyTutor)
                }
            }
            else {
                companyTutor = User.findByEmail(params.email)
                role = External.findByUser(companyTutor)
            }

            internshipInstance.companyTutor = role

            if ((internshipInstance = internshipInstance?.save(flush: true))) {

                flash.message = "internship.created"
                flash.args = [internshipInstance.id]
                flash.defaultMessage = "Internship ${internshipInstance.id} created"
                redirect(action: "show", id: internshipInstance.id)
            }

        }
        else
        {
            render(
                view: "create",
                model: [
                        internshipInstance: internshipInstance,
                        staff: staff,
                        student: student,
                        offer: offer,
                        company: params.companyName,
                        email: params.email,
                        firstName: params.firstName,
                        lastName: params.lastName
                ]
            )
        }
    }

    def show = {
        
        /* We get the user logged in to verify if he is authorized to show an internship */
        def userInstance = authenticateService.userDomain()
        sessionFactory.currentSession.refresh(userInstance, LockMode.NONE)
        Boolean showable = false

        /* Get the offer with id parameter */
        def internshipInstance = Internship.get(params.id)
        /* If the user have an external role, he is allowed to show only offers which he had created */
        if(authenticateService.ifAnyGranted("ROLE_EXTERNAL")) {
            if(internshipInstance?.companyTutor?.user == userInstance) {
                showable = true
            }
        }
        /* If the user is the administrator, he can show all offers */
        else if(authenticateService.ifAnyGranted("ROLE_ADMIN")) {
            showable = true
        }
        else if(authenticateService.ifAnyGranted("ROLE_FORMATIONMANAGER")) {

            def manager = FormationManager.findByUser(userInstance)

            def promotion = internshipInstance?.student?.promotions.find {
                it.millesime?.id == internshipInstance?.millesime?.id
            }

            if(promotion?.formation?.id == manager?.formation?.id)
                showable = true
        }
        /* If the user is a staff's member, he is allowed to show offers which he had created and offers that are validated */
        else if(authenticateService.ifAnyGranted("ROLE_STAFF")) {
            if(internshipInstance?.academicTutor?.user == userInstance) {
                showable = true
            }
        }
        /* If the user is a student, he can show all offers which are validated and correspond to his promotion */
        else if(authenticateService.ifAnyGranted("ROLE_STUDENT")) {
            if(internshipInstance?.student?.user ==  userInstance) {
                showable = true
            }
        }

        /* If the offer doesn't exist or is not showable, we return a flash message */
        if (!internshipInstance || !showable) {
            flash.message = "internship.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "Internship not found with id ${params.id}"
            redirect(action: "list")
        }
        /* Else we return our instance */
        else if(showable) {
            return [internshipInstance: internshipInstance]
        }
    }

    @Secured(['ROLE_ADMIN','ROLE_FORMATIONMANAGER', 'ROLE_STUDENT'])
    def edit = {

        /* We get the user logged in to verify if he is authorized to edit an internship */
        def userInstance = authenticateService.userDomain()
        sessionFactory.currentSession.refresh(userInstance, LockMode.NONE)
        Boolean editable = false

        /* Get the internship with id parameter */
        def internshipInstance = Internship.get(params.id)


        if(authenticateService.ifAnyGranted("ROLE_ADMIN")) {
            editable = true
        }
        else if(authenticateService.ifAnyGranted("ROLE_STUDENT")) {
            if(internshipInstance?.student?.user ==  userInstance) {
                editable = true
            }
        }
        else if(authenticateService.ifAnyGranted("ROLE_FORMATIONMANAGER")) {
            def manager = FormationManager.findByUser(userInstance)

            def promotion = internshipInstance?.student?.promotions.find {
                it.millesime?.id == internshipInstance?.millesime?.id
            }

            if(promotion?.formation?.id == manager?.formation?.id)
                editable = true
        }
        

        /* If the internship is not validated, it is editable */
        if (internshipInstance?.isApproval == true) {
            editable = false
        }

        /* If the offer is editable, we return our needed instance */
        if(editable) {
                return [internshipInstance: internshipInstance]
        }
        /* Else we return a flash message */
        else {
            flash.message = "internship.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "Internship not found with id ${params.id}"
            redirect(action: "list")
        }
    }

    @Secured(['ROLE_ADMIN','ROLE_FORMATIONMANAGER', 'ROLE_STUDENT'])
    def update = {
        def internshipInstance = Internship.get(params.id)

        /* We get the user logged in to verify if he is authorized to edit an internship */
        def userInstance = authenticateService.userDomain()
        sessionFactory.currentSession.refresh(userInstance, LockMode.NONE)
        Boolean editable = false

        if(authenticateService.ifAnyGranted("ROLE_ADMIN")) {
            editable = true
        }
        else if(authenticateService.ifAnyGranted("ROLE_STUDENT")) {
            if(internshipInstance?.student?.user ==  userInstance) {
                editable = true
            }
        }
        else if(authenticateService.ifAnyGranted("ROLE_FORMATIONMANAGER")) {
            def manager = FormationManager.findByUser(userInstance)

            def promotion = internshipInstance?.student?.promotions.find {
                it.millesime?.id == internshipInstance?.millesime?.id
            }

            if(promotion?.formation?.id == manager?.formation?.id)
                editable = true
        }

        if (internshipInstance && editable) {
            if (params.version) {
                def version = params.version.toLong()
                if (internshipInstance.version > version) {

                    internshipInstance.errors.rejectValue("version", "internship.optimistic.locking.failure", "Another user has updated this Internship while you were editing")
                    render(view: "edit", model: [internshipInstance: internshipInstance])
                    return
                }
            }

            def oldDoc = internshipInstance.file

            internshipInstance.properties = params

            if(request.getFile( 'data' ).getSize() > 0)
            {
                def document = new InternshipSubjectFile()
                document.title = params.subject
                document.fileData = fileService.createFile(request.getFile( 'data' ))
                internshipInstance.file = document
            }

            internshipInstance.reason = null

            if (!internshipInstance.hasErrors() && internshipInstance.save()) {

                if(oldDoc) {
                    oldDoc.delete()
                }
                
                flash.message = "internship.updated"
                flash.args = [params.id]
                flash.defaultMessage = "Internship ${params.id} updated"
                redirect(action: "show", id: internshipInstance.id)
            }
            else {
                render(view: "edit", model: [internshipInstance: internshipInstance])
            }
        }
        else {
            flash.message = "internship.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "Internship not found with id ${params.id}"
            redirect(action: "edit", id: params.id)
        }
    }

    /* It is used when an internship is deny by the administrator */
    @Secured(['ROLE_ADMIN','ROLE_FORMATIONMANAGER'])
    def deny = {
        /* Get the internship and set reason with params.reason before saving and redirect the user to list */
        def internshipInstance = Internship.get(params.id)

       /* We get the user logged in to verify if he is authorized to edit an internship */
        def userInstance = authenticateService.userDomain()
        sessionFactory.currentSession.refresh(userInstance, LockMode.NONE)
        Boolean editable = false

        if(authenticateService.ifAnyGranted("ROLE_ADMIN")) {
            editable = true
        }
        else if(authenticateService.ifAnyGranted("ROLE_FORMATIONMANAGER")) {
            def manager = FormationManager.findByUser(userInstance)

            def promotion = internshipInstance?.student?.promotions.find {
                it.millesime?.id == internshipInstance?.millesime?.id
            }

            if(promotion?.formation?.id == manager?.formation?.id)
                editable = true
        }

        if (internshipInstance && editable) {
            internshipInstance.reason = params.reason
            internshipInstance.save()
            redirect(action: "list")
        }
        /* Else we return a flash message */
        else {
            flash.message = "internship.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "Internship not found with id ${params.id}"
            redirect(action: "list")
        }
    }

    @Secured(['ROLE_ADMIN','ROLE_FORMATIONMANAGER', 'ROLE_STUDENT'])
    def delete = {
        
        def internshipInstance = Internship.get(params.id)

        /* We get the user logged in to verify if he is authorized to edit an internship */
        def userInstance = authenticateService.userDomain()
        sessionFactory.currentSession.refresh(userInstance, LockMode.NONE)
        Boolean editable = false

        if(authenticateService.ifAnyGranted("ROLE_ADMIN")) {
            editable = true
        }
        else if(authenticateService.ifAnyGranted("ROLE_STUDENT")) {
            if(internshipInstance?.student?.user ==  userInstance) {
                editable = true
            }
        }
        else if(authenticateService.ifAnyGranted("ROLE_FORMATIONMANAGER")) {
            def manager = FormationManager.findByUser(userInstance)

            def promotion = internshipInstance?.student?.promotions.find {
                it.millesime?.id == internshipInstance?.millesime?.id
            }

            if(promotion?.formation?.id == manager?.formation?.id)
                editable = true
        }

        if (internshipInstance && editable) {
            try {
                internshipInstance.academicTutor = null
                internshipInstance.companyTutor = null
                internshipInstance.student = null
                internshipInstance.company = null
                internshipInstance.millesime = null
                
                internshipInstance.delete()
                flash.message = "internship.deleted"
                flash.args = [params.id]
                flash.defaultMessage = "Internship ${params.id} deleted"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "internship.not.deleted"
                flash.args = [params.id]
                flash.defaultMessage = "Internship ${params.id} could not be deleted"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "internship.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "Internship not found with id ${params.id}"
            redirect(action: "list")
        }
    }

    def dataTableAsJSON = {
        def list = []
        Internship.findAllByMillesime(Millesime.getCurrent()).each {
            if(Promotion.getCurrentForStudent(it.student).formation.id == params.formation.toInteger()) {
                list.add it.id
            }
        }

        def list2
        if(list.size() > 0) {
            list2 = Internship.createCriteria().list(params) {
                'in'('id', list)
            }
        }


        def ret = []
        response.setHeader("Cache-Control", "no-store")

        list2.each {
            ret << [
               id:it.id,
               subject:it.subject,
               student:[name:it.student?.user?.firstName + " " + it.student?.user?.lastName, link:g.createLink(controller: 'user', action: 'show', id:it.student?.id)],
               urlID: it.id
            ]
        }

        def data = [
                totalRecords: list.size(),
                results: ret
        ]

        render data as JSON
    }

    def dataTableDataAsJSON = {
        def list = []
        def ret = []
        response.setHeader("Cache-Control", "no-store")

        def userInstance = authenticateService.userDomain()

        if(authenticateService.ifAnyGranted("ROLE_ADMIN,ROLE_FORMATIONMANAGER,ROLE_STAFF"))
        {
            def millesimes = Millesime.findAllByBeginDateLessThan(new Date())
            def millesimeCurrent = Millesime.getCurrent()
            def millesimeSelected

            if(params.idMillesime && params.idMillesime != "null")
                    millesimeSelected = Millesime.get(params.idMillesime)
            else
                    millesimeSelected = millesimeCurrent

            if(authenticateService.ifAnyGranted("ROLE_ADMIN"))
            {
                Internship.findAllByMillesime(millesimeSelected).each {
                    if(it.getStatus() == params.status)
                        list.add it.id
                }
            }
            else if(authenticateService.ifAnyGranted("ROLE_FORMATIONMANAGER"))
            {
                def manager = FormationManager.findByUser(userInstance)
                def promotion = Promotion.findByMillesimeAndFormation(millesimeSelected, manager?.formation)
                if(promotion?.students.size() > 0)
                {
                    def internships = Internship.createCriteria().list() {
                        'in'('student', promotion?.students)
                        eq('millesime', millesimeSelected)
                    }

                    internships.each {
                        if(it.getStatus() == params.status)
                            list.add it.id
                    }
                }
            }
            else if(authenticateService.ifAnyGranted("ROLE_STAFF"))
            {
                def staff = Staff.findByUser(userInstance)
                Internship.findAllByAcademicTutorAndMillesime(staff, millesimeSelected).each {
                    if(it.getStatus() == 'internship.validated')
                        list.add it.id
                }
            }

        }
        else if(authenticateService.ifAnyGranted("ROLE_STUDENT")) {
            def student = Student.findByUser(userInstance)
            Internship.findAllByStudent(student).each {
                if(it.getStatus() == params.status)
                    list.add it.id
            }
        }
        else if(authenticateService.ifAnyGranted("ROLE_EXTERNAL")) {
            def external = External.findByUser(userInstance)
            external.internships.each {
                if(it.getStatus() == params.status)
                    list.add it.id
            }
        }

		def filter = {
			and {
			  'in'('id', list)
			  if(params.subject && params.subject != ''){
				ilike("subject", "${params.subject}%")
			  }
			}
		}
		
		def list2
		if(list.size() > 0) {
			list2 = Internship.createCriteria().list(params,filter)
		}
	
        list2.each {
            ret << [
               subject:it.subject,
               beginAt:it?.beginAt.formatDate(),
               isApproval:it.isApproval,
               student:[name:it.student?.user?.firstName + " " + it.student?.user?.lastName, link:g.createLink(controller: 'user', action: 'show', id:it.student?.user?.id)],
               urlID: it.id
            ]
        }

        def data = [
                totalRecords: list.size()>0?Internship.createCriteria().count(filter):0,
                results: ret
        ]

        render data as JSON
    }

    def getAcademicTutorList = {
        def internshipInstance = Internship.get(params.id)

        if(internshipInstance)
        {
            response.setHeader("Cache-Control", "no-store")

            def staff = Staff.list().collect {
                [
                        id: it.id,
                        value: it.user?.firstName + " " + it.user?.lastName
                ]
            }

            def list = g.select(
                            name:'assignate_' + internshipInstance.id,
                            from:staff,
                            optionKey:'id',
                            optionValue:'value',
                            value:internshipInstance.academicTutor?.id,
                            noSelection:[null: ''])

            render list
        }
    }

}
