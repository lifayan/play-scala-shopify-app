package controllers

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.{Logger, LoginEvent, Silhouette}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.impl.providers.OAuth2Settings
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Controller
import silhouette.SessionEnv
import silhouette.shopify.ShopifyProvider
import silhouette.shopify.service.ShopIdentityService

import scala.concurrent.Future

@Singleton
class ShopifyAuthController @Inject()(
  val messagesApi: MessagesApi,
  silhouette: Silhouette[SessionEnv],
  shopifyProvider: ShopifyProvider,
  shopIdentityService: ShopIdentityService,
  authInfoRepository: AuthInfoRepository
) extends Controller with I18nSupport with Logger {
  private def baseURL(shop: String) = s"https://$shop"

  private def oauthURL(shop: String) = s"https://$shop/admin/oauth/authorize"

  private def accessTokenURL(shop: String) = s"https://$shop/admin/oauth/access_token"

  def authenticate(shop: Option[String]) = silhouette.UnsecuredAction.async { implicit request =>
    println(shop)
    println(shop.map { s => baseURL(s) })
    val configuredProvider = shopifyProvider.withSettings { config: OAuth2Settings =>
      config.copy(
        authorizationURL = shop.map { s => oauthURL(s) },
        redirectURL = routes.ShopifyAuthController.authenticate(None).url,
        accessTokenURL = accessTokenURL(shop.getOrElse("")),
        apiURL = shop.map { s => baseURL(s) }
      )
    }
    configuredProvider.authenticate().flatMap {
      case Left(result) => Future.successful(result)
      case Right(authInfo) => for {
        profile <- configuredProvider.retrieveProfile(authInfo)
        shop <- shopIdentityService.save(profile)
        _ <- authInfoRepository.save(profile.loginInfo, authInfo)
        authenticator <- silhouette.env.authenticatorService.create(profile.loginInfo)
        value <- silhouette.env.authenticatorService.init(authenticator)
        result <- silhouette.env.authenticatorService.embed(value, Redirect(routes.HomeController.login()))
      } yield {
        silhouette.env.eventBus.publish(LoginEvent(shop, request))
        result
      }
    }.recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        Redirect(routes.HomeController.index()).flashing("error" -> Messages("could.not.authenticate"))
    }
  }
}
