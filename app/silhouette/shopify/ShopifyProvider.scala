package silhouette.shopify

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.providers._
import play.api.libs.json.{JsObject, JsValue}

import scala.concurrent.Future

trait BaseShopifyProvider extends OAuth2Provider {
  import ShopifyProvider._

  override type Content = JsValue

  override val id = ID

  // Neds to be a def so it's evaluated with dynamic settings
  override protected def urls: Map[String,String] = settings.apiURL.map {url =>
    ("api" -> s"${url}/admin/shop.json")
  }.toMap

  override protected def buildProfile(authInfo: OAuth2Info): Future[Profile] = {
    println(settings)
    httpLayer.url(urls("api")).withHeaders("X-Shopify-Access-Token"-> s"${authInfo.accessToken}").get().flatMap { response =>
      val json = response.json
      profileParser.parse(json, authInfo)
    }
  }
}

class ShopifyProfileParser extends SocialProfileParser[JsValue, CommonSocialProfile, OAuth2Info]{
  import ShopifyProvider._

  override def parse(json: JsValue, authInfo: OAuth2Info) = Future.successful {
    val shopName = (json \ "shop" \ "name").as[String]
    val shopDomain = (json \ "shop" \ "myshopify_domain").as[String]
    CommonSocialProfile(
      loginInfo = LoginInfo(ID, shopDomain),
      fullName = Some(shopName)
    )
  }
}

class ShopifyProvider(
  protected val httpLayer: HTTPLayer,
  override val stateProvider: OAuth2StateProvider,
  val settings: OAuth2Settings
) extends BaseShopifyProvider with CommonSocialProfileBuilder {

  override type Self = ShopifyProvider

  override val profileParser = new ShopifyProfileParser

  override def withSettings(f: (Settings) => Settings) = {
    println(settings)
    println(f(settings))
    new ShopifyProvider(httpLayer, stateProvider, f(settings))
  }
}

object ShopifyProvider {
  val ID = "shopify"
}