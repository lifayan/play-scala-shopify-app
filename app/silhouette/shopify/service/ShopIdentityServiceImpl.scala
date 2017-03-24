package silhouette.shopify.service

import javax.inject.Singleton

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import silhouette.shopify.Shop
import silhouette.shopify.dao.ShopDAO

import scala.concurrent.Future

@Singleton
class ShopIdentityServiceImpl @Inject() (shopDao: ShopDAO) extends ShopIdentityService {
  override def retrieve(loginInfo: LoginInfo) = {
    shopDao.findByLoginInfo(loginInfo)
  }

  override def save(shop: Shop): Future[Shop] = {
    shopDao.save(shop)
  }

  override def save(profile: CommonSocialProfile): Future[Shop] = {
    shopDao.save(Shop(
      loginInfo = profile.loginInfo,
      name = profile.fullName.getOrElse("Unknown Shop Name"),
      myshopifyDomain = profile.loginInfo.providerKey
    ))
  }
}
