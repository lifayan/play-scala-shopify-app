package controllers

import javax.inject._

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.filters.csrf.CSRF
import silhouette.SessionEnv
import silhouette.shopify.ShopifyProvider
import silhouette.shopify.service.ShopIdentityService

case class ShopDomain(shopName: String) {
  def fullDomain = {
    s"$shopName.myshopify.com"
  }


}

@Singleton
class HomeController @Inject()(
  val messagesApi: MessagesApi,
  silhouette: Silhouette[SessionEnv]
) extends Controller with I18nSupport {

  val domainForm = Form(
    mapping(
      "shopName" -> nonEmptyText
    )(ShopDomain.apply)(ShopDomain.unapply)
  )

  def index = silhouette.UnsecuredAction { implicit request =>
    val token = CSRF.getToken(request)
    Ok(views.html.index(domainForm))
  }

  def login = silhouette.SecuredAction { implicit request =>
    Ok(views.html.login(request.identity))
  }

  def postDomainForm = silhouette.UnsecuredAction { implicit request =>
    domainForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.index(formWithErrors))
      },
      domainData => {
        Redirect(routes.ShopifyAuthController.authenticate(Some(domainData.fullDomain)))
      }
    )
  }
}
