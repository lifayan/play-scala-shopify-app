package silhouette.shopify

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}

case class Shop(
  loginInfo: LoginInfo,
  name: String,
  myshopifyDomain: String
) extends Identity
