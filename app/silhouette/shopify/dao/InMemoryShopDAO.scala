package silhouette.shopify.dao

import javax.inject.Singleton

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import silhouette.shopify.Shop

import scala.collection.mutable
import scala.concurrent.Future

@Singleton
class InMemoryShopDAO @Inject()() extends ShopDAO {
  val store: mutable.Map[LoginInfo, Shop] = mutable.HashMap.empty

  override def findByLoginInfo(loginInfo: LoginInfo) = {
    store.synchronized {
      Future.successful(store.get(loginInfo))
    }
  }

  override def save(shop: Shop): Future[Shop] = {
    store.synchronized {
      store += (shop.loginInfo -> shop)
    }
    Future.successful(shop)
  }
}
