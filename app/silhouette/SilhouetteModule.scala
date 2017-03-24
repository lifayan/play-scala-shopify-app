package silhouette

import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.crypto.{AuthenticatorEncoder, CookieSigner, Crypter, CrypterAuthenticatorEncoder}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus, Silhouette, SilhouetteProvider}
import com.mohiva.play.silhouette.crypto.{JcaCookieSigner, JcaCookieSignerSettings, JcaCrypter, JcaCrypterSettings}
import com.mohiva.play.silhouette.impl.authenticators.{SessionAuthenticator, SessionAuthenticatorService, SessionAuthenticatorSettings}
import com.mohiva.play.silhouette.impl.providers.oauth2.state.{CookieStateProvider, CookieStateSettings}
import com.mohiva.play.silhouette.impl.providers.{OAuth2Info, OAuth2Settings, OAuth2StateProvider, SocialProviderRegistry}
import com.mohiva.play.silhouette.impl.util.{DefaultFingerprintGenerator, PlayCacheLayer, SecureRandomIDGenerator}
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.mohiva.play.silhouette.persistence.daos.{DelegableAuthInfoDAO, InMemoryAuthInfoDAO}
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.WSClient
import silhouette.shopify.ShopifyProvider
import silhouette.shopify.dao.{InMemoryShopDAO, ShopDAO}
import silhouette.shopify.service.{ShopIdentityService, ShopIdentityServiceImpl}

class SilhouetteModule extends AbstractModule with ScalaModule {
  def configure() {
    bind[Silhouette[SessionEnv]].to[SilhouetteProvider[SessionEnv]]
    bind[ShopIdentityService].to[ShopIdentityServiceImpl]
    bind[ShopDAO].to[InMemoryShopDAO]
    bind[CacheLayer].to[PlayCacheLayer]
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[PasswordHasher].toInstance(new BCryptPasswordHasher)
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())

    bind[DelegableAuthInfoDAO[OAuth2Info]].toInstance(new InMemoryAuthInfoDAO[OAuth2Info])
  }

  @Provides
  def provideHTTPLayer(client: WSClient): HTTPLayer = new PlayHTTPLayer(client)

  @Provides
  def provideEnvironment(
    shopIdentityService: ShopIdentityService,
    authenticatorService: AuthenticatorService[SessionAuthenticator],
    eventBus: EventBus
  ): Environment[SessionEnv] = {
    Environment[SessionEnv](
      shopIdentityService,
      authenticatorService,
      Seq(),
      eventBus
    )
  }

  @Provides
  def provideSocialProviderRegistry(shopifyProvider: ShopifyProvider): SocialProviderRegistry = {
    SocialProviderRegistry(Seq(
      shopifyProvider
    ))
  }

  @Provides @Named("oauth2-state-cookie-signer")
  def provideOAuth2StageCookieSigner(configuration: Configuration): CookieSigner = {
    val config = configuration.underlying.as[JcaCookieSignerSettings]("silhouette.oauth2StateProvider.cookie.signer")

    new JcaCookieSigner(config)
  }

  @Provides
  def provideAuthInfoRepository(oauth2InfoDAO: DelegableAuthInfoDAO[OAuth2Info]): AuthInfoRepository = {
    new DelegableAuthInfoRepository(oauth2InfoDAO)
  }

  @Provides @Named("authenticator-cryptor")
  def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {
    val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.authenticator.crypter")

    new JcaCrypter(config)
  }

  @Provides @Named("authenticator-encoder")
  def provideAuthenticatorEncoder(@Named("authenticator-cryptor") crypter: Crypter): AuthenticatorEncoder = {
    new CrypterAuthenticatorEncoder(crypter)
  }

  @Provides
  def provideAuthenticatorService(
    fingerprintGenerator: FingerprintGenerator,
    @Named("authenticator-encoder") authenticatorEncoder: AuthenticatorEncoder,
    clock: Clock,
    configuration: Configuration
  ): AuthenticatorService[SessionAuthenticator] = {
    val config = configuration.underlying.as[SessionAuthenticatorSettings]("silhouette.authenticator")

    new SessionAuthenticatorService(
      config,
      fingerprintGenerator,
      authenticatorEncoder,
      clock
    )
  }

  @Provides
  def provideOAuth2StateProvider(
    idGenerator: IDGenerator,
    @Named("oauth2-state-cookie-signer") cookieSigner: CookieSigner,
    configuration: Configuration,
    clock: Clock
  ): OAuth2StateProvider = {
    val settings = configuration.underlying.as[CookieStateSettings]("silhouette.oauth2StateProvider")
    new CookieStateProvider(settings, idGenerator, cookieSigner, clock)
  }

  @Provides
  def provideShopifyProvider(
    httpLayer: HTTPLayer,
    stateProvider: OAuth2StateProvider,
    configuration: Configuration
  ): ShopifyProvider = {
    new ShopifyProvider(httpLayer, stateProvider, configuration.underlying.as[OAuth2Settings]("silhouette.shopify"))
  }
}
