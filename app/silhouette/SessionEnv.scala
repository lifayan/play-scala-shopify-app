package silhouette

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import silhouette.shopify.Shop

trait SessionEnv extends Env {
  type I = Shop
  type A = SessionAuthenticator
}
