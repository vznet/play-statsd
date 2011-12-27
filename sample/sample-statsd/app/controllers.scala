package controllers

import play._
import play.mvc._

object Application extends Controller {
    
    import views.Application._
    
    def index = {
        html.index("Your Scala application is ready!")
    }
    
}
