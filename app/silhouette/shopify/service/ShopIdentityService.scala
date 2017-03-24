package silhouette.shopify.service

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import silhouette.shopify.Shop

import scala.concurrent.Future

trait ShopIdentityService extends IdentityService[Shop] {
  def retrieve(loginInfo: LoginInfo): Future[Option[Shop]]
  def save(shop: Shop): Future[Shop]
  def save(profile: CommonSocialProfile): Future[Shop]
}
