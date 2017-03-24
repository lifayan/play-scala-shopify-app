package controllers

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class HomeControllerSpec extends PlaySpec with OneAppPerTest {

  "HomeController GET" should {

    "render the index page from the application" in {
      val controller = app.injector.instanceOf[HomeController]
      val home = controller.index().apply(FakeRequest())

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
    }

    "render the index page from the router" in {
      // Need to specify Host header to get through AllowedHostsFilter
      val request = FakeRequest(GET, "/").withHeaders("Host" -> "localhost")
      val home = route(app, request).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
    }

    "accept the form post from the application" in {
      val controller = app.injector.instanceOf[HomeController]
      val formPostResult = controller.postDomainForm.apply(
        FakeRequest().withFormUrlEncodedBody(("domain" -> "test-domain"))
      )

      status(formPostResult) mustBe OK
    }

    "accept the form post from the router" in {
      val request = FakeRequest(POST, "/postDomainForm")
        .withHeaders("Host" -> "localhost")
        .withFormUrlEncodedBody(("domain" -> "test-domain"))
      val formPostResult = route(app, request).get

      status(formPostResult) mustBe OK
    }
  }
}
