package me.hcl.seekin.Util

import nl.captcha.Captcha
import nl.captcha.backgrounds.*
import nl.captcha.servlet.CaptchaServletUtil
import org.codehaus.groovy.grails.plugins.springsecurity.Secured

class ContactController {

    static navigation = [
            group:'menu',
            order:4,
            title:'contact',
            action:'index'
    ]

    def emailerService

    @Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
    def index = { ContactCommand cmd ->

        if(request.method == 'POST')
        {

            if (!session?.captcha?.isCorrect(params.captcha))
            {
                cmd.errors.rejectValue(
                   'captcha',
                   'user.code.dismatch',
                )
            }

            if(cmd.hasErrors())
            {
                return [contactInstance: cmd]
            }
            else
            {
                flash.message = "contact.message.sent"
                emailerService.buildContactMail(params)
            }

        }

        return [contactInstance: cmd]
    }

    /**
     * Captcha generation
     */
    @Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
    def generateCaptcha = {
        // Generate a captcha of 150px X 50px with gimp, noise and border
        // And add the associated text to the current user session
        def captcha = new Captcha.Builder(150, 50)
            .addText()
            .gimp()
            .addNoise()
            .addBorder()
            .build()
        session.captcha = captcha
        // Write the captcha
        CaptchaServletUtil.writeImage(response, captcha.image)
    }

}
