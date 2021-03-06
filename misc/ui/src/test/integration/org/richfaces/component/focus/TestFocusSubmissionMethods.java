package org.richfaces.component.focus;

import static org.jboss.arquillian.graphene.Graphene.guardHttp;
import static org.jboss.arquillian.graphene.Graphene.guardXhr;
import static org.jboss.arquillian.graphene.Graphene.waitAjax;
import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.richfaces.context.ExtendedPartialViewContext;
import org.richfaces.integration.MiscDeployment;
import org.richfaces.javascript.JavaScriptService;
import org.richfaces.renderkit.FocusRendererBase;
import org.richfaces.shrinkwrap.descriptor.FaceletAsset;

@RunAsClient
@WarpTest
@RunWith(Arquillian.class)
public class TestFocusSubmissionMethods {

    @Drone
    private WebDriver browser;

    @ArquillianResource
    private URL contextPath;

    @FindBy(id = "form:submit")
    private WebElement submitButton;

    @FindBy(id = "form:ajaxJSF")
    private WebElement ajaxJSF;

    @FindBy(id = "form:ajaxRF")
    private WebElement ajaxRF;

    @FindBy(id = "form:ajaxCommandButton")
    private WebElement ajaxCommandButton;

    @FindBy(id = "form:input2")
    private WebElement input2;

    @Deployment
    public static WebArchive createDeployment() {
        MiscDeployment deployment = new MiscDeployment(TestFocusSubmissionMethods.class);

        deployment.archive().addClasses(ComponentBean.class);
        deployment.archive().addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        addIndexPage(deployment);

        return deployment.getFinalArchive();
    }

    @Before
    public void openInitialPage() {
        browser.get(contextPath.toExternalForm());
    }

    @Test
    public void testFocusAfterFormSubmission() {
        // when
        input2.click();
        guardHttp(submitButton).click();

        // then
        assertEquals(input2, FocusRetriever.retrieveActiveElement());
    }

    /**
     * Won't work with f:ajax since it does not support {@link ExtendedPartialViewContext} with oncomplete, which is used by
     * {@link JavaScriptService} in method
     * {@link FocusRendererBase#renderOncompleteScript(javax.faces.context.FacesContext, String)}.
     */
    @Test
    @Ignore
    public void testFocusAfterAjaxJSF() {
        // when
        input2.click();
        guardXhr(ajaxJSF).click();

        // then
        waitAjax().until(new ElementIsFocused(input2));
    }

    @Test
    public void testFocusAfterAjaxRF() {
        // when
        input2.click();
        guardXhr(ajaxRF).click();

        // then
        waitAjax().until(new ElementIsFocused(input2));
    }

    @Test
    public void testFocusAfterAjaxCommandButton() {
        // when
        input2.click();
        guardXhr(ajaxCommandButton).click();

        // then
        waitAjax().until(new ElementIsFocused(input2));
    }

    private static void addIndexPage(MiscDeployment deployment) {
        FaceletAsset p = new FaceletAsset();
        p.xmlns("rich", "http://richfaces.org/misc");
        p.xmlns("a4j", "http://richfaces.org/a4j");

        p.body("<h:form id='form'>");
        p.body("    <rich:focus id='focus' preserve='true' />");

        p.body("    <h:inputText id='input1' />");
        p.body("    <h:inputText id='input2' />");

        p.body("    <h:commandButton id='submit' value='Submit' />");

        p.body("    <h:commandButton id='ajaxJSF' value='Ajax JSF'>");
        p.body("        <f:ajax render='@form' />");
        p.body("    </h:commandButton>");

        p.body("    <h:commandButton id='ajaxRF' value='Ajax RF'>");
        p.body("        <a4j:ajax render='@form' />");
        p.body("    </h:commandButton>");

        p.body("    <a4j:commandButton id='ajaxCommandButton' render='@form' value='Ajax Command Button' />");
        p.body("</h:form>");

        deployment.archive().addAsWebResource(p, "index.xhtml");
    }
}
