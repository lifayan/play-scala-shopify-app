package silhouette.shopify.dao

import com.mohiva.play.silhouette.api.LoginInfo
import silhouette.shopify.Shop

import scala.concurrent.Future

trait ShopDAO {
  def findByLoginInfo(loginInfo: LoginInfo): Future[Option[Shop]]
  def save(shop: Shop): Future[Shop]
}
