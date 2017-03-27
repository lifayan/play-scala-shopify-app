package controllers

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import silhouette.SessionEnv

@Singleton
class ProductAppLinkController @Inject()(
  val messagesApi: MessagesApi,
  silhouette: Silhouette[SessionEnv]
) extends Controller with I18nSupport {
  def product(id: Long, shop: String) = silhouette.SecuredAction { implicit request =>
    Ok(views.html.product(request.identity))
  }
}
